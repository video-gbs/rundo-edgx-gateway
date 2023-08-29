package com.runjian.conf;

import com.sun.jna.Pointer;
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

    private ConcurrentHashMap<Pointer,Object> socketHanderMap = new ConcurrentHashMap();
}
