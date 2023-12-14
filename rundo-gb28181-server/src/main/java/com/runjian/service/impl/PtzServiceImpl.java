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
    IRedisCatchStorageService redisCatchStorageService;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    UserSetting userSetting;

    @Autowired
    SIPCommander sipCommander;

    private final String commonValue = "value";
    private final String horizonSpeed = "horizonSpeed";
    private final String verticalSpeed = "verticalSpeed";

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

        String businessSceneKey = GatewayBusinessMsgType.PTZ_CONTROL.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+deviceControlReq.getDeviceId()+BusinessSceneConstants.SCENE_STREAM_KEY+deviceControlReq.getChannelId();
        try {
            //阻塞型,默认是30s无返回参数
            boolean b = redisCatchStorageService.addBusinessSceneKey(businessSceneKey,GatewayBusinessMsgType.PTZ_CONTROL,deviceControlReq.getMsgId(),0,null);
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
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,BusinessErrorEnums.DB_DEVICE_NOT_FOUND,null);
                return;

            }
            int cmdCode = deviceControlReq.getCmdCode();
            if(!commomCodeArray.contains(cmdCode)){

                log.error(LogTemplate.ERROR_LOG_TEMPLATE, "ptz服务", "ptz操作失败", deviceControlReq);
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,BusinessErrorEnums.VALID_BIND_EXCEPTION_ERROR,null);
                return;
            }
            sipCommander.frontEndCmd(device, deviceControlReq.getChannelId(), cmdCode, deviceControlReq.getHorizonSpeed(), deviceControlReq.getVerticalSpeed(), deviceControlReq.getZoomSpeed());
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,BusinessErrorEnums.SUCCESS,null);

        }catch (Exception e){
            log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "ptz服务", "ptz操作失败", deviceControlReq);
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,BusinessErrorEnums.UNKNOWN_ERROR,null);
        }

    }

    @Override
    public void ptzControl(ChannelPtzControlReq channelPtzControlReq) {
        //进行预置位操作
        //校验参数
        String msgId = channelPtzControlReq.getMsgId();;
        String businessSceneKey = GatewayBusinessMsgType.PTZ_CONTROL.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+channelPtzControlReq.getDeviceId()+BusinessSceneConstants.SCENE_STREAM_KEY+channelPtzControlReq.getChannelId()+BusinessSceneConstants.SCENE_STREAM_KEY+channelPtzControlReq.getCmdCode();
        try {
            //阻塞型,默认是30s无返回参数
            boolean b = redisCatchStorageService.addBusinessSceneKey(businessSceneKey,GatewayBusinessMsgType.PTZ_CONTROL,msgId,0,null);
            //尝试获取锁
            if(!b){
                //加锁失败，不继续执行
                log.info(LogTemplate.PROCESS_LOG_TEMPLATE,"设备信息同步请求,加锁失败，合并全局的请求",msgId);
                return;
            }
            PtzOperationTypeEnum ptzOperationTypeEnum = PtzOperationTypeEnum.getTypeByTypeId(channelPtzControlReq.getCmdCode());
            //查询通道数据
            Device device = deviceService.getDevice(channelPtzControlReq.getDeviceId());
            if(ObjectUtils.isEmpty(device)){
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,BusinessErrorEnums.DB_DEVICE_NOT_FOUND,null);
                return;
            }
            if(ObjectUtils.isEmpty(ptzOperationTypeEnum)){
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,BusinessErrorEnums.PTZ_OPERATION_TYPE_NOT_FOUND,null);
                return;
            }

            //通用的值
            int operationValue = channelPtzControlReq.getCmdValue();
            //云台方向值
            int horizonSpeedValue = (int) channelPtzControlReq.getHorizonSpeed();
            int verticalSpeedValue = (int)channelPtzControlReq.getVerticalSpeed();


            switch (ptzOperationTypeEnum){
                case PRESET_SET:
                    //预置位设置
                    sipCommander.frontEndCmd(device, channelPtzControlReq.getChannelId(), ptzOperationTypeEnum.getCode(),0, operationValue,0);
                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey, BusinessErrorEnums.SUCCESS,null);
                    break;
                case PRESET_INVOKE:
                    //预置位调用
                    sipCommander.frontEndCmd(device, channelPtzControlReq.getChannelId(), ptzOperationTypeEnum.getCode(),0, operationValue,0);
                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey, BusinessErrorEnums.SUCCESS,null);
                    break;
                case PRESET_DEL:
                    //预置位删除
                    sipCommander.frontEndCmd(device, channelPtzControlReq.getChannelId(), ptzOperationTypeEnum.getCode(),0, operationValue,0);
                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey, BusinessErrorEnums.SUCCESS,null);
                    break;
                case PTZ_LEFT:
                    //左转
                    sipCommander.frontEndCmd(device, channelPtzControlReq.getChannelId(), ptzOperationTypeEnum.getCode(),operationValue, operationValue,0);
                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey, BusinessErrorEnums.SUCCESS,null);
                    break;
                case PTZ_RIGHT:
                    //右转
                    sipCommander.frontEndCmd(device, channelPtzControlReq.getChannelId(), ptzOperationTypeEnum.getCode(),operationValue, operationValue,0);
                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey, BusinessErrorEnums.SUCCESS,null);
                    break;
                case PTZ_UP:
                    //上
                    sipCommander.frontEndCmd(device, channelPtzControlReq.getChannelId(), ptzOperationTypeEnum.getCode(),operationValue, operationValue,0);
                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey, BusinessErrorEnums.SUCCESS,null);
                    break;
                case PTZ_DOWN:
                    //下
                    sipCommander.frontEndCmd(device, channelPtzControlReq.getChannelId(), ptzOperationTypeEnum.getCode(),operationValue, operationValue,0);
                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey, BusinessErrorEnums.SUCCESS,null);
                    break;
                case PTZ_UPLEFT:
                    //左上
                    sipCommander.frontEndCmd(device, channelPtzControlReq.getChannelId(), ptzOperationTypeEnum.getCode(),horizonSpeedValue, verticalSpeedValue,0);
                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey, BusinessErrorEnums.SUCCESS,null);
                    break;
                case PTZ_UPRIGHT:
                    //右上
                    sipCommander.frontEndCmd(device, channelPtzControlReq.getChannelId(), ptzOperationTypeEnum.getCode(),horizonSpeedValue, verticalSpeedValue,0);
                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey, BusinessErrorEnums.SUCCESS,null);
                    break;
                case PTZ_DOWNLEFT:
                    //左下
                    sipCommander.frontEndCmd(device, channelPtzControlReq.getChannelId(), ptzOperationTypeEnum.getCode(),horizonSpeedValue, verticalSpeedValue,0);
                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey, BusinessErrorEnums.SUCCESS,null);
                    break;
                case PTZ_DOWNRIGHT:
                    //右下
                    sipCommander.frontEndCmd(device, channelPtzControlReq.getChannelId(), ptzOperationTypeEnum.getCode(),horizonSpeedValue, verticalSpeedValue,0);
                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey, BusinessErrorEnums.SUCCESS,null);
                    break;
                case ZOOM_IN:
                    //倍率放大
                    sipCommander.frontEndCmd(device, channelPtzControlReq.getChannelId(), ptzOperationTypeEnum.getCode(),0, 0,operationValue);
                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey, BusinessErrorEnums.SUCCESS,null);
                    break;
                case ZOOM_OUT:
                    //倍率缩小
                    sipCommander.frontEndCmd(device, channelPtzControlReq.getChannelId(), ptzOperationTypeEnum.getCode(),0, 0,operationValue);
                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey, BusinessErrorEnums.SUCCESS,null);
                    break;
                case PTZ_STOP:
                    //ptz停止
                    sipCommander.frontEndCmd(device, channelPtzControlReq.getChannelId(), ptzOperationTypeEnum.getCode(),0, 0,0);
                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey, BusinessErrorEnums.SUCCESS,null);
                    break;
                case IRIS_REDUCE:
                    //光圈缩小
                    sipCommander.frontEndCmd(device, channelPtzControlReq.getChannelId(), ptzOperationTypeEnum.getCode(),0, operationValue,0);
                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey, BusinessErrorEnums.SUCCESS,null);
                    break;
                case IRIS_GROW:
                    //光圈放大
                    sipCommander.frontEndCmd(device, channelPtzControlReq.getChannelId(), ptzOperationTypeEnum.getCode(),0, operationValue,0);
                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey, BusinessErrorEnums.SUCCESS,null);
                    break;
                case FOCUS_FAR:
                    //聚焦近远
                    sipCommander.frontEndCmd(device, channelPtzControlReq.getChannelId(), ptzOperationTypeEnum.getCode(),operationValue, 0,0);
                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey, BusinessErrorEnums.SUCCESS,null);
                    break;
                case FOCUS_NEAR:
                    //聚焦近远
                    sipCommander.frontEndCmd(device, channelPtzControlReq.getChannelId(), ptzOperationTypeEnum.getCode(),operationValue, 0,0);
                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey, BusinessErrorEnums.SUCCESS,null);
                    break;
                case IRISE_AND_FOCUS_STOP:
                    //F1停止
                    sipCommander.frontEndCmd(device, channelPtzControlReq.getChannelId(), ptzOperationTypeEnum.getCode(),0, 0,0);
                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey, BusinessErrorEnums.SUCCESS,null);
                    break;

                default:
                    //信令操作异常
                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey,BusinessErrorEnums.PTZ_OPERATION_TYPE_NOT_FOUND,null);
                    break;
            }

        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_MSG_TEMPLATE, "ptz服务", "ptz操作失败", channelPtzControlReq,e);
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,BusinessErrorEnums.UNKNOWN_ERROR,null);
        }

    }

    @Override
    public void ptzPresetControl(String deviceId, String channelId, String msgId) {
        String businessSceneKey = GatewayBusinessMsgType.CHANNEL_PTZ_PRESET.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+deviceId+BusinessSceneConstants.SCENE_STREAM_KEY+channelId;

        try {
            //阻塞型,默认是30s无返回参数
            boolean b = redisCatchStorageService.addBusinessSceneKey(businessSceneKey, GatewayBusinessMsgType.CHANNEL_PTZ_PRESET, msgId,0,null);
            if (!b) {
                //加锁失败，不继续执行
                log.info(LogTemplate.PROCESS_LOG_TEMPLATE, "设备信息同步请求,加锁失败，合并全局的请求", msgId);
                return;
            }
            Device device = deviceService.getDevice(deviceId);
            if(ObjectUtils.isEmpty(device)){
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,BusinessErrorEnums.DB_DEVICE_NOT_FOUND,null);
                return;
            }

            sipCommander.presetQuery(device,channelId,null);
        }catch (Exception e){
            log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "ptz服务", "预置位查询失败", msgId);
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,BusinessErrorEnums.UNKNOWN_ERROR,null);
        }

    }

    @Override
    public void dragZoomControl(DragZoomControlReq dragZoomControlReq) {
        //进行预置位操作
        //校验参数
        String msgId = dragZoomControlReq.getMsgId();;
        String businessSceneKey = GatewayBusinessMsgType.CHANNEL_3D_OPERATION.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+dragZoomControlReq.getDeviceId()+BusinessSceneConstants.SCENE_STREAM_KEY+dragZoomControlReq.getChannelId()+BusinessSceneConstants.SCENE_STREAM_KEY+dragZoomControlReq.getDragType();


        try {
            //阻塞型,默认是30s无返回参数
            boolean b = redisCatchStorageService.addBusinessSceneKey(businessSceneKey, GatewayBusinessMsgType.CHANNEL_3D_OPERATION, msgId,0,null);
            if (!b) {
                //加锁失败，不继续执行
                log.info(LogTemplate.PROCESS_LOG_TEMPLATE, "设备信息同步请求,加锁失败，合并全局的请求", msgId);
                return;
            }
            DragRoomTypeEnum dragRoomTypeEnumOne = DragRoomTypeEnum.getTypeByTypeId(dragZoomControlReq.getDragType());
            //查询通道数据
            Device device = deviceService.getDevice(dragZoomControlReq.getDeviceId());
            if(ObjectUtils.isEmpty(device)){
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,BusinessErrorEnums.DB_DEVICE_NOT_FOUND,null);
                return;
            }
            //查询通道
            if(ObjectUtils.isEmpty(dragRoomTypeEnumOne)){
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,BusinessErrorEnums.PTZ_OPERATION_TYPE_NOT_FOUND,null);
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
            switch (dragRoomTypeEnumOne){
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
                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey,BusinessErrorEnums.PTZ_OPERATION_TYPE_NOT_FOUND,null);
                    break;

            }
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,BusinessErrorEnums.SUCCESS,null);
        }catch (Exception e){
            log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "拉框服务", "拉框服务操作失败", dragZoomControlReq);
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,BusinessErrorEnums.UNKNOWN_ERROR,null);
        }
    }
}
