package com.runjian.common.config;

import com.runjian.common.config.response.GatewayBusinessSceneResp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentLinkedQueue;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
public class GatewayTaskQueueConfig {


    private ConcurrentLinkedQueue<GatewayBusinessSceneResp> taskQueue = new ConcurrentLinkedQueue<>();
}
