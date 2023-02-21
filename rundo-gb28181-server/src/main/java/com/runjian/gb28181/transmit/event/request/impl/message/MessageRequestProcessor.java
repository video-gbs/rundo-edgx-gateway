package com.runjian.gb28181.transmit.event.request.impl.message;

import com.runjian.common.constant.LogTemplate;
import com.runjian.common.utils.BeanUtil;
import com.runjian.gb28181.bean.Device;
import com.runjian.gb28181.bean.DeviceNotFoundEvent;
import com.runjian.gb28181.bean.ParentPlatform;
import com.runjian.gb28181.bean.RemoteAddressInfo;
import com.runjian.gb28181.event.SipSubscribe;
import com.runjian.gb28181.transmit.SIPProcessorObserver;
import com.runjian.gb28181.transmit.event.request.ISIPRequestProcessor;
import com.runjian.gb28181.transmit.event.request.SIPRequestProcessorParent;
import com.runjian.gb28181.utils.SipUtils;
import com.runjian.service.IDeviceService;
import gov.nist.javax.sip.message.SIPRequest;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.SipException;
import javax.sip.header.CallIdHeader;
import javax.sip.message.Response;
import java.text.ParseException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MessageRequestProcessor extends SIPRequestProcessorParent implements InitializingBean, ISIPRequestProcessor {

    private final static Logger logger = LoggerFactory.getLogger(MessageRequestProcessor.class);

    private final String method = "MESSAGE";

    private static Map<String, IMessageHandler> messageHandlerMap = new ConcurrentHashMap<>();

    @Autowired
    private SIPProcessorObserver sipProcessorObserver;

    @Autowired
    private SipSubscribe sipSubscribe;

    @Autowired
    private IDeviceService iDeviceService;
    @Override
    public void afterPropertiesSet() throws Exception {
        // 添加消息处理的订阅
        sipProcessorObserver.addRequestProcessor(method, this);
    }

    public void addHandler(String name, IMessageHandler handler) {
        messageHandlerMap.put(name, handler);
    }

    @Override
    public void process(RequestEvent evt) {
        SIPRequest sipRequest = (SIPRequest)evt.getRequest();
        logger.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "SIP消息处理器", "接收到消息", evt.getRequest());
        String deviceId = SipUtils.getUserIdFromFromHeader(evt.getRequest());
        CallIdHeader callIdHeader = sipRequest.getCallIdHeader();

        SIPRequest request = (SIPRequest) evt.getRequest();
        //TODO  查询设备是否存在 设备信息进行缓存
        Device device = iDeviceService.getDevice(deviceId);

        try {
            RemoteAddressInfo remoteAddressFromRequest = SipUtils.getRemoteAddressFromRequest(request, false);

            String hostAddress = remoteAddressFromRequest.getIp();
            int remotePort = remoteAddressFromRequest.getPort();
            if (!device.getHostAddress().equals(hostAddress + ":" + remotePort)) {

                device = null;
            }
            if (device == null) {
                // 不存在则回复404
                responseAck(request, Response.NOT_FOUND, "device "+ deviceId +" not found");
                logger.warn(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "SIP消息处理器", "设备未找到", "设备id" + deviceId);
                if (sipSubscribe.getErrorSubscribe(callIdHeader.getCallId()) != null){
                    DeviceNotFoundEvent deviceNotFoundEvent = new DeviceNotFoundEvent(evt.getDialog());
                    deviceNotFoundEvent.setCallId(callIdHeader.getCallId());
                    SipSubscribe.EventResult eventResult = new SipSubscribe.EventResult(deviceNotFoundEvent);
                    sipSubscribe.getErrorSubscribe(callIdHeader.getCallId()).response(eventResult);
                };

                return;
            }else {
                Element rootElement = null;
                try {
                    rootElement = getRootElement(evt);
                    if (rootElement == null) {
                        logger.error(LogTemplate.ERROR_LOG_MSG_TEMPLATE, "SIP消息处理器", "处理MESSAGE请求  未获取到消息体", evt.getRequest(), null);
                        responseAck(request, Response.BAD_REQUEST, "content is null");
                        return;
                    }
                } catch (DocumentException e) {
                    logger.warn(LogTemplate.ERROR_LOG_TEMPLATE, "SIP消息处理器", "解析XML消息内容异常", e);
                    // 不存在则回复404
                    responseAck(request, Response.BAD_REQUEST, e.getMessage());
                }
                String name = rootElement.getName();
                IMessageHandler messageHandler = messageHandlerMap.get(name);
                if (messageHandler != null) {
                    messageHandler.handForDevice(evt, device, rootElement);
                }else {
                    // 不支持的message
                    // 不存在则回复415
                    responseAck(request, Response.UNSUPPORTED_MEDIA_TYPE, "Unsupported message type, must Control/Notify/Query/Response");
                }
            }
        } catch (SipException e) {
            logger.warn(LogTemplate.ERROR_LOG_TEMPLATE, "SIP消息处理器", "SIP 回复错误", e);
        } catch (InvalidArgumentException e) {
            logger.warn(LogTemplate.ERROR_LOG_TEMPLATE, "SIP消息处理器", "参数无效", e);
        } catch (ParseException e) {
            logger.warn(LogTemplate.ERROR_LOG_TEMPLATE, "SIP消息处理器", "SIP回复时解析异常", e);
        }
    }


}
