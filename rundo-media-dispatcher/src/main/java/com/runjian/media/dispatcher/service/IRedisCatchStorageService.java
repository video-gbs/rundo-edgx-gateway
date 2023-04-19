package com.runjian.media.dispatcher.service;

import com.runjian.common.commonDto.Gateway.dto.SsrcConfig;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.constant.GatewayMsgType;
import com.runjian.common.constant.LogTemplate;
import com.runjian.common.constant.VideoManagerConstants;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.common.utils.redis.RedisCommonUtil;
import org.springframework.util.ObjectUtils;

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
     * @param gatewayMsgType
     * @param data
     */
    void editBusinessSceneKey(String businessSceneKey, GatewayMsgType gatewayMsgType, BusinessErrorEnums businessErrorEnums, Object data);

    /**
     * 操作业务场景的redis修改
     * @param businessSceneKey
     * @param gatewayMsgType
     * @param data
     */
    void editRunningBusinessSceneKey(String businessSceneKey, GatewayMsgType gatewayMsgType, BusinessErrorEnums businessErrorEnums, Object data);
    /**
     * redis的新增
     * @param businessSceneKey
     * @param gatewayMsgType
     * @param msgId
     */
    void addBusinessSceneKey(String businessSceneKey, GatewayMsgType gatewayMsgType, String msgId);

}
