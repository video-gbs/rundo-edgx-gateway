package com.runjian;

import com.runjian.hik.module.service.SdkInitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @author chenjialing
 */

@Component
public class AppRunner implements ApplicationRunner {

    @Autowired
    private SdkInitService hksdkInitService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        hksdkInitService.initSdk();
    }
}
