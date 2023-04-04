package com.runjian.service.impl;

import com.runjian.common.commonDto.Gateway.req.ChannelPtzControlReq;
import com.runjian.common.commonDto.Gateway.req.DeviceControlReq;
import com.runjian.common.commonDto.Gateway.req.DragZoomControlReq;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.constant.*;
import com.runjian.conf.UserSetting;
import com.runjian.gb28181.bean.Device;
import com.runjian.gb28181.bean.DeviceChannel;
import com.runjian.gb28181.transmit.cmd.impl.SIPCommander;
import com.runjian.service.IDeviceChannelService;
import com.runjian.service.IDeviceService;
import com.runjian.service.IPtzService;
import com.runjian.service.IRedisCatchStorageService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author chenjialing
 */
@Service
@Slf4j
public class PtzServiceImpl implements IPtzService {
    @Autowired
    IDeviceService deviceService;

    @Autowired
    IDeviceChannelService deviceChannelService;
    @Autowired
    RedissonClient redissonClient;

    @Autowired
    IRedisCatchStorageService redisCatchStorageService;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    UserSetting userSetting;

    @Autowired
    SIPCommander sipCommander;

//    case "left":
//        cmdCode = 2;
//        break;
//    case "right":
//        cmdCode = 1;
//        break;
//    case "up":
//        cmdCode = 8;
//        break;
//    case "down":
//        cmdCode = 4;
//        break;
//    case "upleft":
//        cmdCode = 10;
//        break;
//    case "upright":
//        cmdCode = 9;
//        break;
//    case "downleft":
//        cmdCode = 6;
//        break;
//    case "downright":
//        cmdCode = 5;
//        break;
//    case "zoomin":
//        cmdCode = 16;
//        break;
//    case "zoomout":
//        cmdCode = 32;
//        break;
//    case "stop":
//        cmdCode = 0;
    private List<Integer> commomCodeArray = Arrays.asList(2,1,8,4,10,9,6,5,16,32,0);

