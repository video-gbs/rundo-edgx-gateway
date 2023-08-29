package com.runjian.conf;

import com.runjian.common.commonDto.Gateway.dto.EdgeGatewayInfoDto;
import com.sun.jna.Pointer;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * 网关信息的配置
 * @author chenjialing
 */
@Component
@Data
public class PlayHandleConf {

    private ConcurrentHashMap<Pointer,Object> socketHanderMap = new ConcurrentHashMap();
}
