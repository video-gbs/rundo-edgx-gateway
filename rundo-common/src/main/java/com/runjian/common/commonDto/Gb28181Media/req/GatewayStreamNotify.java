package com.runjian.common.commonDto.Gb28181Media.req;

import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.common.config.response.GatewayBusinessSceneResp;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author chenjialing
 */
@Data
public class GatewayStreamNotify {

    @NotNull(message= "流id不得为空")
    String streamId;

    @NotNull(message="业务dto不得为空")
    GatewayBusinessSceneResp businessSceneResp;

}
