package com.runjian.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.runjian.common.commonDto.Gateway.req.RecordInfoReq;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.constant.BusinessSceneConstants;
import com.runjian.common.constant.GatewayBusinessMsgType;
import com.runjian.common.constant.GatewayMsgType;
import com.runjian.common.constant.LogTemplate;
import com.runjian.common.utils.redis.RedisCommonUtil;
import com.runjian.conf.UserSetting;
import com.runjian.gb28181.bean.Device;
import com.runjian.gb28181.bean.DeviceChannel;
import com.runjian.gb28181.transmit.cmd.ISIPCommander;
import com.runjian.service.IDeviceChannelService;
import com.runjian.service.IDeviceService;
import com.runjian.service.IRedisCatchStorageService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author chenjialing
 */
@Service
@Slf4j
public class DeviceChannelServiceImpl implements IDeviceChannelService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    UserSetting userSetting;

    @Autowired
    private ISIPCommander sipCommander;
    @Autowired
    IRedisCatchStorageService redisCatchStorageService;

    @Autowired
    IDeviceService deviceService;

    @Autowired
    DataSourceTransactionManager dataSourceTransactionManager;

    @Autowired
    TransactionDefinition transactionDefinition;

    @Override
    public void recordInfo(RecordInfoReq recordInfoReq) {
        String deviceId = recordInfoReq.getDeviceId();
        String channelId = recordInfoReq.getChannelId();
        String startTime = recordInfoReq.getStartTime();
        String endTime = recordInfoReq.getEndTime();
        String businessSceneKey = GatewayBusinessMsgType.RECORD_INFO.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+deviceId+ BusinessSceneConstants.SCENE_STREAM_KEY+channelId;
        try {
            Boolean b = redisCatchStorageService.addBusinessSceneKey(businessSceneKey, GatewayBusinessMsgType.RECORD_INFO, recordInfoReq.getMsgId(),0,null);
            if(!b){
                //加锁失败，不继续执行
                log.info(LogTemplate.PROCESS_LOG_TEMPLATE,"设备服务,获取录像信息，合并全局的请求",recordInfoReq);
                return;
            }
            Device device = deviceService.getDevice(deviceId);
            if(ObjectUtils.isEmpty(device)){
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,BusinessErrorEnums.DB_DEVICE_NOT_FOUND,null);
                return ;
            }

            int sn  =  (int)((Math.random()*9+1)*100000);
            sipCommander.recordInfoQuery(device, channelId, startTime, endTime, sn, null, null, null, null);
        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "设备服务", "[命令发送失败] 查询设备信息", e);
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,BusinessErrorEnums.UNKNOWN_ERROR,null);

        }
        //在异步线程进行解锁
    }


    @Override
    public void channelHardDelete(String deviceId, String channelId, String msgId) {

        String businessSceneKey = GatewayBusinessMsgType.CHANNEL_DELETE_HARD.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+deviceId+ BusinessSceneConstants.SCENE_STREAM_KEY+channelId;
        try {
            //阻塞型,默认是30s无返回参数
            Boolean b = redisCatchStorageService.addBusinessSceneKey(businessSceneKey, GatewayBusinessMsgType.CHANNEL_DELETE_HARD, msgId, 0,null);
            //尝试获取锁
            if(!b){
                //加锁失败，不继续执行
                log.info(LogTemplate.PROCESS_LOG_TEMPLATE,"设备服务,删除通道，合并全局的请求",msgId);
                return;
            }
            //删除通道
//            deviceChannelMapper.hardDeleteByDeviceId(deviceId,channelId);

            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,BusinessErrorEnums.SUCCESS,true);

        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "设备服务", "删除通道", e);
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,BusinessErrorEnums.UNKNOWN_ERROR,null);

        }
    }

    @Override
    public void channelSoftDelete(String deviceId, String channelId, String msgId) {
        String businessSceneKey = GatewayBusinessMsgType.CHANNEL_DELETE_SOFT.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+deviceId+ BusinessSceneConstants.SCENE_STREAM_KEY+channelId;
        try {
            //阻塞型,默认是30s无返回参数
            boolean b = redisCatchStorageService.addBusinessSceneKey(businessSceneKey, GatewayBusinessMsgType.CHANNEL_DELETE_SOFT,msgId,0,null);
            if(!b){
                //加锁失败，不继续执行
                log.info(LogTemplate.PROCESS_LOG_TEMPLATE,"设备服务,软删除通道，合并全局的请求",msgId);
                return;
            }
            //软删除通道
//            deviceChannelMapper.softDeleteByDeviceIdAndChannelId(deviceId,channelId);

            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,BusinessErrorEnums.SUCCESS,true);

        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "设备服务", "软删除通道", e);
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,BusinessErrorEnums.UNKNOWN_ERROR,null);

        }
    }

    @Override
    public void channelDeleteRecover(String deviceId, String channelId, String msgId) {
        String businessSceneKey = GatewayBusinessMsgType.CHANNEL_DELETE_RECOVER.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+deviceId+ BusinessSceneConstants.SCENE_STREAM_KEY+channelId;
        try {
            //阻塞型,默认是30s无返回参数
            redisCatchStorageService.addBusinessSceneKey(businessSceneKey, GatewayBusinessMsgType.CHANNEL_DELETE_RECOVER,msgId,0,null);

            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,BusinessErrorEnums.SUCCESS,true);

        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "设备服务", "软删除通道恢复", e);
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,BusinessErrorEnums.UNKNOWN_ERROR,null);

        }
    }

    @Override
    public void channelTalk(String deviceId, String channelId,String dispacherUrl, String msgId) {
        String businessSceneKey = GatewayBusinessMsgType.CHANNEL_TALK.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+deviceId+ BusinessSceneConstants.SCENE_STREAM_KEY+channelId;
        try {
            Device device = deviceService.getDevice(deviceId);
            sipCommander.audioBroadcastCmd(device,channelId,event -> {
                //广播指令下发失败
                String errorMsg = String.format("广播指令下发失败，错误码： %s, %s", event.statusCode, event.msg);
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,BusinessErrorEnums.UNKNOWN_ERROR,errorMsg);
            },eventResult -> {
                //成功进行缓存
                RedisCommonUtil.set(redisTemplate,BusinessSceneConstants.GATEWAY_BUSINESS_KEY+businessSceneKey,dispacherUrl,10);
            });
        }catch (Exception e){
            log.info(LogTemplate.ERROR_LOG_TEMPLATE, "设备服务", "语音广播",e);
        }
    }
}
