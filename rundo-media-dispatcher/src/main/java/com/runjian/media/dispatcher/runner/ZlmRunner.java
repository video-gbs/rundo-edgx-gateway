package com.runjian.media.dispatcher.runner;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.runjian.common.constant.LogTemplate;
import com.runjian.media.dispatcher.conf.DynamicTask;
import com.runjian.media.dispatcher.conf.MediaConfig;
import com.runjian.media.dispatcher.zlm.ZlmHttpHookSubscribe;
import com.runjian.media.dispatcher.zlm.ZLMRESTfulUtils;
import com.runjian.media.dispatcher.zlm.ZLMServerConfig;
import com.runjian.media.dispatcher.zlm.dto.HookSubscribeFactory;
import com.runjian.media.dispatcher.zlm.dto.HookSubscribeForServerStarted;
import com.runjian.media.dispatcher.zlm.dto.MediaServerItem;
import com.runjian.media.dispatcher.zlm.event.publisher.EventPublisher;
import com.runjian.media.dispatcher.zlm.service.ImediaServerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * zlm服务启动管理
 * @author chenjialing
 */
@Component
@Order(value=1)
public class ZlmRunner implements CommandLineRunner {

    private final static Logger logger = LoggerFactory.getLogger(ZlmRunner.class);

    private Map<String, Boolean> startGetMedia;

    @Autowired
    private ZLMRESTfulUtils zlmresTfulUtils;

    @Autowired
    private ZlmHttpHookSubscribe hookSubscribe;

    @Autowired
    private EventPublisher publisher;

    @Autowired
    private ImediaServerService mediaServerService;

    @Autowired
    private MediaConfig mediaConfig;

    @Autowired
    private DynamicTask dynamicTask;


    @Override
    public void run(String... strings) throws Exception {

        MediaServerItem defaultMediaServer = mediaServerService.getDefaultMediaServer();
        if (defaultMediaServer == null) {
            mediaServerService.addToDatabase(mediaConfig.getMediaSerItem());
        }else {
            MediaServerItem mediaSerItem = mediaConfig.getMediaSerItem();
            mediaServerService.updateToDatabase(mediaSerItem);
        }
        HookSubscribeForServerStarted hookSubscribeForServerStarted = HookSubscribeFactory.on_server_started();
        // 订阅 zlm启动事件, 新的zlm也会从这里进入系统
        hookSubscribe.addSubscribe(hookSubscribeForServerStarted,
                (MediaServerItem mediaServerItem, JSONObject response)->{
            ZLMServerConfig zlmServerConfig = JSONObject.toJavaObject(response, ZLMServerConfig.class);
            if (zlmServerConfig !=null ) {
                if (startGetMedia != null) {
                    startGetMedia.remove(zlmServerConfig.getGeneralMediaServerId());
                    if (startGetMedia.size() == 0) {
                        hookSubscribe.removeSubscribe(HookSubscribeFactory.on_server_started());
                    }
                }
            }
        });



        // 获取zlm信息
        logger.info(LogTemplate.PROCESS_LOG_TEMPLATE, "ZLM连接服务", "等待默认zlm中...");

        // 获取所有的zlm， 并开启主动连接
        List<MediaServerItem> all = mediaServerService.getAllFromDatabase();
        Map<String, MediaServerItem> allMap = new HashMap<>();
        if (all.size() == 0) {
            all.add(mediaConfig.getMediaSerItem());
        }
        for (MediaServerItem mediaServerItem : all) {
            if (startGetMedia == null) {
                startGetMedia = new HashMap<>();
            }
            startGetMedia.put(mediaServerItem.getId(), true);
            connectZlmServer(mediaServerItem);
            allMap.put(mediaServerItem.getId(), mediaServerItem);
        }
        String taskKey = "zlm-connect-timeout";
        dynamicTask.startDelay(taskKey, ()->{
            if (startGetMedia != null) {
                Set<String> allZlmId = startGetMedia.keySet();
                for (String id : allZlmId) {
                    logger.error(LogTemplate.PROCESS_LOG_TEMPLATE, "ZLM连接服务", String.format("[ %s ]]主动连接失败，不再尝试连接", id));
                }
                startGetMedia = null;
            }

        }, 60 * 1000 );
    }

    @Async("taskExecutor")
    public void connectZlmServer(MediaServerItem mediaServerItem){
        String connectZlmServerTaskKey = "connect-zlm-" + mediaServerItem.getId();
        ZLMServerConfig zlmServerConfigFirst = getMediaServerConfig(mediaServerItem);
        if (zlmServerConfigFirst != null) {
            zlmServerConfigFirst.setIp(mediaServerItem.getIp());
            zlmServerConfigFirst.setHttpPort(mediaServerItem.getHttpPort());
            startGetMedia.remove(mediaServerItem.getId());
            if (startGetMedia.size() == 0) {
                hookSubscribe.removeSubscribe(HookSubscribeFactory.on_server_started());
            }
            mediaServerService.zlmServerOnline(zlmServerConfigFirst);
        }else {
            logger.info(LogTemplate.PROCESS_LOG_TEMPLATE, "ZLM连接服务", String.format("[ %s ]-[ %s:%s ]主动连接失败, 清理相关资源， 开始尝试重试连接", mediaServerItem.getId(), mediaServerItem.getIp(), mediaServerItem.getHttpPort()));
            publisher.zlmOfflineEventPublish(mediaServerItem.getId());
        }

        dynamicTask.startCron(connectZlmServerTaskKey, ()->{
            ZLMServerConfig zlmServerConfig = getMediaServerConfig(mediaServerItem);
            if (zlmServerConfig != null) {
                dynamicTask.stop(connectZlmServerTaskKey);
                zlmServerConfig.setIp(mediaServerItem.getIp());
                zlmServerConfig.setHttpPort(mediaServerItem.getHttpPort());
                startGetMedia.remove(mediaServerItem.getId());
                if (startGetMedia.size() == 0) {
                    hookSubscribe.removeSubscribe(HookSubscribeFactory.on_server_started());
                }
                mediaServerService.zlmServerOnline(zlmServerConfig);
            }
        }, 2000);
    }

    public ZLMServerConfig getMediaServerConfig(MediaServerItem mediaServerItem) {
        if (startGetMedia == null) { return null;}
        if (!mediaServerItem.isDefaultServer()) {
            return null;
        }
        if ( startGetMedia.get(mediaServerItem.getId()) == null || !startGetMedia.get(mediaServerItem.getId())) {
            return null;
        }
        JSONObject responseJson = zlmresTfulUtils.getMediaServerConfig(mediaServerItem);
        ZLMServerConfig zlmServerConfig = null;
        if (responseJson != null) {
            JSONArray data = responseJson.getJSONArray("data");
            if (data != null && data.size() > 0) {
                zlmServerConfig = JSON.parseObject(JSON.toJSONString(data.get(0)), ZLMServerConfig.class);
            }
        } else {
            logger.error(LogTemplate.ERROR_LOG_TEMPLATE, "ZLM连接服务", String.format("[ %s ]-[ %s:%s ]主动连接失败, 2s后重试", mediaServerItem.getId(), mediaServerItem.getIp(), mediaServerItem.getHttpPort()), null);
        }
        return zlmServerConfig;

    }
}
