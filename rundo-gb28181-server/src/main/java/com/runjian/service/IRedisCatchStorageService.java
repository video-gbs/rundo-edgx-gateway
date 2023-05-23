package com.runjian.service;

import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.common.config.response.GatewayBusinessSceneResp;
import com.runjian.common.constant.GatewayBusinessMsgType;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.conf.SsrcConfig;

import java.util.List;

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

    Boolean ssrcInit();

    Boolean ssrcRelease(String ssrc);

    SsrcConfig getSsrcConfig();

    Boolean setSsrcConfig(SsrcConfig ssrcConfig);
    /**
     * 操作业务场景的redis修改
     * @param businessSceneKey
     * @param GatewayBusinessMsgType
     * @param data
     */
    void editBusinessSceneKey(String businessSceneKey, GatewayBusinessMsgType GatewayBusinessMsgType, BusinessErrorEnums businessErrorEnums, Object data);

    /**
     * 新增业务缓存
     * @param businessSceneKey
     * @param GatewayBusinessMsgType
     * @param msgId
     */
    Boolean addBusinessSceneKey(String businessSceneKey, GatewayBusinessMsgType GatewayBusinessMsgType, String msgId);


    Boolean businessSceneLogDb(GatewayBusinessSceneResp businessSceneResp, List<String> msgStrings);

}
