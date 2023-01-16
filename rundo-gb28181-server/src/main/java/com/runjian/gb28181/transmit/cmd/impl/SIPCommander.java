package com.runjian.gb28181.transmit.cmd.impl;

import com.alibaba.fastjson.JSONObject;
import com.runjian.common.commonDto.SsrcInfo;
import com.runjian.common.constant.LogTemplate;
import com.runjian.conf.SipConfig;
import com.runjian.conf.UserSetting;
import com.runjian.conf.exception.SsrcTransactionNotFoundException;
import com.runjian.domain.dto.MediaServerItem;
import com.runjian.gb28181.bean.Device;
import com.runjian.gb28181.bean.DeviceAlarm;
import com.runjian.gb28181.event.SipSubscribe;
import com.runjian.gb28181.transmit.SIPSender;
import com.runjian.gb28181.transmit.cmd.ISIPCommander;
import com.runjian.gb28181.transmit.cmd.SIPRequestHeaderProvider;
import com.runjian.gb28181.utils.SipUtils;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.message.SIPResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.connection.stream.StreamInfo;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.sip.InvalidArgumentException;
import javax.sip.ResponseEvent;
import javax.sip.SipException;
import javax.sip.SipFactory;
import javax.sip.header.CallIdHeader;
import javax.sip.message.Request;
import java.text.ParseException;

/**
 * @description:设备能力接口，用于定义设备的控制、查询能力
 * @author: swwheihei
 * @date: 2020年5月3日 下午9:22:48
 */
@Component
@DependsOn("sipLayer")
@Slf4j
public class SIPCommander implements ISIPCommander {
    @Autowired
    private SIPRequestHeaderProvider headerProvider;

    @Autowired
    private SIPSender sipSender;

    @Autowired
    private SipConfig sipConfig;


    @Override
    public void ptzdirectCmd(Device device, String channelId, int leftRight, int upDown) throws InvalidArgumentException, ParseException, SipException {

    }

    @Override
    public void ptzdirectCmd(Device device, String channelId, int leftRight, int upDown, int moveSpeed) throws InvalidArgumentException, ParseException, SipException {

    }

    @Override
    public void ptzZoomCmd(Device device, String channelId, int inOut) throws InvalidArgumentException, ParseException, SipException {

    }

    @Override
    public void ptzZoomCmd(Device device, String channelId, int inOut, int moveSpeed) throws InvalidArgumentException, ParseException, SipException {

    }

    @Override
    public void ptzCmd(Device device, String channelId, int leftRight, int upDown, int inOut, int moveSpeed, int zoomSpeed) throws InvalidArgumentException, SipException, ParseException {

    }

    @Override
    public void frontEndCmd(Device device, String channelId, int cmdCode, int parameter1, int parameter2, int combineCode2) throws SipException, InvalidArgumentException, ParseException {

    }

    @Override
    public void fronEndCmd(Device device, String channelId, String cmdString, SipSubscribe.Event errorEvent, SipSubscribe.Event okEvent) throws InvalidArgumentException, SipException, ParseException {

    }

    @Override
    public void streamByeCmd(Device device, String channelId, String stream, String callId, SipSubscribe.Event okEvent) throws InvalidArgumentException, SipException, ParseException, SsrcTransactionNotFoundException {

    }

    @Override
    public void streamByeCmd(Device device, String channelId, String stream, String callId) throws InvalidArgumentException, ParseException, SipException, SsrcTransactionNotFoundException {

    }

    @Override
    public void playPauseCmd(Device device, StreamInfo streamInfo) throws InvalidArgumentException, ParseException, SipException {

    }

    @Override
    public void playResumeCmd(Device device, StreamInfo streamInfo) throws InvalidArgumentException, ParseException, SipException {

    }

    @Override
    public void playSeekCmd(Device device, StreamInfo streamInfo, long seekTime) throws InvalidArgumentException, ParseException, SipException {

    }

    @Override
    public void playSpeedCmd(Device device, StreamInfo streamInfo, Double speed) throws InvalidArgumentException, ParseException, SipException {

    }

