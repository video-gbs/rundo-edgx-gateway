package com.runjian.gb28181.transmit.event.request.impl;

import com.runjian.common.constant.LogTemplate;
import com.runjian.gb28181.transmit.SIPProcessorObserver;
import com.runjian.gb28181.transmit.cmd.ISIPCommander;
import com.runjian.gb28181.transmit.event.request.ISIPRequestProcessor;
import com.runjian.gb28181.transmit.event.request.SIPRequestProcessorParent;
import com.runjian.service.IDeviceService;
import gov.nist.javax.sip.message.SIPRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.SipException;
import javax.sip.address.SipURI;
import javax.sip.header.CallIdHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderAddress;
import javax.sip.header.ToHeader;
import javax.sip.message.Response;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

/**
 * SIP命令类型： BYE请求
 */
@Component
public class ByeRequestProcessor extends SIPRequestProcessorParent implements InitializingBean, ISIPRequestProcessor {

	private final Logger logger = LoggerFactory.getLogger(ByeRequestProcessor.class);
	private final String method = "BYE";

	@Autowired
	private ISIPCommander cmder;



	@Autowired
	private IDeviceService deviceService;

	@Autowired
	private SIPProcessorObserver sipProcessorObserver;

	@Override
	public void afterPropertiesSet() throws Exception {
		// 添加消息处理的订阅
		sipProcessorObserver.addRequestProcessor(method, this);
	}

	/**
	 * 处理BYE请求
	 * @param evt
	 */
	@Override
	public void process(RequestEvent evt) {

		try {
			responseAck((SIPRequest) evt.getRequest(), Response.OK);
		} catch (SipException | InvalidArgumentException | ParseException e) {
			logger.error(LogTemplate.ERROR_LOG_MSG_TEMPLATE, "SIP命令BYE请求处理", "回复BYE信息失败",this.getClass().getName(), e);
		}


	}
}
