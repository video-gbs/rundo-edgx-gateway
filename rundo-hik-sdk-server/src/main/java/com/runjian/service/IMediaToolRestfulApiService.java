package com.runjian.service;

import com.runjian.common.config.response.CommonResponse;
import com.runjian.entity.PlayBackControlToolEntity;
import com.runjian.entity.PlayBackToolEntity;
import com.runjian.entity.PlayToolEntity;

/**
 * @author chenjialing
 */
public interface IMediaToolRestfulApiService {

    /**
     * 直播流处理
     * @return
     */
    CommonResponse<Integer> liveStreamDeal(PlayToolEntity playToolEntity);


    /**
     * 录播流处理
     * @return
     */
    CommonResponse<Integer> backStreamDeal(PlayBackToolEntity playToolEntity);


    /**
     * 录播流处理
     * @return
     */
    CommonResponse<Integer> backStreamControlDeal(PlayBackControlToolEntity playBackControlToolEntity);
    /**
     * 直播流处理
     * @return
     */
    CommonResponse<Boolean> streamToolBye(Integer playHandle);

}
