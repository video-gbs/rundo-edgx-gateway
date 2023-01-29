package com.runjian.media.dispatcher;

import com.runjian.media.dispatcher.conf.druid.EnableDruidSupport;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author chenjialingasd
 * 对所有的流媒体使用，提供统一的接口能力;前期先支持zlm流媒体的使用能力。
 */
@SpringBootApplication
@EnableScheduling
@EnableDruidSupport
@ComponentScan(value = {"com.runjian.*"})
public class MediaDispatcher {
    public static void main(String[] args) {
        SpringApplication.run(MediaDispatcher.class, args);
    }
}