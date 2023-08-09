package com.runjian.media.manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author chenjialing
 */
@SpringBootApplication
@EnableScheduling
@ComponentScan(value = {"com.runjian.*"})
public class MediaManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(MediaManagerApplication.class, args);
    }
}
