package com.runjian.media.dispatcher.zlm.service;


import com.runjian.media.dispatcher.zlm.dto.MediaServerItem;

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

    MediaServerItem getNewMediaServerItem();

}
