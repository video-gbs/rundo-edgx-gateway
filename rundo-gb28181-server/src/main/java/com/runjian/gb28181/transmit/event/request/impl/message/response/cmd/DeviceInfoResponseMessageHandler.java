package com.runjian.gb28181.transmit.event.request.impl.message.response.cmd;

import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.common.constant.BusinessSceneConstants;
import com.runjian.common.constant.LogTemplate;
import com.runjian.common.utils.redis.RedisCommonUtil;
import com.runjian.gb28181.bean.Device;
import com.runjian.gb28181.bean.ParentPlatform;
import com.runjian.gb28181.transmit.callback.DeferredResultHolder;
import com.runjian.gb28181.transmit.callback.RequestMessage;
import com.runjian.gb28181.transmit.event.request.SIPRequestProcessorParent;
import com.runjian.gb28181.transmit.event.request.impl.message.IMessageHandler;
import com.runjian.gb28181.transmit.event.request.impl.message.response.ResponseMessageHandler;
import com.runjian.service.IDeviceService;
import gov.nist.javax.sip.message.SIPRequest;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.SipException;
import javax.sip.message.Response;
import java.text.ParseException;

import static com.runjian.gb28181.utils.XmlUtil.getText;
/**
 * DeviceInfo应答消息处理
 * @author lin
 */
@Component
public class DeviceInfoResponseMessageHandler extends SIPRequestProcessorParent implements InitializingBean, IMessageHandler {

    private Logger logger = LoggerFactory.getLogger(DeviceInfoResponseMessageHandler.class);
    private final String cmdType = "DeviceInfo";

    @Autowired
    private ResponseMessageHandler responseMessageHandler;



    @Autowired
    private IDeviceService deviceService;

    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public void afterPropertiesSet() throws Exception {
        responseMessageHandler.addHandler(cmdType, this);
    }

    @Override
    public void handForDevice(RequestEvent evt, Device device, Element rootElement) {
        logger.info(LogTemplate.PROCESS_LOG_TEMPLATE, "DeviceInfo应答消息处理", "接收到消息");
        SIPRequest request = (SIPRequest) evt.getRequest();
        BusinessSceneResp<Device> deviceBusinessSceneResp;
        String redisKey = BusinessSceneConstants.DEVICE_INFO_SCENE_KEY+device.getDeviceId();
        // 检查设备是否存在， 不存在则不回复
        if (device == null || device.getOnline() == 0) {
            logger.warn(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "DeviceInfo应答消息处理", "接收到应答消息,但是设备已经离线", (device != null ? device.getDeviceId():"" ));
            deviceBusinessSceneResp = BusinessSceneResp.addSceneEnd(BusinessErrorEnums.SIP_DEVICE_NOTFOUND_EVENT, null);
            RedisCommonUtil.setOverWrite(redisTemplate,redisKey,deviceBusinessSceneResp);
            return;
        }
        try {
            rootElement = getRootElement(evt, device.getCharset());

            if (rootElement == null) {
                logger.warn("[ 接收到DeviceInfo应答消息 ] content cannot be null, {}", evt.getRequest());
                logger.warn(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "DeviceInfo应答消息处理", "应答消息，content cannot be null", evt.getRequest());
                try {
                    responseAck((SIPRequest) evt.getRequest(), Response.BAD_REQUEST);
                } catch (SipException | InvalidArgumentException | ParseException e) {
                    logger.error(LogTemplate.ERROR_LOG_TEMPLATE, "DeviceInfo应答消息处理", "命令发送失败,BAD_REQUEST", e);
                    deviceBusinessSceneResp = BusinessSceneResp.addSceneEnd(BusinessErrorEnums.SIP_SEND_EXCEPTION, null);
                    RedisCommonUtil.setOverWrite(redisTemplate,redisKey,deviceBusinessSceneResp);
                }
                return;
            }
            Element deviceIdElement = rootElement.element("DeviceID");
            device.setName(getText(rootElement, "DeviceName"));

            device.setManufacturer(getText(rootElement, "Manufacturer"));
            device.setModel(getText(rootElement, "Model"));
            device.setFirmware(getText(rootElement, "Firmware"));

            deviceService.updateDevice(device);

        } catch (Exception e) {
            logger.error(LogTemplate.ERROR_LOG_TEMPLATE, "DeviceInfo应答消息处理", "消息解析处理失败", e);
            deviceBusinessSceneResp = BusinessSceneResp.addSceneEnd(BusinessErrorEnums.BUSINESS_SCENE_EXCEPTION, null);
            RedisCommonUtil.setOverWrite(redisTemplate,redisKey,deviceBusinessSceneResp);
        }

        try {
            // 回复200 OK
            responseAck(request, Response.OK);
        } catch (SipException | InvalidArgumentException | ParseException e) {
            logger.error(LogTemplate.ERROR_LOG_TEMPLATE, "DeviceInfo应答消息处理", "命令发送失败", e);

        }
        //处理完成
        deviceBusinessSceneResp = BusinessSceneResp.addSceneEnd(BusinessErrorEnums.SUCCESS, device);
        RedisCommonUtil.setOverWrite(redisTemplate,redisKey,deviceBusinessSceneResp);

    }

    @Override
    public void handForPlatform(RequestEvent evt, ParentPlatform parentPlatform, Element rootElement) {

    }
}
