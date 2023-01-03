package com.runjian.media.dispatcher;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author chenjialing
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
@ComponentScan(value = {"com.runjian.common","com.runjian.timer"})
public class MediaDispatcher {
}
