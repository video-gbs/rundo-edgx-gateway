package com.runjian.gb28181.transmit.event.request.impl.message.query.cmd;

import com.runjian.common.constant.LogTemplate;
import com.runjian.conf.SipConfig;
import com.runjian.gb28181.bean.Device;
import com.runjian.gb28181.bean.ParentPlatform;
import com.runjian.gb28181.transmit.event.request.SIPRequestProcessorParent;
import com.runjian.gb28181.transmit.event.request.impl.message.IMessageHandler;
import com.runjian.gb28181.transmit.event.request.impl.message.query.QueryMessageHandler;
import gov.nist.javax.sip.message.SIPRequest;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.SipException;
import javax.sip.message.Response;
import java.text.ParseException;

/**
 * 告警信息查询处理器
 */
@Component
public class AlarmQueryMessageHandler extends SIPRequestProcessorParent implements InitializingBean, IMessageHandler {

    private Logger logger = LoggerFactory.getLogger(AlarmQueryMessageHandler.class);
    private final String cmdType = "Alarm";

    @Autowired
    private QueryMessageHandler queryMessageHandler;

    @Autowired
    private SipConfig config;



    @Override
    public void afterPropertiesSet() throws Exception {
        queryMessageHandler.addHandler(cmdType, this);
    }

    @Override
    public void handForDevice(RequestEvent evt, Device device, Element element) {

    }

    @Override
    public void handForPlatform(RequestEvent evt, ParentPlatform parentPlatform, Element rootElement) {

        logger.info(LogTemplate.PROCESS_LOG_TEMPLATE, "告警信息查询处理器", "不支持alarm查询");
        try {
             responseAck((SIPRequest) evt.getRequest(), Response.NOT_FOUND, "not support alarm query");
        } catch (SipException | InvalidArgumentException | ParseException e) {
            logger.error(LogTemplate.ERROR_LOG_TEMPLATE, "告警信息查询处理器", "命令发送失败,alarm查询回复200OK", e);
        }

    }
}
