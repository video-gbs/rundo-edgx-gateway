package com.runjian.media.dispatcher.conf;

import com.runjian.common.config.response.GatewayBusinessSceneResp;
import com.runjian.common.config.response.StreamBusinessSceneResp;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 网关信息的配置
 * @author chenjialing
 */
@Component
@Data
public class StreamInfoConf {

    private ConcurrentLinkedQueue<StreamBusinessSceneResp> taskQueue = new ConcurrentLinkedQueue<>();
}
