package com.runjian.gb28181.transmit.cmd.impl;

import com.runjian.common.commonDto.SsrcInfo;
import com.runjian.common.constant.LogTemplate;
import com.runjian.conf.SipConfig;
import com.runjian.gb28181.bean.Device;
import com.runjian.gb28181.bean.DeviceAlarm;
import com.runjian.gb28181.bean.SsrcTransaction;
import com.runjian.gb28181.event.SipSubscribe;
import com.runjian.gb28181.transmit.SIPSender;
import com.runjian.gb28181.transmit.cmd.ISIPCommander;
import com.runjian.gb28181.transmit.cmd.SIPRequestHeaderProvider;
import com.runjian.gb28181.utils.SipUtils;
import com.runjian.utils.DateUtil;
import gov.nist.javax.sip.message.SIPRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.sip.InvalidArgumentException;
import javax.sip.SipException;
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
        String cmdStr = frontEndCmdString(cmdCode, parameter1, parameter2, combineCode2);
        StringBuffer ptzXml = new StringBuffer(200);
        String charset = device.getCharset();
        ptzXml.append("<?xml version=\"1.0\" encoding=\"" + charset + "\"?>\r\n");
        ptzXml.append("<Control>\r\n");
        ptzXml.append("<CmdType>DeviceControl</CmdType>\r\n");
        ptzXml.append("<SN>" + (int) ((Math.random() * 9 + 1) * 100000) + "</SN>\r\n");
        ptzXml.append("<DeviceID>" + channelId + "</DeviceID>\r\n");
        ptzXml.append("<PTZCmd>" + cmdStr + "</PTZCmd>\r\n");
        ptzXml.append("<Info>\r\n");
        ptzXml.append("<ControlPriority>5</ControlPriority>\r\n");
        ptzXml.append("</Info>\r\n");
        ptzXml.append("</Control>\r\n");




        SIPRequest request = (SIPRequest) headerProvider.createMessageRequest(device, ptzXml.toString(), SipUtils.getNewViaTag(), SipUtils.getNewFromTag(), null,sipSender.getNewCallIdHeader(device.getTransport()));
        sipSender.transmitRequest(request);
    }

    /**
     * 云台指令码计算
     *
     * @param cmdCode      指令码
     * @param parameter1   数据1
     * @param parameter2   数据2
     * @param combineCode2 组合码2
     */
    public static String frontEndCmdString(int cmdCode, int parameter1, int parameter2, int combineCode2) {
        StringBuilder builder = new StringBuilder("A50F01");
        String strTmp;
        strTmp = String.format("%02X", cmdCode);
        builder.append(strTmp, 0, 2);
        strTmp = String.format("%02X", parameter1);
        builder.append(strTmp, 0, 2);
        strTmp = String.format("%02X", parameter2);
        builder.append(strTmp, 0, 2);
        strTmp = String.format("%X", combineCode2);
        builder.append(strTmp, 0, 1).append("0");
        //计算校验码
        int checkCode = (0XA5 + 0X0F + 0X01 + cmdCode + parameter1 + parameter2 + (combineCode2 & 0XF0)) % 0X100;
        strTmp = String.format("%02X", checkCode);
        builder.append(strTmp, 0, 2);
        return builder.toString();
    }

    @Override
    public void fronEndCmd(Device device, String channelId, String cmdString, SipSubscribe.Event errorEvent, SipSubscribe.Event okEvent) throws InvalidArgumentException, SipException, ParseException {

    }


    @Override
    public void streamByeCmd(SsrcTransaction ssrcTransaction,Device device,String channelId,SipSubscribe.Event errorEvent,SipSubscribe.Event okEvent) throws InvalidArgumentException, ParseException, SipException {
        Request byteRequest = headerProvider.createByteRequest(device, channelId, ssrcTransaction.getSipTransactionInfo());
        sipSender.transmitRequest( byteRequest, errorEvent, okEvent);
    }

    @Override
    public void playPauseCmd(Device device, SsrcTransaction streamSessionSsrcTransaction) throws InvalidArgumentException, ParseException, SipException {
        StringBuffer content = new StringBuffer(200);
        content.append("PAUSE RTSP/1.0\r\n");
        content.append("CSeq: " + getInfoCseq() + "\r\n");
        content.append("PauseTime: now\r\n");

        playbackControlCmd(device, streamSessionSsrcTransaction, content.toString(), null, null);
    }

    @Override
    public void playResumeCmd(Device device, SsrcTransaction streamSessionSsrcTransaction) throws InvalidArgumentException, ParseException, SipException {
        StringBuffer content = new StringBuffer(200);
        content.append("PLAY RTSP/1.0\r\n");
        content.append("CSeq: " + getInfoCseq() + "\r\n");
        content.append("Range: npt=now-\r\n");

        playbackControlCmd(device, streamSessionSsrcTransaction, content.toString(), null, null);
    }

    @Override
    public void playSeekCmd(Device device, SsrcTransaction streamSessionSsrcTransaction,long seekTime) throws InvalidArgumentException, ParseException, SipException {
        StringBuffer content = new StringBuffer(200);
        content.append("PLAY RTSP/1.0\r\n");
        content.append("CSeq: " + getInfoCseq() + "\r\n");
        content.append("Range: npt=" + Math.abs(seekTime) + "-\r\n");

        playbackControlCmd(device, streamSessionSsrcTransaction, content.toString(), null, null);
    }

    @Override
    public void playSpeedCmd(Device device, SsrcTransaction streamSessionSsrcTransaction, Double speed) throws InvalidArgumentException, ParseException, SipException {
        StringBuffer content = new StringBuffer(200);
        content.append("PLAY RTSP/1.0\r\n");
        content.append("CSeq: " + getInfoCseq() + "\r\n");
        content.append("Scale: " + String.format("%.6f", speed) + "\r\n");

        playbackControlCmd(device, streamSessionSsrcTransaction, content.toString(), null, null);
    }

    private int getInfoCseq() {
        return (int) ((Math.random() * 9 + 1) * Math.pow(10, 8));
    }

    @Override
    public void playbackControlCmd(Device device, SsrcTransaction streamSessionSsrcTransaction, String content, SipSubscribe.Event errorEvent, SipSubscribe.Event okEvent) throws SipException, InvalidArgumentException, ParseException {

        SIPRequest request = headerProvider.createInfoRequest(device, streamSessionSsrcTransaction.getChannelId(), content.toString(), streamSessionSsrcTransaction.getSipTransactionInfo());
        if (request == null) {
            log.info("[回放控制]构建Request信息失败，设备：{}, 流ID: {}", device.getDeviceId(), streamSessionSsrcTransaction.getStream());
            return;
        }

        sipSender.transmitRequest( request, errorEvent, okEvent);
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
    public void recordInfoQuery(Device device, String channelId, String startTime, String endTime, int sn, Integer secrecy, String type, SipSubscribe.Event okEvent, SipSubscribe.Event errorEvent) throws InvalidArgumentException, SipException, ParseException {
        if (secrecy == null) {
            secrecy = 0;
        }
        if (type == null) {
            type = "all";
        }

        StringBuffer recordInfoXml = new StringBuffer(200);
        String charset = device.getCharset();
        recordInfoXml.append("<?xml version=\"1.0\" encoding=\"" + charset + "\"?>\r\n");
        recordInfoXml.append("<Query>\r\n");
        recordInfoXml.append("<CmdType>RecordInfo</CmdType>\r\n");
        recordInfoXml.append("<SN>" + sn + "</SN>\r\n");
        recordInfoXml.append("<DeviceID>" + channelId + "</DeviceID>\r\n");
        if (startTime != null) {
            recordInfoXml.append("<StartTime>" + DateUtil.yyyy_MM_dd_HH_mm_ssToISO8601(startTime) + "</StartTime>\r\n");
        }
        if (endTime != null) {
            recordInfoXml.append("<EndTime>" + DateUtil.yyyy_MM_dd_HH_mm_ssToISO8601(endTime) + "</EndTime>\r\n");
        }
        if (secrecy != null) {
            recordInfoXml.append("<Secrecy> " + secrecy + " </Secrecy>\r\n");
        }
        if (type != null) {
            // 大华NVR要求必须增加一个值为all的文本元素节点Type
            recordInfoXml.append("<Type>" + type + "</Type>\r\n");
        }
        recordInfoXml.append("</Query>\r\n");



        Request request = headerProvider.createMessageRequest(device, recordInfoXml.toString(),
                SipUtils.getNewViaTag(), SipUtils.getNewFromTag(), null,sipSender.getNewCallIdHeader(device.getTransport()));

        sipSender.transmitRequest( request, errorEvent, okEvent);
    }

    @Override
    public void alarmInfoQuery(Device device, String startPriority, String endPriority, String alarmMethod, String alarmType, String startTime, String endTime, SipSubscribe.Event errorEvent) throws InvalidArgumentException, SipException, ParseException {

    }

    @Override
    public void deviceConfigQuery(Device device, String channelId, String configType, SipSubscribe.Event errorEvent) throws InvalidArgumentException, SipException, ParseException {

    }

    @Override
    public void presetQuery(Device device, String channelId, SipSubscribe.Event errorEvent) throws InvalidArgumentException, SipException, ParseException {
        StringBuffer cmdXml = new StringBuffer(200);
        String charset = device.getCharset();
        cmdXml.append("<?xml version=\"1.0\" encoding=\"" + charset + "\"?>\r\n");
        cmdXml.append("<Query>\r\n");
        cmdXml.append("<CmdType>PresetQuery</CmdType>\r\n");
        cmdXml.append("<SN>" + (int) ((Math.random() * 9 + 1) * 100000) + "</SN>\r\n");
        cmdXml.append("<DeviceID>" + channelId + "</DeviceID>\r\n");
        cmdXml.append("</Query>\r\n");



        Request request = headerProvider.createMessageRequest(device, cmdXml.toString(), null, SipUtils.getNewFromTag(), null,sipSender.getNewCallIdHeader(device.getTransport()));
        sipSender.transmitRequest( request, errorEvent);
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
        StringBuffer dragXml = new StringBuffer(200);
        String charset = device.getCharset();
        dragXml.append("<?xml version=\"1.0\" encoding=\"" + charset + "\"?>\r\n");
        dragXml.append("<Control>\r\n");
        dragXml.append("<CmdType>DeviceControl</CmdType>\r\n");
        dragXml.append("<SN>" + (int) ((Math.random() * 9 + 1) * 100000) + "</SN>\r\n");
        dragXml.append("<DeviceID>" + channelId + "</DeviceID>\r\n");
        dragXml.append(cmdString);
        dragXml.append("</Control>\r\n");

        Request request = headerProvider.createMessageRequest(device, dragXml.toString(), SipUtils.getNewViaTag(), SipUtils.getNewFromTag(), null,sipSender.getNewCallIdHeader(device.getTransport()));
        log.info(LogTemplate.PROCESS_LOG_TEMPLATE, "设备能力接口", "拉框信令：" + request.toString());
        sipSender.transmitRequest(request);
    }

    @Override
    public void sendAlarmMessage(Device device, DeviceAlarm deviceAlarm) throws InvalidArgumentException, SipException, ParseException {

    }

    @Override
    public void playStreamCmd(Integer streamMode, SsrcInfo ssrcInfo, Device device, String channelId, SipSubscribe.Event okEvent, SipSubscribe.Event errorEvent) throws InvalidArgumentException, SipException, ParseException {
        StringBuffer content = new StringBuffer(200);
        content.append("v=0\r\n");
        content.append("o=" + channelId + " 0 0 IN IP4 " + ssrcInfo.getSdpIp() + "\r\n");
        content.append("s=Play\r\n");
        content.append("c=IN IP4 " + ssrcInfo.getSdpIp() + "\r\n");
        content.append("t=0 0\r\n");

        if (streamMode == 1) {
            content.append("m=video " + ssrcInfo.getPort() + " TCP/RTP/AVP 96 97 98 99\r\n");
            content.append("a=setup:passive\r\n");
            content.append("a=connection:new\r\n");
        }  else  {
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

    @Override
    public void playbackStreamCmd(Integer streamMode, SsrcInfo ssrcInfo, Device device, String channelId, String startTime, String endTime, SipSubscribe.Event okEvent, SipSubscribe.Event errorEvent) throws InvalidArgumentException, SipException, ParseException {
        StringBuffer content = new StringBuffer(200);
        content.append("v=0\r\n");
        content.append("o=" + channelId + " 0 0 IN IP4 " + ssrcInfo.getSdpIp() + "\r\n");
        content.append("s=Playback\r\n");
        content.append("u=" + channelId + ":0\r\n");
        content.append("c=IN IP4 " + ssrcInfo.getSdpIp() + "\r\n");
        content.append("t=" + DateUtil.yyyy_MM_dd_HH_mm_ssToTimestamp(startTime) + " "
                + DateUtil.yyyy_MM_dd_HH_mm_ssToTimestamp(endTime) + "\r\n");

        if (streamMode == 1) {
            content.append("m=video " + ssrcInfo.getPort() + " TCP/RTP/AVP 96 97 98 99\r\n");
            content.append("a=setup:passive\r\n");
            content.append("a=connection:new\r\n");
        }  else  {
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
        log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "设备能力接口", "请求回放视频流", request.toString());
        //成功与否，均有业务侧进行处理
        sipSender.transmitRequest( request, (e -> {
            errorEvent.response(e);
        }), ok -> {
            // 这里为例避免一个通道的点播只有一个callID这个参数使用一个固定值
            okEvent.response(ok);
        });
    }
}
