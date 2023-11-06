package com.runjian.gb28181.transmit.event.request.impl.message.notify.cmd;

import com.alibaba.fastjson.JSONObject;
import com.runjian.common.constant.LogTemplate;
import com.runjian.gb28181.bean.*;
import com.runjian.gb28181.session.DeviceAlarmCatch;
import com.runjian.gb28181.transmit.event.request.SIPRequestProcessorParent;
import com.runjian.gb28181.transmit.event.request.impl.message.IMessageHandler;
import com.runjian.gb28181.transmit.event.request.impl.message.notify.NotifyMessageHandler;
import com.runjian.gb28181.utils.NumericUtil;
import com.runjian.gb28181.utils.XmlUtil;
import com.runjian.utils.DateUtil;
import gov.nist.javax.sip.message.SIPRequest;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.SipException;
import javax.sip.message.Response;
import java.text.ParseException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;

import static com.runjian.gb28181.utils.XmlUtil.getText;


/**
 * 报警事件的处理，参考：9.4
 * @author chenjialing
 */
@Slf4j
@Component
public class AlarmNotifyMessageHandler extends SIPRequestProcessorParent implements InitializingBean, IMessageHandler {

    private final Logger logger = LoggerFactory.getLogger(AlarmNotifyMessageHandler.class);
    private final String cmdType = "Alarm";

    @Qualifier("taskExecutor")
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;


    @Autowired
    private NotifyMessageHandler notifyMessageHandler;

    private ConcurrentLinkedQueue<SipMsgInfo> taskQueue = new ConcurrentLinkedQueue<>();

    @Autowired
    private DeviceAlarmCatch deviceAlarmCatch;
    @Override
    public void afterPropertiesSet() throws Exception {
        notifyMessageHandler.addHandler(cmdType, this);
    }

    @Override
    public void handForDevice(RequestEvent evt, Device device, Element rootElement) {
//        logger.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "报警事件的处理", "收到报警通知", "设备id:" + device.getDeviceId());
        taskQueue.offer(new SipMsgInfo(evt, device, rootElement));
        // 回复200 OK
        try {
            responseAck((SIPRequest) evt.getRequest(), Response.OK);
        } catch (SipException | InvalidArgumentException | ParseException e) {
            logger.error(LogTemplate.ERROR_LOG_TEMPLATE, "报警事件的处理", "处理报警通知,回复200OK失败", e);
        }
        while (!taskQueue.isEmpty()) {
            taskExecutor.execute(() -> {
                try {
                    SipMsgInfo sipMsgInfo = taskQueue.poll();
                    if (ObjectUtils.isEmpty(sipMsgInfo)) {
                        return;
                    }
                    Element deviceIdElement = sipMsgInfo.getRootElement().element("SN");
                    String snCode = deviceIdElement.getText();
                    String channelId = getText(sipMsgInfo.getRootElement(), "DeviceID");
                    DeviceAlarm deviceAlarm = new DeviceAlarm();
                    deviceAlarm.setDeviceId(sipMsgInfo.getDevice().getDeviceId());
                    deviceAlarm.setChannelId(channelId);
                    deviceAlarm.setAlarmPriority(getText(sipMsgInfo.getRootElement(), "AlarmPriority"));
                    deviceAlarm.setAlarmMethod(getText(sipMsgInfo.getRootElement(), "AlarmMethod"));
                    String alarmTime = getText(sipMsgInfo.getRootElement(), "AlarmTime");
                    if (alarmTime == null) {
                        return;
                    }
                    deviceAlarm.setAlarmTime(DateUtil.ISO8601Toyyyy_MM_dd_HH_mm_ss(alarmTime));
                    String alarmDescription = getText(sipMsgInfo.getRootElement(), "AlarmDescription");
                    if (alarmDescription == null) {
                        deviceAlarm.setAlarmDescription("");
                    } else {
                        deviceAlarm.setAlarmDescription(alarmDescription);
                    }
                    String longitude = getText(sipMsgInfo.getRootElement(), "Longitude");
                    if (longitude != null && NumericUtil.isDouble(longitude)) {
                        deviceAlarm.setLongitude(Double.parseDouble(longitude));
                    } else {
                        deviceAlarm.setLongitude(0.00);
                    }
                    String latitude = getText(sipMsgInfo.getRootElement(), "Latitude");
                    if (latitude != null && NumericUtil.isDouble(latitude)) {
                        deviceAlarm.setLatitude(Double.parseDouble(latitude));
                    } else {
                        deviceAlarm.setLatitude(0.00);
                    }
                    Element infoElem = sipMsgInfo.getRootElement().element("Info");
                    if(!ObjectUtils.isEmpty(infoElem)){
                        deviceAlarm.setAlarmType(getText(infoElem, "AlarmType"));
                    }
                    deviceAlarmCatch.addReady(deviceAlarm);

                } catch (Exception e) {
                    logger.error(LogTemplate.ERROR_LOG_TEMPLATE, "报警事件的处理失败", "收到报警通知", e);
                }


            });
        }


    }

    @Override
    public void handForPlatform(RequestEvent evt, ParentPlatform parentPlatform, Element rootElement) {
        logger.info(LogTemplate.PROCESS_LOG_TEMPLATE, "报警事件的处理", String.format("收到来自平台[%s]的报警通知", parentPlatform.getServerGBId()));
        // 回复200 OK

    }
}
