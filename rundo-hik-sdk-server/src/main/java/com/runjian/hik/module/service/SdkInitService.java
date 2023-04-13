package com.runjian.hik.module.service;

import com.runjian.hik.module.task.InitSdkTask;
import com.runjian.hik.sdklib.HCNetSDK;
import com.runjian.hik.module.util.OsUtils;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadPoolExecutor;


/**
 * @author chenjialing
 */
@Slf4j
@Component
public class SdkInitService {

    public static HCNetSDK hCNetSDK = null;

    static FExceptionCallBack_Imp fExceptionCallBack;

    @Qualifier("taskExecutor")
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    static class FExceptionCallBack_Imp implements HCNetSDK.FExceptionCallBack {
        public void invoke(int dwType, int lUserID, int lHandle, Pointer pUser) {
            System.out.println("异常事件类型:" + dwType);
            return;
        }
    }

    public SdkInitService() {
        if (hCNetSDK == null) {
            synchronized (HCNetSDK.class) {
                try {
                    hCNetSDK = (HCNetSDK) Native.loadLibrary(OsUtils.getLoadLibrary(), HCNetSDK.class);
                } catch (Exception ex) {
                    log.error("SdkInitService-init-hCNetSDK-error");
                }
            }
        }
    }


    public void initSdk() {
        log.info("海康sdk-init-coming");
        taskExecutor.execute(new InitSdkTask());
    }
}
