package com.runjian.common.commonDto;

import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.constant.BusinessSceneStatusEnum;
import com.runjian.common.constant.GatewayBusinessMsgType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 业务场景返回值
 * @author chenjialing
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GatewayBusinessNotifyReq implements Serializable {

    /**
     * 消息码
     */
    private int code;

    /**
     * 消息
     */
    private String msg;


    /**
     * 消息ID
     */
    private String msgId;


    private GatewayBusinessMsgType gatewayMsgType;

    private String streamId;





}
