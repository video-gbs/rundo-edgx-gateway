package com.runjian.gb28181.transmit.event.request.impl;

import com.runjian.common.constant.LogTemplate;
import com.runjian.gb28181.bean.Device;
import com.runjian.gb28181.transmit.SIPProcessorObserver;
import com.runjian.gb28181.transmit.event.request.ISIPRequestProcessor;
import com.runjian.gb28181.transmit.event.request.SIPRequestProcessorParent;
import com.runjian.gb28181.utils.SipUtils;
import com.runjian.service.IDeviceService;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.message.SIPResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sdp.*;
import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.SipException;
import javax.sip.header.CallIdHeader;
import javax.sip.message.Response;
import java.text.ParseException;
import java.util.Vector;

/**
 * SIP命令类型： INVITE请求
 */
@SuppressWarnings("rawtypes")
@Component
public class InviteRequestProcessor extends SIPRequestProcessorParent implements InitializingBean, ISIPRequestProcessor {

    private final static Logger logger = LoggerFactory.getLogger(InviteRequestProcessor.class);

    private final String method = "INVITE";

    @Autowired
    private SIPProcessorObserver sipProcessorObserver;

    @Autowired
    private IDeviceService deviceService;

    @Override
    public void afterPropertiesSet() throws Exception {
        // 添加消息处理的订阅
        sipProcessorObserver.addRequestProcessor(method, this);
    }

