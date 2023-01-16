package com.runjian.service;

import com.runjian.domain.req.PlayReq;

public interface IplayService {

    /**
     * 点播接口处理
     * @param playReq
     */
    public void play(PlayReq playReq);

    /**
     * 流注册/注销处理
     */
    public void onStreamChanges();

    public void onStreamNoneReader();
}
