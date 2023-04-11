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
public class HikSdkServer {
    private static ConfigurableApplicationContext context;
    private static String[] args;
    public static void main(String[] args) {
        HikSdkServer.args = args;
        HikSdkServer.context = SpringApplication.run(HikSdkServer.class, args);
    }
    // 项目重启
    public static void restart() {
        context.close();
        HikSdkServer.context = SpringApplication.run(HikSdkServer.class, args);
    }

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(50);
        return taskScheduler;
    }
}