    @Override
    public void playbackControlCmd(Device device, StreamInfo streamInfo, String content, SipSubscribe.Event errorEvent, SipSubscribe.Event okEvent) throws SipException, InvalidArgumentException, ParseException {

    }

    @Override
    public void audioBroadcastCmd(Device device, String channelId) {

    }

    @Override
    public void audioBroadcastCmd(Device device, SipSubscribe.Event okEvent) throws InvalidArgumentException, SipException, ParseException {

    }

    @Override
    public void audioBroadcastCmd(Device device) throws InvalidArgumentException, SipException, ParseException {

    }

    @Override
    public void recordCmd(Device device, String channelId, String recordCmdStr, SipSubscribe.Event errorEvent) throws InvalidArgumentException, SipException, ParseException {

    }

    @Override
    public void teleBootCmd(Device device) throws InvalidArgumentException, SipException, ParseException {

    }

    @Override
    public void guardCmd(Device device, String guardCmdStr, SipSubscribe.Event errorEvent) throws InvalidArgumentException, SipException, ParseException {

    }

    @Override
    public void alarmCmd(Device device, String alarmMethod, String alarmType, SipSubscribe.Event errorEvent) throws InvalidArgumentException, SipException, ParseException {

    }

    @Override
    public void iFrameCmd(Device device, String channelId) throws InvalidArgumentException, SipException, ParseException {

    }

    @Override
    public void homePositionCmd(Device device, String channelId, String enabled, String resetTime, String presetIndex, SipSubscribe.Event errorEvent) throws InvalidArgumentException, SipException, ParseException {

    }

    @Override
    public void deviceConfigCmd(Device device) {

    }

    @Override
    public void deviceBasicConfigCmd(Device device, String channelId, String name, String expiration, String heartBeatInterval, String heartBeatCount, SipSubscribe.Event errorEvent) throws InvalidArgumentException, SipException, ParseException {

    }

    @Override
    public void deviceStatusQuery(Device device, SipSubscribe.Event errorEvent) throws InvalidArgumentException, SipException, ParseException {

    }

    @Override
    public void deviceInfoQuery(Device device) throws InvalidArgumentException, SipException, ParseException {
        StringBuffer catalogXml = new StringBuffer(200);
        String charset = device.getCharset();
        catalogXml.append("<?xml version=\"1.0\" encoding=\"" + charset + "\"?>\r\n");
        catalogXml.append("<Query>\r\n");
        catalogXml.append("<CmdType>DeviceInfo</CmdType>\r\n");
        catalogXml.append("<SN>" + (int) ((Math.random() * 9 + 1) * 100000) + "</SN>\r\n");
        catalogXml.append("<DeviceID>" + device.getDeviceId() + "</DeviceID>\r\n");
        catalogXml.append("</Query>\r\n");



        Request request = headerProvider.createMessageRequest(device, catalogXml.toString(), SipUtils.getNewViaTag(), SipUtils.getNewFromTag(), null,sipSender.getNewCallIdHeader(device.getTransport()));

        sipSender.transmitRequest( request);
    }

    @Override
    public void catalogQuery(Device device, int sn, SipSubscribe.Event errorEvent) throws SipException, InvalidArgumentException, ParseException {
        StringBuffer catalogXml = new StringBuffer(200);
        String charset = device.getCharset();
        catalogXml.append("<?xml version=\"1.0\" encoding=\"" + charset + "\"?>\r\n");
        catalogXml.append("<Query>\r\n");
        catalogXml.append("  <CmdType>Catalog</CmdType>\r\n");
        catalogXml.append("  <SN>" + sn + "</SN>\r\n");
        catalogXml.append("  <DeviceID>" + device.getDeviceId() + "</DeviceID>\r\n");
        catalogXml.append("</Query>\r\n");



        Request request = headerProvider.createMessageRequest(device, catalogXml.toString(), SipUtils.getNewViaTag(), SipUtils.getNewFromTag(), null,sipSender.getNewCallIdHeader(device.getTransport()));

        sipSender.transmitRequest( request, errorEvent);
    }

    @Override
    public void recordInfoQuery(Device device, String channelId, String startTime, String endTime, int sn, Integer Secrecy, String type, SipSubscribe.Event okEvent, SipSubscribe.Event errorEvent) throws InvalidArgumentException, SipException, ParseException {

    }

