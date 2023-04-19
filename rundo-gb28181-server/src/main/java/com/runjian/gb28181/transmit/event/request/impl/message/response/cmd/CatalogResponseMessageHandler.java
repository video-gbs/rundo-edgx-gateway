package com.runjian.gb28181.transmit.event.request.impl.message.response.cmd;

import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.common.constant.BusinessSceneConstants;
import com.runjian.common.constant.GatewayMsgType;
import com.runjian.common.constant.LogTemplate;
import com.runjian.conf.UserSetting;
import com.runjian.domain.dto.CatalogMqSyncDto;
import com.runjian.gb28181.bean.*;
import com.runjian.gb28181.session.CatalogDataCatch;
import com.runjian.gb28181.transmit.event.request.SIPRequestProcessorParent;
import com.runjian.gb28181.transmit.event.request.impl.message.IMessageHandler;
import com.runjian.gb28181.transmit.event.request.impl.message.response.ResponseMessageHandler;
import com.runjian.gb28181.utils.XmlUtil;
import com.runjian.service.IDeviceChannelService;
import com.runjian.service.IRedisCatchStorageService;
import gov.nist.javax.sip.message.SIPRequest;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.SipException;
import javax.sip.message.Response;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 目录查询的回复
 */
@Component
public class CatalogResponseMessageHandler extends SIPRequestProcessorParent implements InitializingBean, IMessageHandler {

    private Logger logger = LoggerFactory.getLogger(CatalogResponseMessageHandler.class);
    private final String cmdType = "Catalog";

    private boolean taskQueueHandlerRun = false;

    @Autowired
    private ResponseMessageHandler responseMessageHandler;

    private ConcurrentLinkedQueue<HandlerCatchData> taskQueue = new ConcurrentLinkedQueue<>();

    @Autowired
    private IDeviceChannelService storager;

    @Autowired
    private CatalogDataCatch catalogDataCatch;

    @Qualifier("taskExecutor")
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserSetting userSetting;

    @Autowired
    IRedisCatchStorageService redisCatchStorageService;

    @Override
    public void afterPropertiesSet() throws Exception {
        responseMessageHandler.addHandler(cmdType, this);
    }

