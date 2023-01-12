package com.runjian.media.dispatcher.zlm.service;


import com.runjian.media.dispatcher.zlm.dto.MediaServerItem;

/**
 * 点播处理
 * @author chenjialing
 */
public interface IplayService {

    /**
     * todo 处理级联相关的上线业务
     * @param mediaServerId
     */
    void zlmServerOnline(String mediaServerId);

    /**
     * todo 处理级联相关的下线业务
     * @param mediaServerId
     */
    void zlmServerOffline(String mediaServerId);


}
