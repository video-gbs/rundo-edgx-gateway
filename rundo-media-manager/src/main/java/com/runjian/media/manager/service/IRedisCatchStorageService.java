package com.runjian.media.manager.service;

import com.runjian.common.commonDto.Gateway.dto.SsrcConfig;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.StreamBusinessSceneResp;
import com.runjian.common.constant.StreamBusinessMsgType;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.common.utils.CircleArray;

import java.util.List;

/**
 * @author chenjialing
 */
public interface IRedisCatchStorageService {

    /**
     * 过期时钟
     */
    CircleArray<String> msgIdArray = new CircleArray<>(20);


    /**
     * 心跳
     */
    void msgExpireRoutine();
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

    CommonMqDto getMqInfo(String msgType, String snIncr, String snPrefix, String msgId);

    /**
     * 初始化国标ssrc
     * @return
     */
    Boolean ssrcInit();

    Boolean ssrcRelease(String ssrc);

    SsrcConfig getSsrcConfig();

    Boolean setSsrcConfig(SsrcConfig ssrcConfig);
    /**
     * 操作业务场景的redis修改
     * @param businessSceneKey
     * @param data
     */
    void editBusinessSceneKey(String businessSceneKey, BusinessErrorEnums businessErrorEnums, Object data);


    /**
     * redis的新增
     * @param businessSceneKey
     * @param msgType
     * @param msgId
     */
    Boolean addBusinessSceneKey(String businessSceneKey, StreamBusinessMsgType msgType, String msgId);


}
