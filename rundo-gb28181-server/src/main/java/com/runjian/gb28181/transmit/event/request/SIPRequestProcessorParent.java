package com.runjian.gb28181.transmit.event.request;

import com.runjian.common.constant.LogTemplate;
import com.runjian.gb28181.transmit.SIPSender;
import com.runjian.gb28181.utils.SipUtils;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.message.SIPResponse;
import org.apache.commons.lang3.ArrayUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.dom.DOMDocument;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sip.*;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.ExpiresHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.io.ByteArrayInputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**    
 * @description:处理接收IPCamera发来的SIP协议请求消息
 * @author: songww
 * @date:   2020年5月3日 下午4:42:22     
 */
public abstract class SIPRequestProcessorParent {

	private final static Logger logger = LoggerFactory.getLogger(SIPRequestProcessorParent.class);

	@Autowired
	private SIPSender sipSender;

	public AddressFactory getAddressFactory() {
		try {
			return SipFactory.getInstance().createAddressFactory();
		} catch (PeerUnavailableException e) {
			logger.error(LogTemplate.ERROR_LOG_TEMPLATE, "处理接收IPCamera发来的SIP协议请求消息", "getAddressFactory触发异常", e);
		}
		return null;
	}

	public HeaderFactory getHeaderFactory() {
		try {
			return SipFactory.getInstance().createHeaderFactory();
		} catch (PeerUnavailableException e) {
			logger.error(LogTemplate.ERROR_LOG_TEMPLATE, "处理接收IPCamera发来的SIP协议请求消息", "getHeaderFactory触发异常", e);
		}
		return null;
	}

	public MessageFactory getMessageFactory() {
		try {
			return SipFactory.getInstance().createMessageFactory();
		} catch (PeerUnavailableException e) {
			logger.error(LogTemplate.ERROR_LOG_TEMPLATE, "处理接收IPCamera发来的SIP协议请求消息", "getMessageFactory触发异常", e);
		}
		return null;
	}

	class ResponseAckExtraParam{
		String content;
		ContentTypeHeader contentTypeHeader;
		SipURI sipURI;
		int expires = -1;
	}

	/***
	 * 回复状态码
	 * 100 trying
	 * 200 OK
	 * 400
	 * 404
	 */
	public SIPResponse responseAck(SIPRequest sipRequest, int statusCode) throws SipException, InvalidArgumentException, ParseException {
		return responseAck(sipRequest, statusCode, null);
	}

	public SIPResponse responseAck(SIPRequest sipRequest, int statusCode, String msg) throws SipException, InvalidArgumentException, ParseException {
		return responseAck(sipRequest, statusCode, msg, null);
	}


	public SIPResponse responseAck(SIPRequest sipRequest, int statusCode, String msg, ResponseAckExtraParam responseAckExtraParam) throws SipException, InvalidArgumentException, ParseException {
		if (sipRequest.getToHeader().getTag() == null) {
			sipRequest.getToHeader().setTag(SipUtils.getNewTag());
		}
		SIPResponse response = (SIPResponse)getMessageFactory().createResponse(statusCode, sipRequest);
		response.setStatusCode(statusCode);
		if (msg != null) {
			response.setReasonPhrase(msg);
		}

		if (responseAckExtraParam != null) {
			if (responseAckExtraParam.sipURI != null && sipRequest.getMethod().equals(Request.INVITE)) {
				logger.info(LogTemplate.PROCESS_LOG_TEMPLATE, "处理接收IPCamera发来的SIP协议请求消息", "responseSdpAck SipURI: " + responseAckExtraParam.sipURI.getHost() + ":" + responseAckExtraParam.sipURI.getPort());
				Address concatAddress = SipFactory.getInstance().createAddressFactory().createAddress(
						SipFactory.getInstance().createAddressFactory().createSipURI(responseAckExtraParam.sipURI.getUser(),  responseAckExtraParam.sipURI.getHost()+":"+responseAckExtraParam.sipURI.getPort()
						));
				response.addHeader(SipFactory.getInstance().createHeaderFactory().createContactHeader(concatAddress));
			}
			if (responseAckExtraParam.contentTypeHeader != null) {
				response.setContent(responseAckExtraParam.content, responseAckExtraParam.contentTypeHeader);
			}

			if (sipRequest.getMethod().equals(Request.SUBSCRIBE)) {
				if (responseAckExtraParam.expires == -1) {
					logger.error(LogTemplate.ERROR_LOG_TEMPLATE, "处理接收IPCamera发来的SIP协议请求消息", "参数不全", "2xx的SUBSCRIBE回复，必须设置Expires header");
				}else {
					ExpiresHeader expiresHeader = SipFactory.getInstance().createHeaderFactory().createExpiresHeader(responseAckExtraParam.expires);
					response.addHeader(expiresHeader);
				}
			}
		}else {
			if (sipRequest.getMethod().equals(Request.SUBSCRIBE)) {
				logger.error(LogTemplate.ERROR_LOG_TEMPLATE, "处理接收IPCamera发来的SIP协议请求消息", "参数不全", "2xx的SUBSCRIBE回复，必须设置Expires header");
			}
		}

		// 发送response
		sipSender.transmitRequest(response);

		return response;
	}


	public Element getRootElement(RequestEvent evt) throws DocumentException {
		return getRootElement(evt, "gb2312");
	}
	public Element getRootElement(RequestEvent evt, String charset) throws DocumentException {
		if (charset == null) {
			charset = "gb2312";
		}
		Request request = evt.getRequest();
		SAXReader reader = new SAXReader();
		reader.setEncoding(charset);
		// 对海康出现的未转义字符做处理。
		String[] destStrArray = new String[]{"&lt;","&gt;","&amp;","&apos;","&quot;"};
		char despChar = '&'; // 或许可扩展兼容其他字符
		byte destBye = (byte) despChar;
		List<Byte> result = new ArrayList<>();
		byte[] rawContent = request.getRawContent();
		if (rawContent == null) {
			return null;
		}
		for (int i = 0; i < rawContent.length; i++) {
			if (rawContent[i] == destBye) {
				boolean resul = false;
				for (String destStr : destStrArray) {
					if (i + destStr.length() <= rawContent.length) {
						byte[] bytes = Arrays.copyOfRange(rawContent, i, i + destStr.length());
						resul = resul || (Arrays.equals(bytes,destStr.getBytes()));
					}
				}
				if (resul) {
					result.add(rawContent[i]);
				}
			}else {
				result.add(rawContent[i]);
			}
		}
		Byte[] bytes = new Byte[0];
		byte[] bytesResult = ArrayUtils.toPrimitive(result.toArray(bytes));
		Document xml = new DOMDocument();
		try {
			 xml = reader.read(new ByteArrayInputStream(bytesResult));

		}catch (DocumentException e){
			logger.error(LogTemplate.ERROR_LOG_TEMPLATE, "处理接收IPCamera发来的SIP协议请求消息", "通道数据编码解析失败", "charset=" + charset);
			String newCharset = "UTF-8";
			if(charset.equals(newCharset)){
				newCharset = "gb2312";
			}
			reader.setEncoding(newCharset);
			xml = reader.read(new ByteArrayInputStream(bytesResult));

		}
		return xml.getRootElement();
	}

}
