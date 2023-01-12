package com.runjian.service;

import com.runjian.common.mq.domain.GatewayMqDto;
import com.runjian.conf.SsrcConfig;

/**
 * @author chenjialing
 */
public interface IRedisCatchStorageService {

    /**
     * 计数器。为cseq进行计数
     *
     * @return
     */
    Long getCSEQ();


    /**
     * 计数器,sn码
     *
     * @return
     */
    String getSn(String key);

    GatewayMqDto getMqInfo(String msgType,String snIncr,String snPrefix,String msgId);

    Boolean ssrcInit();

    Boolean ssrcRelease(String ssrc);

    SsrcConfig getSsrcConfig();

}
