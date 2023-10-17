package com.runjian.mq.event.mqEvent;

import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.GatewayBusinessSceneResp;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author chenjialing
 */
@AllArgsConstructor
@Data

public class MqSendSceneDto {

    GatewayBusinessSceneResp businessSceneResp;
    BusinessErrorEnums businessErrorEnums;
}