    //逐渐废弃
    @Override
    public void deviceControl(DeviceControlReq deviceControlReq) {

        String businessSceneKey = GatewayMsgType.PTZ_CONTROL.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+deviceControlReq.getDeviceId()+BusinessSceneConstants.SCENE_STREAM_KEY+deviceControlReq.getChannelId();
        RLock lock = redissonClient.getLock(businessSceneKey);
        try {
            //阻塞型,默认是30s无返回参数
            redisCatchStorageService.addBusinessSceneKey(businessSceneKey,GatewayMsgType.PTZ_CONTROL,deviceControlReq.getMsgId());
            //尝试获取锁
            boolean b = lock.tryLock(0,userSetting.getBusinessSceneTimeout()+100, TimeUnit.MILLISECONDS);
            if(!b){
                //加锁失败，不继续执行
                log.info(LogTemplate.PROCESS_LOG_TEMPLATE,"设备信息同步请求,加锁失败，合并全局的请求",deviceControlReq.getMsgId());
                return;
            }
            //参数校验
            //判断设备是否存在
            String deviceId = deviceControlReq.getDeviceId();
            Device device = deviceService.getDevice(deviceId);
            if(ObjectUtils.isEmpty(device)){
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.PTZ_CONTROL,BusinessErrorEnums.DB_DEVICE_NOT_FOUND,null);
                return;

            }
            int cmdCode = deviceControlReq.getCmdCode();
            if(!commomCodeArray.contains(cmdCode)){

                log.error(LogTemplate.ERROR_LOG_TEMPLATE, "ptz服务", "ptz操作失败", deviceControlReq);
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.PTZ_CONTROL,BusinessErrorEnums.VALID_BIND_EXCEPTION_ERROR,null);
                return;
            }
            sipCommander.frontEndCmd(device, deviceControlReq.getChannelId(), cmdCode, deviceControlReq.getHorizonSpeed(), deviceControlReq.getVerticalSpeed(), deviceControlReq.getZoomSpeed());
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.PTZ_CONTROL,BusinessErrorEnums.SUCCESS,null);

        }catch (Exception e){
            log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "ptz服务", "ptz操作失败", deviceControlReq);
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.PTZ_CONTROL,BusinessErrorEnums.UNKNOWN_ERROR,null);
        }

    }

    @Override
    public void ptzControl(ChannelPtzControlReq channelPtzControlReq) {
        //进行预置位操作
        //校验参数
        String msgId = channelPtzControlReq.getMsgId();;
        String businessSceneKey = GatewayMsgType.CHANNEL_PTZ_OPERATION.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+channelPtzControlReq.getDeviceId()+BusinessSceneConstants.SCENE_STREAM_KEY+channelPtzControlReq.getChannelId()+BusinessSceneConstants.SCENE_STREAM_KEY+channelPtzControlReq.getPtzOperationType();
        RLock lock = redissonClient.getLock(businessSceneKey);

        try {
            //阻塞型,默认是30s无返回参数
            redisCatchStorageService.addBusinessSceneKey(businessSceneKey,GatewayMsgType.CHANNEL_PTZ_OPERATION,msgId);
            //尝试获取锁
            boolean b = lock.tryLock(0,userSetting.getBusinessSceneTimeout()+100, TimeUnit.MILLISECONDS);
            if(!b){
                //加锁失败，不继续执行
                log.info(LogTemplate.PROCESS_LOG_TEMPLATE,"设备信息同步请求,加锁失败，合并全局的请求",msgId);
                return;
            }
            PtzOperationTypeEnum presetOperationTypeEnumOne = PtzOperationTypeEnum.getTypeByTypeId(channelPtzControlReq.getPtzOperationType());
            //查询通道数据
            Device device = deviceService.getDevice(channelPtzControlReq.getDeviceId());
            if(ObjectUtils.isEmpty(device)){
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.CHANNEL_PTZ_OPERATION,BusinessErrorEnums.DB_DEVICE_NOT_FOUND,null);
                return;
            }
            //查询通道
            DeviceChannel channelOne = deviceChannelService.getOne(channelPtzControlReq.getDeviceId(), channelPtzControlReq.getChannelId());
            if(ObjectUtils.isEmpty(channelOne)){
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.CHANNEL_PTZ_OPERATION,BusinessErrorEnums.DB_CHANNEL_NOT_FOUND,null);
                return;
            }
            if(ObjectUtils.isEmpty(presetOperationTypeEnumOne)){
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.CHANNEL_PTZ_OPERATION,BusinessErrorEnums.PRESET_OPERATION_TYPE_NOT_FOUND,null);
                return;
            }

            int operationValue = channelPtzControlReq.getOperationValue();
            switch (presetOperationTypeEnumOne){
                case PresetGet:
                    //预置位查询
                    sipCommander.presetQuery(device,channelPtzControlReq.getChannelId(),null);
                    //在预置位回调的sip信令中释放相关的请求
                    break;
                case PresetSet:
                    //预置位设置
                    sipCommander.frontEndCmd(device, channelPtzControlReq.getChannelId(), MarkConstant.PRESET_SET,0, channelPtzControlReq.getOperationValue(),0);
                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.CHANNEL_PTZ_OPERATION, BusinessErrorEnums.SUCCESS,null);
                    break;
                case PresetInvoke:
                    //预置位调用
                    sipCommander.frontEndCmd(device, channelPtzControlReq.getChannelId(), MarkConstant.PRESET_INVOKE,0, channelPtzControlReq.getOperationValue(),0);
                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.CHANNEL_PTZ_OPERATION, BusinessErrorEnums.SUCCESS,null);
                    break;
                case PresetDel:
                    //预置位删除
                    sipCommander.frontEndCmd(device, channelPtzControlReq.getChannelId(), MarkConstant.PRESET_DEL,0, channelPtzControlReq.getOperationValue(),0);
                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.CHANNEL_PTZ_OPERATION, BusinessErrorEnums.SUCCESS,null);
                    break;
                case Left:
                    //左转
                    sipCommander.frontEndCmd(device, channelPtzControlReq.getChannelId(), MarkConstant.PTZ_LEFT,operationValue, operationValue,0);
                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.CHANNEL_PTZ_OPERATION, BusinessErrorEnums.SUCCESS,null);
                    break;
                case Right:
                    //右转
                    sipCommander.frontEndCmd(device, channelPtzControlReq.getChannelId(), MarkConstant.PTZ_RIGHT,operationValue, operationValue,0);
                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.CHANNEL_PTZ_OPERATION, BusinessErrorEnums.SUCCESS,null);
                    break;
                case Up:
                    //上
                    sipCommander.frontEndCmd(device, channelPtzControlReq.getChannelId(), MarkConstant.PTZ_UP,operationValue, operationValue,0);
                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.CHANNEL_PTZ_OPERATION, BusinessErrorEnums.SUCCESS,null);
                    break;
                case Down:
                    //下
                    sipCommander.frontEndCmd(device, channelPtzControlReq.getChannelId(), MarkConstant.PTZ_DOWN,operationValue, operationValue,0);
                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.CHANNEL_PTZ_OPERATION, BusinessErrorEnums.SUCCESS,null);
                    break;
                case Upleft:
                    //左上
                    sipCommander.frontEndCmd(device, channelPtzControlReq.getChannelId(), MarkConstant.PTZ_UPLEFT,operationValue, operationValue,0);
                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.CHANNEL_PTZ_OPERATION, BusinessErrorEnums.SUCCESS,null);
                    break;
                case Upright:
                    //右上
                    sipCommander.frontEndCmd(device, channelPtzControlReq.getChannelId(), MarkConstant.PTZ_UPRIGHT,operationValue, operationValue,0);
                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.CHANNEL_PTZ_OPERATION, BusinessErrorEnums.SUCCESS,null);
                    break;
                case Downleft:
                    //左下
                    sipCommander.frontEndCmd(device, channelPtzControlReq.getChannelId(), MarkConstant.PTZ_DOWNLEFT,operationValue, operationValue,0);
                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.CHANNEL_PTZ_OPERATION, BusinessErrorEnums.SUCCESS,null);
                    break;
                case Downright:
                    //右下
                    sipCommander.frontEndCmd(device, channelPtzControlReq.getChannelId(), MarkConstant.PTZ_DOWNRIGHT,operationValue, operationValue,0);
                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.CHANNEL_PTZ_OPERATION, BusinessErrorEnums.SUCCESS,null);
                    break;
                case Zoomin:
                    //倍率缩小
                    sipCommander.frontEndCmd(device, channelPtzControlReq.getChannelId(), MarkConstant.Zoomin,0, 0,operationValue);
                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.CHANNEL_PTZ_OPERATION, BusinessErrorEnums.SUCCESS,null);
                    break;
                case Zoomout:
                    //倍率放大
                    sipCommander.frontEndCmd(device, channelPtzControlReq.getChannelId(), MarkConstant.Zoomout,0, 0,operationValue);
                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.CHANNEL_PTZ_OPERATION, BusinessErrorEnums.SUCCESS,null);
                    break;
                case PtzStop:
                    //ptz停止
                    sipCommander.frontEndCmd(device, channelPtzControlReq.getChannelId(), MarkConstant.PTZ_STOP,operationValue, operationValue,operationValue);
                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.CHANNEL_PTZ_OPERATION, BusinessErrorEnums.SUCCESS,null);
                    break;
                case IrisReduce:
                    //光圈缩小
                    sipCommander.frontEndCmd(device, channelPtzControlReq.getChannelId(), MarkConstant.IrisReduce,0, operationValue,0);
                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.CHANNEL_PTZ_OPERATION, BusinessErrorEnums.SUCCESS,null);
                    break;
                case IrisGrow:
                    //光圈放大
                    sipCommander.frontEndCmd(device, channelPtzControlReq.getChannelId(), MarkConstant.IrisGrow,0, operationValue,0);
                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.CHANNEL_PTZ_OPERATION, BusinessErrorEnums.SUCCESS,null);
                    break;
                case FocusFar:
                    //聚焦近远
                    sipCommander.frontEndCmd(device, channelPtzControlReq.getChannelId(), MarkConstant.FocusNear,operationValue, 0,0);
                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.CHANNEL_PTZ_OPERATION, BusinessErrorEnums.SUCCESS,null);
                    break;
                case FocusNear:
                    //聚焦近远
                    sipCommander.frontEndCmd(device, channelPtzControlReq.getChannelId(), MarkConstant.FocusFar,operationValue, 0,0);
                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.CHANNEL_PTZ_OPERATION, BusinessErrorEnums.SUCCESS,null);
                    break;
                case IrisAndFocusStop:
                    //F1停止
                    sipCommander.frontEndCmd(device, channelPtzControlReq.getChannelId(), MarkConstant.IrisAndFocusStop,operationValue, 0,0);
                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.CHANNEL_PTZ_OPERATION, BusinessErrorEnums.SUCCESS,null);
                    break;

                default:
                    //信令操作异常
                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.CHANNEL_PTZ_OPERATION,BusinessErrorEnums.PRESET_OPERATION_TYPE_NOT_FOUND,null);
                    break;
            }

        }catch (Exception e){
            log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "ptz服务", "预置位操作失败", channelPtzControlReq);
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.CHANNEL_PTZ_OPERATION,BusinessErrorEnums.UNKNOWN_ERROR,null);
        }

    }

    @Override
    public void dragZoomControl(DragZoomControlReq dragZoomControlReq) {
        //进行预置位操作
        //校验参数
        String msgId = dragZoomControlReq.getMsgId();;
        String businessSceneKey = GatewayMsgType.CHANNEL_3D_OPERATION.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+dragZoomControlReq.getDeviceId()+BusinessSceneConstants.SCENE_STREAM_KEY+dragZoomControlReq.getChannelId()+BusinessSceneConstants.SCENE_STREAM_KEY+dragZoomControlReq.getDragOperationType();
        RLock lock = redissonClient.getLock(businessSceneKey);

        try {
            //阻塞型,默认是30s无返回参数
            redisCatchStorageService.addBusinessSceneKey(businessSceneKey, GatewayMsgType.CHANNEL_3D_OPERATION, msgId);
            //尝试获取锁
            boolean b = lock.tryLock(0, userSetting.getBusinessSceneTimeout() + 100, TimeUnit.MILLISECONDS);
            if (!b) {
                //加锁失败，不继续执行
                log.info(LogTemplate.PROCESS_LOG_TEMPLATE, "设备信息同步请求,加锁失败，合并全局的请求", msgId);
                return;
            }
            PtzOperationTypeEnum presetOperationTypeEnumOne = PtzOperationTypeEnum.getTypeByTypeId(dragZoomControlReq.getDragOperationType());
            //查询通道数据
            Device device = deviceService.getDevice(dragZoomControlReq.getDeviceId());
            if(ObjectUtils.isEmpty(device)){
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.CHANNEL_3D_OPERATION,BusinessErrorEnums.DB_DEVICE_NOT_FOUND,null);
                return;
            }
            //查询通道
            DeviceChannel channelOne = deviceChannelService.getOne(dragZoomControlReq.getDeviceId(), dragZoomControlReq.getChannelId());
            if(ObjectUtils.isEmpty(channelOne)){
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.CHANNEL_3D_OPERATION,BusinessErrorEnums.DB_CHANNEL_NOT_FOUND,null);
                return;
            }
            if(ObjectUtils.isEmpty(presetOperationTypeEnumOne)){
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.CHANNEL_3D_OPERATION,BusinessErrorEnums.PRESET_OPERATION_TYPE_NOT_FOUND,null);
                return;
            }
            StringBuffer cmdXml = null;
            int length = dragZoomControlReq.getLength();
            int width = dragZoomControlReq.getWidth();
            int lengthx = dragZoomControlReq.getLengthx();
            int lengthy = dragZoomControlReq.getLengthy();
            int midpointx = dragZoomControlReq.getMidpointx();
            int midpointy = dragZoomControlReq.getMidpointy();
            String channelId = dragZoomControlReq.getChannelId();
            switch (presetOperationTypeEnumOne){
                case DragZoomIn:
                    cmdXml = new StringBuffer(200);
                    cmdXml.append("<DragZoomIn>\r\n");
                    cmdXml.append("<Length>" + length+ "</Length>\r\n");
                    cmdXml.append("<Width>" + width+ "</Width>\r\n");
                    cmdXml.append("<MidPointX>" + midpointx+ "</MidPointX>\r\n");
                    cmdXml.append("<MidPointY>" + midpointy+ "</MidPointY>\r\n");
                    cmdXml.append("<LengthX>" + lengthx+ "</LengthX>\r\n");
                    cmdXml.append("<LengthY>" + lengthy+ "</LengthY>\r\n");
                    cmdXml.append("</DragZoomIn>\r\n");
                    sipCommander.dragZoomCmd(device,channelId,cmdXml.toString());
                    break;
                case DragZoomOut:
                    cmdXml = new StringBuffer(200);
                    cmdXml.append("<DragZoomOut>\r\n");
                    cmdXml.append("<Length>" + length+ "</Length>\r\n");
                    cmdXml.append("<Width>" + width+ "</Width>\r\n");
                    cmdXml.append("<MidPointX>" + midpointx+ "</MidPointX>\r\n");
                    cmdXml.append("<MidPointY>" + midpointy+ "</MidPointY>\r\n");
                    cmdXml.append("<LengthX>" + lengthx+ "</LengthX>\r\n");
                    cmdXml.append("<LengthY>" + lengthy+ "</LengthY>\r\n");
                    cmdXml.append("</DragZoomOut>\r\n");
                    sipCommander.dragZoomCmd(device,channelId,cmdXml.toString());
                    break;
                default:
                    //信令操作异常
                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.CHANNEL_3D_OPERATION,BusinessErrorEnums.PRESET_OPERATION_TYPE_NOT_FOUND,null);
                    break;

            }

        }catch (Exception e){
            log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "拉框服务", "拉框服务操作失败", dragZoomControlReq);
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.CHANNEL_3D_OPERATION,BusinessErrorEnums.UNKNOWN_ERROR,null);
        }
    }
}
