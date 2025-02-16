package com.runjian.gb28181.transmit.event.response.impl;

import com.runjian.common.constant.LogTemplate;
import com.runjian.gb28181.transmit.SIPProcessorObserver;
import com.runjian.gb28181.transmit.SIPSender;
import com.runjian.gb28181.transmit.cmd.SIPRequestHeaderProvider;
import com.runjian.gb28181.transmit.event.response.SIPResponseProcessorAbstract;
import gov.nist.javax.sip.ResponseEventExt;
import gov.nist.javax.sip.message.SIPResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sdp.SdpFactory;
import javax.sdp.SdpParseException;
import javax.sdp.SessionDescription;
import javax.sip.InvalidArgumentException;
import javax.sip.ResponseEvent;
import javax.sip.SipException;
import javax.sip.SipFactory;
import javax.sip.address.SipURI;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.text.ParseException;


/**
 * @description: 处理INVITE响应
 * @author: panlinlin
 * @date: 2021年11月5日 16：40
 */
@Component
public class InviteResponseProcessor extends SIPResponseProcessorAbstract {

	private final static Logger logger = LoggerFactory.getLogger(InviteResponseProcessor.class);
	private final String method = "INVITE";

	@Autowired
	private SIPProcessorObserver sipProcessorObserver;


	@Autowired
	private SipFactory sipFactory;

	@Autowired
	private SIPSender sipSender;

	@Autowired
	private SIPRequestHeaderProvider headerProvider;


	@Override
	public void afterPropertiesSet() throws Exception {
		// 添加消息处理的订阅
		sipProcessorObserver.addResponseProcessor(method, this);
	}



	/**
	 * 处理invite响应
	 * 
	 * @param evt 响应消息
	 * @throws ParseException
	 */
	@Override
	public void process(ResponseEvent evt ){
		try {

			SIPResponse response = (SIPResponse)evt.getResponse();
			int statusCode = response.getStatusCode();
			// trying不会回复
			if (statusCode == Response.TRYING) {
			}
			// 成功响应
			// 下发ack
			if (statusCode == Response.OK) {
				ResponseEventExt event = (ResponseEventExt)evt;

				String contentString = new String(response.getRawContent());
				// jainSip不支持y=字段， 移除以解析。
				int ssrcIndex = contentString.indexOf("y=");
				// 检查是否有y字段
				SessionDescription sdp;
				if (ssrcIndex >= 0) {
					//ssrc规定长度为10字节，不取余下长度以避免后续还有“f=”字段
					String substring = contentString.substring(0, contentString.indexOf("y="));
					sdp = SdpFactory.getInstance().createSessionDescription(substring);
				} else {
					sdp = SdpFactory.getInstance().createSessionDescription(contentString);
				}
				SipURI requestUri = sipFactory.createAddressFactory().createSipURI(sdp.getOrigin().getUsername(), event.getRemoteIpAddress() + ":" + event.getRemotePort());
				Request reqAck = headerProvider.createAckRequest(requestUri, response);

				logger.info(LogTemplate.PROCESS_LOG_TEMPLATE, "处理INVITE响应", String.format("[回复ack] %s-> %s:%s ",sdp.getOrigin().getUsername(), event.getRemoteIpAddress(), event.getRemotePort()));
				sipSender.transmitRequest(reqAck);
			}
		} catch (InvalidArgumentException | ParseException | SipException | SdpParseException e) {
			logger.error(LogTemplate.ERROR_LOG_TEMPLATE, "处理INVITE响应", "点播回复ACK异常", e);
		}
	}

}
