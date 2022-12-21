package com.runjian.conf;

import com.runjian.domain.dto.EdgeGatewayInfoDto;
import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * 网关信息的配置
 * @author chenjialing
 */
@Component
@Data
public class GatewayInfoConf {

    private EdgeGatewayInfoDto edgeGatewayInfoDto;
}
