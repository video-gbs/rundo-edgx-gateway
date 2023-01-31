package com.runjian.mq.gatewayBusiness;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.runjian.common.commonDto.Gateway.req.NoneStreamReaderReq;
import com.runjian.common.commonDto.Gateway.req.RecordInfoReq;
import com.runjian.common.commonDto.StreamInfo;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.CommonResponse;
import com.runjian.common.constant.GatewayMsgType;
import com.runjian.common.constant.LogTemplate;
import com.runjian.common.mq.domain.GatewayMqDto;
import com.runjian.gb28181.bean.Device;
import com.runjian.service.IDeviceChannelService;
import com.runjian.service.IplayService;
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
            String msgType = gatewayMqDto.getMsgType();
            if(msgType.equals(GatewayMsgType.PLAY_STREAM_CALLBACK.getTypeName())){
                StreamInfo streamInfo = JSONObject.toJavaObject((JSONObject)gatewayMqDto.getData(),StreamInfo.class);
                iplayService.onStreamChanges(streamInfo);
            }else if(msgType.equals(GatewayMsgType.PLAY_NONE_STREAM_READER_CALLBACK.getTypeName())){
                NoneStreamReaderReq noneStreamReaderReq = JSONObject.toJavaObject((JSONObject)gatewayMqDto.getData(),NoneStreamReaderReq.class);
                iplayService.onStreamNoneReader(noneStreamReaderReq);
            }if(msgType.equals(GatewayMsgType.RECORD_INFO.getTypeName())){
                RecordInfoReq recordInfoReq = JSONObject.toJavaObject((JSONObject)gatewayMqDto.getData(), RecordInfoReq.class);
                recordInfoReq.setMsgId(gatewayMqDto.getMsgId());
                deviceChannelService.recordInfo(recordInfoReq);
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
