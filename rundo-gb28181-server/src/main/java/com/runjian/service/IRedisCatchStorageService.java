package com.runjian.service;

import com.runjian.common.mq.domain.GatewayMqDto;

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

    GatewayMqDto getMqInfo(String msgType,String snIncr,String snPrefix);

}
