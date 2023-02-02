package com.runjian.mq.gatewayBusiness;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.runjian.common.commonDto.Gateway.dto.GatewayBindMedia;
import com.runjian.common.commonDto.Gateway.req.*;
import com.runjian.common.commonDto.StreamInfo;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.CommonResponse;
import com.runjian.common.constant.GatewayMsgType;
import com.runjian.common.constant.LogTemplate;
import com.runjian.common.mq.domain.GatewayMqDto;
import com.runjian.common.utils.BeanUtil;
import com.runjian.domain.dto.DeviceDto;
import com.runjian.gb28181.bean.Device;
import com.runjian.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 业务状态下队列监听--动态监听
 * @author chenjialing
 */
@Slf4j
@Component
public class GatewayBusinessMqListener implements ChannelAwareMessageListener {

    @Autowired
    IplayService iplayService;

    @Autowired
    IDeviceChannelService deviceChannelService;

    @Autowired
    IDeviceService deviceService;

    @Autowired
    IPtzService ptzService;

    @Autowired
    IGatewayBaseService gatewayBaseService;
    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        try {
            String msg = new String(message.getBody());
            log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "网关业务消息监听服务网关业务消息监听服务", "接收到返回消息", msg);

            JSONObject jsonMsg = JSONObject.parseObject(msg);

            //转化为接收的通用mq信息
            GatewayMqDto gatewayMqDto = JSONObject.toJavaObject(jsonMsg, GatewayMqDto.class);

            //目前的消息，注册返回，心跳返回(code,data,msg),data(GatewayMqDto中判断消息的类型)
            Integer code = gatewayMqDto.getCode();
            if(!code.equals(BusinessErrorEnums.SUCCESS.getErrCode())){
                //注册信息失败，无法进行业务队列的监听于发送 todo
                log.error(LogTemplate.ERROR_LOG_MSG_TEMPLATE, "网关业务消息监听服务", "消息接收成功，上层服务创建队列失败", jsonMsg, "");
                return;
            }
            JSONObject dataJson = (JSONObject)gatewayMqDto.getData();
            //实际的请求参数
            JSONObject dataMapJson = dataJson.getJSONObject("dataMap");
            String msgType = gatewayMqDto.getMsgType();
            if(msgType.equals(GatewayMsgType.PLAY_STREAM_CALLBACK.getTypeName())){
                StreamInfo streamInfo = JSONObject.toJavaObject((JSONObject)gatewayMqDto.getData(),StreamInfo.class);
                iplayService.onStreamChanges(streamInfo);
            }else if(msgType.equals(GatewayMsgType.PLAY_NONE_STREAM_READER_CALLBACK.getTypeName())){
                NoneStreamReaderReq noneStreamReaderReq = JSONObject.toJavaObject((JSONObject)gatewayMqDto.getData(),NoneStreamReaderReq.class);
                iplayService.onStreamNoneReader(noneStreamReaderReq);
            }else if(msgType.equals(GatewayMsgType.RECORD_INFO.getTypeName())){
                //录像列表
                String deviceId = dataJson.getString("deviceId");
                String channelId = dataJson.getString("channelId");
                RecordInfoReq recordInfoReq = JSONObject.toJavaObject(dataMapJson, RecordInfoReq.class);
                recordInfoReq.setChannelId(channelId);
                recordInfoReq.setDeviceId(deviceId);
                recordInfoReq.setMsgId(gatewayMqDto.getMsgId());
                recordInfoReq.setMsgId(gatewayMqDto.getMsgId());
                deviceChannelService.recordInfo(recordInfoReq);
            }else if(msgType.equals(GatewayMsgType.DEVICEINFO.getTypeName())){
                //设备信息同步  获取设备信息
                String deviceId = dataJson.getString("deviceId");
                DeviceDto deviceDto = deviceService.getDevice(deviceId);
                Device device = new Device();
                BeanUtil.copyProperties(deviceDto,device);
                deviceService.deviceInfoQuery(device, gatewayMqDto.getMsgId());

            }else if (msgType.equals(GatewayMsgType.CATALOG.getTypeName())){
                //设备通道信息同步
                String deviceId = dataJson.getString("deviceId");
                DeviceDto deviceDto = deviceService.getDevice(deviceId);
                Device device = new Device();
                BeanUtil.copyProperties(deviceDto,device);
                deviceService.sync(device, gatewayMqDto.getMsgId());
            }else if (msgType.equals(GatewayMsgType.PLAY.getTypeName())){
                //设备点播同步
                String deviceId = dataJson.getString("deviceId");
                String channelId = dataJson.getString("channelId");
                PlayReq playReq = JSONObject.toJavaObject(dataMapJson, PlayReq.class);
                playReq.setDeviceId(deviceId);
                playReq.setChannelId(channelId);
                playReq.setMsgId(gatewayMqDto.getMsgId());
                iplayService.play(playReq);
            }else if (msgType.equals(GatewayMsgType.PLAY_BACK.getTypeName())){
                //设备点播同步
                String deviceId = dataJson.getString("deviceId");
                String channelId = dataJson.getString("channelId");
                PlayBackReq playBackReq = JSONObject.toJavaObject(dataMapJson, PlayBackReq.class);
                playBackReq.setDeviceId(deviceId);
                playBackReq.setChannelId(channelId);
                playBackReq.setMsgId(gatewayMqDto.getMsgId());
                iplayService.playBack(playBackReq);
            }else if (msgType.equals(GatewayMsgType.DEVICE_DELETE.getTypeName())) {
                String deviceId = dataJson.getString("deviceId");
                deviceService.deviceDelete(deviceId,gatewayMqDto.getMsgId());
            }else if (msgType.equals(GatewayMsgType.PTZ_CONTROL.getTypeName())) {
                String deviceId = dataJson.getString("deviceId");
                String channelId = dataJson.getString("channelId");
                DeviceControlReq deviceControlReq = JSONObject.toJavaObject(dataMapJson, DeviceControlReq.class);
                deviceControlReq.setDeviceId(deviceId);
                deviceControlReq.setChannelId(channelId);
                deviceControlReq.setMsgId(gatewayMqDto.getMsgId());
                ptzService.deviceControl(deviceControlReq);
            }else if(msgType.equals(GatewayMsgType.GATEWAY_BIND_MEDIA.getTypeName())){
                GatewayBindMedia gatewayBindMedia = JSONObject.toJavaObject(dataMapJson, GatewayBindMedia.class);
                gatewayBindMedia.setMsgId(gatewayBindMedia.getMsgId());
                gatewayBaseService.gatewayBindMedia(gatewayBindMedia);
            }


        }catch (Exception ex){
            log.error(LogTemplate.ERROR_LOG_MSG_TEMPLATE, "网关业务消息监听服务", "消息接收成功，处理失败", message, ex);
        }finally {
            // 手动确认消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

    public static void main(String[] args) {
        GatewayMqDto gatewayMqDto = new GatewayMqDto();
        gatewayMqDto.setMsgType("sign-in");
        gatewayMqDto.setSerialNum("asdasd");
        gatewayMqDto.setMsgId("b_123123");
        Device device = new Device();
        device.setCharset("asdas");
        device.setDeviceId("12112312");
        gatewayMqDto.setData(device);
        CommonResponse<GatewayMqDto> success = CommonResponse.success(gatewayMqDto);
        String s = JSON.toJSONString(success);

        JSONObject jsonObject = JSON.parseObject(s);
        System.out.println(JSON.toJSONString(success));

    }
}