    @Override
    public void alarmInfoQuery(Device device, String startPriority, String endPriority, String alarmMethod, String alarmType, String startTime, String endTime, SipSubscribe.Event errorEvent) throws InvalidArgumentException, SipException, ParseException {

    }

    @Override
    public void deviceConfigQuery(Device device, String channelId, String configType, SipSubscribe.Event errorEvent) throws InvalidArgumentException, SipException, ParseException {

    }

    @Override
    public void presetQuery(Device device, String channelId, SipSubscribe.Event errorEvent) throws InvalidArgumentException, SipException, ParseException {

    }

    @Override
    public void mobilePostitionQuery(Device device, SipSubscribe.Event errorEvent) throws InvalidArgumentException, SipException, ParseException {

    }

    @Override
    public SIPRequest mobilePositionSubscribe(Device device, SIPRequest request, SipSubscribe.Event okEvent, SipSubscribe.Event errorEvent) throws InvalidArgumentException, SipException, ParseException {
        return null;
    }

    @Override
    public void alarmSubscribe(Device device, int expires, String startPriority, String endPriority, String alarmMethod, String alarmType, String startTime, String endTime) throws InvalidArgumentException, SipException, ParseException {

    }

    @Override
    public SIPRequest catalogSubscribe(Device device, SIPRequest request, SipSubscribe.Event okEvent, SipSubscribe.Event errorEvent) throws InvalidArgumentException, SipException, ParseException {
        return null;
    }

    @Override
    public void dragZoomCmd(Device device, String channelId, String cmdString) throws InvalidArgumentException, SipException, ParseException {

    }

    @Override
    public void sendAlarmMessage(Device device, DeviceAlarm deviceAlarm) throws InvalidArgumentException, SipException, ParseException {

    }

    @Override
    public void playStreamCmd(String streamMode, SsrcInfo ssrcInfo, Device device, String channelId, SipSubscribe.Event okEvent, SipSubscribe.Event errorEvent) throws InvalidArgumentException, SipException, ParseException {
        StringBuffer content = new StringBuffer(200);
        content.append("v=0\r\n");
        content.append("o=" + channelId + " 0 0 IN IP4 " + ssrcInfo.getSdpIp() + "\r\n");
        content.append("s=Play\r\n");
        content.append("c=IN IP4 " + ssrcInfo.getSdpIp() + "\r\n");
        content.append("t=0 0\r\n");

        if ("TCP".equalsIgnoreCase(streamMode)) {
            content.append("m=video " + ssrcInfo.getPort() + " TCP/RTP/AVP 96 97 98 99\r\n");
            content.append("a=setup:passive\r\n");
            content.append("a=connection:new\r\n");
        }  else if ("UDP".equalsIgnoreCase(streamMode)) {
            content.append("m=video " + ssrcInfo.getPort() + " RTP/AVP 96 97 98 99\r\n");
        }
        content.append("a=recvonly\r\n");
        content.append("a=rtpmap:96 PS/90000\r\n");
        content.append("a=rtpmap:98 H264/90000\r\n");
        content.append("a=rtpmap:97 MPEG4/90000\r\n");
        content.append("a=rtpmap:99 H265/90000\r\n");

        content.append("y=" + ssrcInfo.getSsrc() + "\r\n");//ssrc
        // f字段:f= v/编码格式/分辨率/帧率/码率类型/码率大小a/编码格式/码率大小/采样率
//			content.append("f=v/2/5/25/1/4000a/1/8/1" + "\r\n"); // 未发现支持此特性的设备



        Request request = headerProvider.createInviteRequest(device, channelId, content.toString(), SipUtils.getNewViaTag(), SipUtils.getNewFromTag(), null, ssrcInfo.getSsrc(),sipSender.getNewCallIdHeader(device.getTransport()));
        log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "设备能力接口", "请求预览视频流", request.toString());
        //成功与否，均有业务侧进行处理
        sipSender.transmitRequest( request, (e -> {
            errorEvent.response(e);
        }), ok -> {
            // 这里为例避免一个通道的点播只有一个callID这个参数使用一个固定值
            okEvent.response(ok);
        });
    }

}
