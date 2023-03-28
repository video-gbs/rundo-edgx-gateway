package com.runjian.mq.MqMsgDealService;

import com.runjian.common.constant.LogTemplate;
import com.runjian.common.mq.domain.CommonMqDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public  class IMqMsgDealServer {

    private static final Map<String,  IMsgProcessorService> msgProcessorServiceConcurrentHashMap = new ConcurrentHashMap<>();

    public void addRequestProcessor(String method, IMsgProcessorService processor) {
        msgProcessorServiceConcurrentHashMap.put(method, processor);
    }

    /**
     * 消息执行
     * @param commonMqDto
     */
    public void msgProcess(CommonMqDto commonMqDto){
        String msgType = commonMqDto.getMsgType();
        IMsgProcessorService iMsgProcessorService = msgProcessorServiceConcurrentHashMap.get(msgType);
        if(iMsgProcessorService != null){
            msgProcessorServiceConcurrentHashMap.get(msgType).process(commonMqDto);
        }else {
            log.warn(LogTemplate.PROCESS_LOG_MSG_TEMPLATE,"mq消息分发","该消息类型不存在",commonMqDto);
        }

    }
}
