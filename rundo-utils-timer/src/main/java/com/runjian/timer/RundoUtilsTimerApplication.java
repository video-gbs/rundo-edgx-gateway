package com.runjian.timer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(value = {"com.runjian.common","com.runjian.timer"})
public class RundoUtilsTimerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RundoUtilsTimerApplication.class, args);
    }

}
