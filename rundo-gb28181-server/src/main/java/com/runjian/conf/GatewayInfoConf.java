package com.runjian.conf;

import com.runjian.common.commonDto.Gateway.dto.EdgeGatewayInfoDto;
import com.runjian.common.config.response.GatewayBusinessSceneResp;
import com.runjian.mq.gatewayBusiness.asyncSender.GatewayBusinessAsyncSender;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 网关信息的配置
 * @author chenjialing
 */
@Component
@Data
public class GatewayInfoConf {

    private ConcurrentLinkedQueue<GatewayBusinessSceneResp> taskQueue = new ConcurrentLinkedQueue<>();
}
