package com.runjian.conf;

import com.runjian.common.commonDto.Gateway.dto.EdgeGatewayInfoDto;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 网关信息的配置
 * @author chenjialing
 */
@Component
@Data
public class PlayHandleConf {

    private ConcurrentHashMap<Integer,Object> socketHanderMap = new ConcurrentHashMap();
}
