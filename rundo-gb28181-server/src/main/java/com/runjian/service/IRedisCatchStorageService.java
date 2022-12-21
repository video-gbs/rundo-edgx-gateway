package com.runjian.service;

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

}
