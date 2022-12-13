package com.runjian.gb28181.event.record;

import com.runjian.common.constant.LogTemplate;
import com.runjian.gb28181.bean.RecordInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * @description: 录像查询结束时间
 * @author: pan
 * @data: 2022-02-23
 */

@Component
public class RecordEndEventListener implements ApplicationListener<RecordEndEvent> {

    private final static Logger logger = LoggerFactory.getLogger(RecordEndEventListener.class);

    private static Map<String, SseEmitter> sseEmitters = new Hashtable<>();

    public interface RecordEndEventHandler{
        void  handler(RecordInfo recordInfo);
    }

    private Map<String, RecordEndEventHandler> handlerMap = new HashMap<>();
    @Override
    public void onApplicationEvent(RecordEndEvent event) {
        logger.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "录像查询结束时间", "录像查询完成事件触发", String.format("deviceId：%s, channelId: %s, 录像数量%s条", event.getRecordInfo().getDeviceId(),
                event.getRecordInfo().getChannelId(), event.getRecordInfo().getSumNum()));
        if (handlerMap.size() > 0) {
            for (RecordEndEventHandler recordEndEventHandler : handlerMap.values()) {
                recordEndEventHandler.handler(event.getRecordInfo());
            }
        }

    }

    public void addEndEventHandler(String device, String channelId, RecordEndEventHandler recordEndEventHandler) {
        handlerMap.put(device + channelId, recordEndEventHandler);
    }
}