    /**
     * 处理invite请求
     *
     * @param evt 请求消息
     */
    @Override
    public void process(RequestEvent evt) {
        //  Invite Request消息实现，此消息一般为级联消息，上级给下级发送请求视频指令

        try {
            SIPRequest request = (SIPRequest)evt.getRequest();
            logger.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "SIP命令INVITE请求处理", "收到请求信息", request.toString());

            String channelId = SipUtils.getChannelIdFromRequest(request);
            String requesterId = SipUtils.getUserIdFromFromHeader(request);
            CallIdHeader callIdHeader = (CallIdHeader) request.getHeader(CallIdHeader.NAME);
            if (requesterId == null || channelId == null) {
                logger.info(LogTemplate.PROCESS_LOG_TEMPLATE, "SIP命令INVITE请求处理", "无法从FromHeader的Address中获取到平台id，返回400");
                // 参数不全， 发400，请求错误
                try {
                    responseAck(request, Response.BAD_REQUEST);
                } catch (SipException | InvalidArgumentException | ParseException e) {
                    logger.error(LogTemplate.ERROR_LOG_TEMPLATE, "SIP命令INVITE请求处理", "命令发送失败，invite BAD_REQUEST" , e);
                }
                return;
            }
            // todo 预留级联相关点播请求
            inviteFromDeviceHandle(request, requesterId);

        } catch (Exception e) {
            logger.error(LogTemplate.ERROR_LOG_TEMPLATE, "SIP命令INVITE请求处理", "process方法触发SdpException异常", e);
        }
    }

    /**
     * 安排推流
     *
     * @return
     */

    public SIPResponse inviteFromDeviceHandle(SIPRequest request, String requesterId) {
        // 非上级平台请求，查询是否设备请求（通常为接收语音广播的设备）
        Device device = deviceService.getDevice(requesterId);
        if (device != null) {
            logger.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "SIP命令INVITE请求处理", "收到设备的语音广播Invite请求", "设备：" + requesterId);
            try {
                responseAck(request, Response.TRYING);
            } catch (SipException | InvalidArgumentException | ParseException e) {
                logger.error(LogTemplate.ERROR_LOG_TEMPLATE, "SIP命令INVITE请求处理", "命令发送失败, invite BAD_REQUEST", e);
            }
            String contentString = new String(request.getRawContent());
            // jainSip不支持y=字段， 移除移除以解析。
            String substring = contentString;
            String ssrc = "0000000404";
            int ssrcIndex = contentString.indexOf("y=");
            if (ssrcIndex > 0) {
                substring = contentString.substring(0, ssrcIndex);
                ssrc = contentString.substring(ssrcIndex + 2, ssrcIndex + 12);
            }
            ssrcIndex = substring.indexOf("f=");
            if (ssrcIndex > 0) {
                substring = contentString.substring(0, ssrcIndex);
            }
            SessionDescription sdp = null;
            try {
                sdp = SdpFactory.getInstance().createSessionDescription(substring);
                //  获取支持的格式
                Vector mediaDescriptions = sdp.getMediaDescriptions(true);
                // 查看是否支持PS 负载96
                int port = -1;
                //协议类型0 为udp
                int protocalKind = 0;
                for (int i = 0; i < mediaDescriptions.size(); i++) {
                    MediaDescription mediaDescription = (MediaDescription) mediaDescriptions.get(i);
                    Media media = mediaDescription.getMedia();

                    Vector mediaFormats = media.getMediaFormats(false);
                    if (mediaFormats.contains("8")) {
                        port = media.getMediaPort();
                        String protocol = media.getProtocol();
                        // 区分TCP发流还是udp， 当前默认udp
                        if ("TCP/RTP/AVP".equals(protocol)) {
                            String setup = mediaDescription.getAttribute("setup");
                            if (setup != null) {
                                if ("active".equals(setup)) {
                                    protocalKind = 1;
                                } else if ("passive".equals(setup)) {
                                    protocalKind = 2;
                                }
                            }
                        }else {

                        }
                        break;
                    }
                }
                if (port == -1) {
                    logger.info(LogTemplate.PROCESS_LOG_TEMPLATE, "SIP命令INVITE请求处理", "不支持的媒体格式，返回415");
                    // 回复不支持的格式
                    try {
                        responseAck(request, Response.UNSUPPORTED_MEDIA_TYPE); // 不支持的格式，发415
                    } catch (SipException | InvalidArgumentException | ParseException e) {
                        logger.error(LogTemplate.ERROR_LOG_TEMPLATE, "SIP命令INVITE请求处理", "命令发送失败, invite 不支持的媒体格式，返回415", e);
                    }
                    return null;
                }
                String username = sdp.getOrigin().getUsername();
                String addressStr = sdp.getOrigin().getAddress();
                logger.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "SIP命令INVITE请求处理", "设备请求语音流", "设备:" + username + " 地址：" + addressStr + " ssrc：" + ssrc);

                StringBuffer content = new StringBuffer(200);
                content.append("v=0\r\n");
                content.append("o=34020000001320000211 0 0 IN IP4 172.20.0.104 \r\n");
                content.append("s=Play\r\n");
                content.append("c=IN IP4 172.20.0.104\r\n");
                content.append("t=0 0\r\n");
                // 非严格模式端口不统一, 增加兼容性，修改为一个不为0的端口


                if (protocalKind == 0) {
                    content.append("m=audio 30000 RTP/AVP 8\r\n");
                }else {
                    content.append("m=audio 30000 TCP/RTP/AVP 8\r\n");
                    if(protocalKind == 1){
                        content.append("a=setup:passive\r\n");
                    }else {
                        content.append("a=setup:active\r\n");
                    }
                }
                content.append("a=sendonly\r\n");
                content.append("a=rtpmap:8 PCMA/8000\r\n");
                content.append("a=connection:new\r\n");
                content.append("y=" + ssrc + "\r\n");
                content.append("f=\r\n");

                try {
                    return responseSdpAck(request, content.toString());
                } catch (SipException e) {
                    logger.error("未处理的异常 ", e);
                } catch (InvalidArgumentException e) {
                    logger.error("未处理的异常 ", e);
                } catch (ParseException e) {
                    logger.error("未处理的异常 ", e);
                }

            } catch (SdpException e) {
                logger.error(LogTemplate.ERROR_LOG_TEMPLATE, "SIP命令INVITE请求处理", "SDP解析异常", e);
            }



        } else {
            logger.info(LogTemplate.PROCESS_LOG_TEMPLATE, "SIP命令INVITE请求处理", "来自无效设备/平台的请求");
            try {
                responseAck(request, Response.BAD_REQUEST);; // 不支持的格式，发415
            } catch (SipException | InvalidArgumentException | ParseException e) {
                logger.error(LogTemplate.ERROR_LOG_TEMPLATE, "SIP命令INVITE请求处理", "命令发送失败,invite 来自无效设备/平台的请求", e);
            }
        }
        return null;
    }
}
