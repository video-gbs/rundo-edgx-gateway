package com.runjian.service;

import com.runjian.common.mq.domain.CommonMqDto;

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

//    /**
//     * 操作业务场景的redis修改
//     * @param businessSceneKey
//     * @param gatewayMsgType
//     * @param data
//     */
//    void editBusinessSceneKey(String businessSceneKey, GatewayMsgType gatewayMsgType, BusinessErrorEnums businessErrorEnums, Object data);
//
//    /**
//     * 新增业务缓存
//     * @param businessSceneKey
//     * @param gatewayMsgType
//     * @param msgId
//     */
//    void addBusinessSceneKey(String businessSceneKey, GatewayMsgType gatewayMsgType, String msgId);
//
//    /**
//     * 获取一个业务缓存
//     * @param businessSceneKey
//     */
//    BusinessSceneResp getOneBusinessSceneKey(String businessSceneKey);
}
