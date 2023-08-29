package com.runjian;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * @author chenjialing
 */
@SpringBootApplication
@EnableScheduling
@MapperScan(basePackages = "com.runjian.**.mapper")
public class DahuaSdkServer {
    private static ConfigurableApplicationContext context;
    private static String[] args;
    public static void main(String[] args) {
        DahuaSdkServer.args = args;
        DahuaSdkServer.context = SpringApplication.run(DahuaSdkServer.class, args);
    }


    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(50);
        return taskScheduler;
    }
}