    @Override
    public void handForDevice(RequestEvent evt, Device device, Element element) {

        taskQueue.offer(new HandlerCatchData(evt, device, element));
        // 回复200 OK
        try {
            responseAck((SIPRequest) evt.getRequest(), Response.OK);
        } catch (SipException | InvalidArgumentException | ParseException e) {
            logger.error(LogTemplate.ERROR_LOG_TEMPLATE, "目录查询回复", "命令发送失败", e);
        }
        taskExecutor.execute(() -> {
            //获取同步的设备数据
            HandlerCatchData take = taskQueue.poll();
            if(ObjectUtils.isEmpty(take)){
                return;
            }

            String businessSceneKey = GatewayMsgType.CATALOG.getTypeName()+BusinessSceneConstants.SCENE_SEM_KEY+take.getDevice().getDeviceId();

            BusinessSceneResp businessSceneRedis = redisCatchStorageService.getOneBusinessSceneKey(businessSceneKey);

            Element rootElement = null;
            try {
                rootElement = getRootElement(take.getEvt(), take.getDevice().getCharset());
            } catch (DocumentException e) {
                logger.error(LogTemplate.ERROR_LOG_TEMPLATE, "目录查询回复", "xml解析失败", e);
            }
            if (rootElement == null) {
                logger.warn(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "目录查询回复", "[ 收到通道 ] content cannot be null", evt.getRequest());
            }
            Element deviceListElement = rootElement.element("DeviceList");
            Element sumNumElement = rootElement.element("SumNum");
            Element snElement = rootElement.element("SN");
            int sumNum = Integer.parseInt(sumNumElement.getText());

            if (sumNum == 0) {
                logger.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "目录查询回复", "[收到通道]设备: 0个", take.getDevice().getDeviceId());
                // 数据已经完整接收
                storager.cleanChannelsForDevice(take.getDevice().getDeviceId());
                catalogDataCatch.setChannelSyncEnd(take.getDevice().getDeviceId(), null,0);

                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.CATALOG,BusinessErrorEnums.SUCCESS,new CatalogMqSyncDto());
            } else {
                Iterator<Element> deviceListIterator = deviceListElement.elementIterator();
                if (deviceListIterator != null) {
                    List<DeviceChannel> channelList = new ArrayList<>();
                    // 遍历DeviceList
                    while (deviceListIterator.hasNext()) {
                        Element itemDevice = deviceListIterator.next();
                        Element channelDeviceElement = itemDevice.element("DeviceID");
                        if (channelDeviceElement == null) {
                            continue;
                        }
                        DeviceChannel deviceChannel = XmlUtil.channelContentHander(itemDevice, device, null);
                        deviceChannel.setDeviceId(take.getDevice().getDeviceId());

                        channelList.add(deviceChannel);
                    }
                    int sn = Integer.parseInt(snElement.getText());
                    //超过超时时间便不在继续接收处理
                    CatalogData catalogData = catalogDataCatch.getData(take.getDevice().getDeviceId());
                    if(!ObjectUtils.isEmpty(catalogData)){
                        if(catalogData.getSn() == sn){
                            //仅仅处理本次请求的相关数据,至于上一次的请求数据先不处理
                            //预留10毫秒的过期时间
                            LocalDateTime expireTime = businessSceneRedis.getTime().minus(10, ChronoUnit.MILLIS);
                            if(expireTime.isBefore(LocalDateTime.now())){
                                //同步已经超时
                                logger.error(LogTemplate.ERROR_LOG_TEMPLATE, "目录查询回复", "查询过程超时,放弃后续的数据库同步", take.getDevice().getDeviceId());
//                                    catalogDataCatch.setChannelSyncEnd(take.getDevice().getDeviceId(), null,0);
                                if (catalogData.getStatus().equals(CatalogData.CatalogDataStatus.runIng)) {
                                    //只处理正在执行的通道同步

                                    storager.resetChannelsForcatalog(catalogData.getDevice().getDeviceId(), catalogData.getChannelList());

                                    CatalogMqSyncDto catalogMqSyncDto = new CatalogMqSyncDto();
                                    catalogMqSyncDto.setTotal(catalogData.getTotal());
                                    catalogMqSyncDto.setNum(catalogData.getChannelList().size());
                                    catalogMqSyncDto.setChannelDetailList(catalogData.getChannelList());
                                    //更新redis

                                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.CATALOG,BusinessErrorEnums.SUCCESS,catalogMqSyncDto);
                                    catalogDataCatch.setChannelSyncEnd(catalogData.getDevice().getDeviceId(), null,0);
                                    return;
                                }

                            }

                        }


                    }

                    catalogDataCatch.put(take.getDevice().getDeviceId(), sn, sumNum, take.getDevice(), channelList);
                    logger.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "目录查询回复", "[收到通道]", "设备id" + take.getDevice().getDeviceId() + " 通道数量:" + channelList.size() + " " + (catalogDataCatch.get(take.getDevice().getDeviceId()) == null ? 0 : catalogDataCatch.get(take.getDevice().getDeviceId()).size()) + "/" + sumNum);

                    if (catalogDataCatch.get(take.getDevice().getDeviceId()).size() == sumNum) {
                        // 数据已经完整接收， 此时可能存在某个设备离线变上线的情况，但是考虑到性能，此处不做处理，
                        // 目前支持设备通道上线通知时和设备上线时向上级通知
                        List<DeviceChannel> deviceChannels = catalogDataCatch.get(take.getDevice().getDeviceId());
                        storager.resetChannelsForcatalog(take.getDevice().getDeviceId(), deviceChannels);

                        CatalogMqSyncDto catalogMqSyncDto = new CatalogMqSyncDto();
                        catalogMqSyncDto.setTotal(sumNum);
                        catalogMqSyncDto.setNum(sumNum);
                        catalogMqSyncDto.setChannelDetailList(deviceChannels);
                        redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.CATALOG,BusinessErrorEnums.SUCCESS,catalogMqSyncDto);
                        //该结束状态用于删除之前的本地缓存数据
                        catalogDataCatch.setChannelSyncEnd(take.getDevice().getDeviceId(), null,0);
                    }
                }

            }


            });

    }

    @Override
    public void handForPlatform(RequestEvent evt, ParentPlatform parentPlatform, Element rootElement) {

    }

    public SyncStatus getChannelSyncProgress(String deviceId) {
        if (catalogDataCatch.get(deviceId) == null) {
            return null;
        } else {
            return catalogDataCatch.getSyncStatus(deviceId);
        }
    }

    public boolean isSyncRunning(String deviceId) {
        if (catalogDataCatch.get(deviceId) == null) {
            return false;
        } else {
            return catalogDataCatch.isSyncRunning(deviceId);
        }
    }

    public void setChannelSyncReady(Device device, int sn) {
        catalogDataCatch.addReady(device, sn);
    }


}
