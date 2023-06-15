package com.runjian.media.manager.event.mqEvent;

import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.StreamBusinessSceneResp;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author chenjialing
 */
@AllArgsConstructor
@Data

public class MqSendSceneDto {

    StreamBusinessSceneResp streamBusinessSceneResp;
    BusinessErrorEnums businessErrorEnums;
    Object data;
}
