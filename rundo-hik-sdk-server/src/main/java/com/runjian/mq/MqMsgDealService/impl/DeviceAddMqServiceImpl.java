package com.runjian.mq.MqMsgDealService.impl;

import com.alibaba.fastjson.JSONObject;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.CommonResponse;
import com.runjian.common.constant.BusinessSceneStatusEnum;
import com.runjian.common.constant.GatewayBusinessMsgType;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.entity.DeviceEntity;
import com.runjian.mq.MqMsgDealService.IMqMsgDealServer;
import com.runjian.mq.MqMsgDealService.IMsgProcessorService;
import com.runjian.mq.gatewayBusiness.asyncSender.GatewayBusinessAsyncSender;
import com.runjian.service.IDeviceService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author chenjialing
 */
@Component
public class DeviceAddMqServiceImpl implements InitializingBean, IMsgProcessorService {

    @Autowired
    IMqMsgDealServer iMqMsgDealServer;

    @Autowired
    IDeviceService deviceService;

    @Autowired
    GatewayBusinessAsyncSender gatewayBusinessAsyncSender;
    @Override
    public void afterPropertiesSet() throws Exception {
        iMqMsgDealServer.addRequestProcessor(GatewayBusinessMsgType.DEVICE_ADD.getTypeName(),this);
    }

    @Override
    public void process(CommonMqDto commonMqDto) {
        JSONObject dataJson = (JSONObject) commonMqDto.getData();
        //实际的请求参数
        JSONObject dataMapJson = dataJson.getJSONObject("dataMap");
        //设备信息同步  获取设备信息
        //设备通道信息同步
        String ip = dataMapJson.getString("ip");
        short port = dataMapJson.getShort("port");
        String user = dataMapJson.getString("username");
        String pwd = dataMapJson.getString("password");

        CommonResponse<Long> add = deviceService.add(ip, port, user, pwd);
        gatewayBusinessAsyncSender.sendforAllScene(add, commonMqDto.getMsgId(), GatewayBusinessMsgType.DEVICE_ADD);
        //消息回复
        if(add.getCode() == BusinessErrorEnums.SUCCESS.getErrCode()){
            //进行设备注册
            DeviceEntity one = deviceService.getOne(add.getData());
            CommonResponse<DeviceEntity> success = CommonResponse.success(one);
            gatewayBusinessAsyncSender.sendforAllScene(success, null, GatewayBusinessMsgType.REGISTER);
        }


    }


}
