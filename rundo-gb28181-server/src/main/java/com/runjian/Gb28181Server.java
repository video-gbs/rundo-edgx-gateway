package com.runjian;

import com.runjian.conf.druid.EnableDruidSupport;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * @author chenjialing
 */
@SpringBootApplication
@EnableScheduling
//@EnableAsync
@EnableDruidSupport
public class Gb28181Server {
    private static ConfigurableApplicationContext context;
    private static String[] args;
    public static void main(String[] args) {
        Gb28181Server.args = args;
        Gb28181Server.context = SpringApplication.run(Gb28181Server.class, args);
    }
    // 项目重启
    public static void restart() {
        context.close();
        Gb28181Server.context = SpringApplication.run(Gb28181Server.class, args);
    }

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(50);
        return taskScheduler;
    }
}
