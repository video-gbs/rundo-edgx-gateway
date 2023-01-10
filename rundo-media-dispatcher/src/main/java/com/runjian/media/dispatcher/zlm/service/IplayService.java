package com.runjian.media.dispatcher.zlm.service;


/**
 * 点播处理
 * @author chenjialing
 */
public interface IplayService {

    /**
     *
     * @param mediaServerId
     */
    void zlmServerOnline(String mediaServerId);

    void zlmServerOffline(String mediaServerId);

}
