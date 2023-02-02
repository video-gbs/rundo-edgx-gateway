package com.runjian.service;

import com.runjian.common.commonDto.Gateway.dto.GatewayBindMedia;

/**
 * @author chenjialing
 */
public interface IGatewayBaseService {

    /**
     * 网关绑定调度服务
     * @param gatewayBindMedia
     */
    void gatewayBindMedia(GatewayBindMedia gatewayBindMedia);
}
