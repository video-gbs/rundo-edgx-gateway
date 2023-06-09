package com.runjian.media.manager;

import com.runjian.media.manager.conf.druid.EnableDruidSupport;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author chenjialing
 */
@SpringBootApplication
@EnableScheduling
@EnableDruidSupport
@ComponentScan(value = {"com.runjian.*"})
@MapperScan(basePackages = "com.runjian.**.mapper")
public class MediaManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(MediaManagerApplication.class, args);
    }
}
