package com.runjian.sdk.module.service;


import com.runjian.sdk.sdklib.NetSDKLib;
import com.runjian.sdk.sdklib.ToolKits;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.io.File;
import java.util.Objects;


/**
 * @author chenjialing
 */
@Slf4j
@Component
@Data
public class SdkInitService {

    public  NetSDKLib hCNetSDK;

    @Qualifier("taskExecutor")
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    // device disconnect callback instance
    private  DisConnect disConnect  = new DisConnect();

    // device reconnect callback instance
    private static HaveReConnect haveReConnect = new HaveReConnect();


    private static boolean bInit    = false;
    private static boolean bLogopen = false;
    public SdkInitService() {
        if (hCNetSDK == null) {
            synchronized (NetSDKLib.class) {
                try {
                    hCNetSDK = NetSDKLib.NETSDK_INSTANCE;
                } catch (Exception ex) {
                    log.error("SdkInitService-init-hCNetSDK-error");
                }
            }
        }
    }

    private class DisConnect implements NetSDKLib.fDisConnect {


        @Override
        public void invoke(NetSDKLib.LLong lLoginID, String pchDVRIP, int nDVRPort, Pointer dwUser) {
            log.info("Device[%s] Port[%d] DisConnect!\n", pchDVRIP, nDVRPort);
        }
    }

    // device reconnect(success) callback class
    // set it's instance by call CLIENT_SetAutoReconnect, when device reconnect success sdk will call it.
    private static  class HaveReConnect implements NetSDKLib.fHaveReConnect {
        public void invoke(NetSDKLib.LLong m_hLoginHandle, String pchDVRIP, int nDVRPort, Pointer dwUser) {
            log.info("ReConnect Device[%s] Port[%d]\n", pchDVRIP, nDVRPort);

        }
    }

    @PostConstruct
    public void initSdk() throws Exception {
        init(disConnect, haveReConnect);
    }

    private  boolean init(NetSDKLib.fDisConnect disConnect, NetSDKLib.fHaveReConnect haveReConnect) {
        bInit = hCNetSDK.CLIENT_Init(disConnect, null);
        if(!bInit) {
            System.out.println("Initialize SDK failed");
            return false;
        }

        //打开日志，可选
        NetSDKLib.LOG_SET_PRINT_INFO setLog = new NetSDKLib.LOG_SET_PRINT_INFO();
        File path = new File("./sdklog/");
        if (!path.exists()) {
            path.mkdir();
        }
        String logPath = path.getAbsoluteFile().getParent() + "\\sdklog\\" + ToolKits.getDate() + ".log";
        setLog.nPrintStrategy = 0;
        setLog.bSetFilePath = 1;
        System.arraycopy(logPath.getBytes(), 0, setLog.szLogFilePath, 0, logPath.getBytes().length);
        System.out.println(logPath);
        setLog.bSetPrintStrategy = 1;
        bLogopen = hCNetSDK.CLIENT_LogOpen(setLog);
        if(!bLogopen ) {
            System.err.println("Failed to open NetSDK log");
        }

        // 设置断线重连回调接口，设置过断线重连成功回调函数后，当设备出现断线情况，SDK内部会自动进行重连操作
        // 此操作为可选操作，但建议用户进行设置
        hCNetSDK.CLIENT_SetAutoReconnect(haveReConnect, null);

        //设置登录超时时间和尝试次数，可选
        int waitTime = 5000; //登录请求响应超时时间设置为5S
        int tryTimes = 1;    //登录时尝试建立链接1次
        hCNetSDK.CLIENT_SetConnectTime(waitTime, tryTimes);


        // 设置更多网络参数，NET_PARAM的nWaittime，nConnectTryNum成员与CLIENT_SetConnectTime
        // 接口设置的登录设备超时时间和尝试次数意义相同,可选
        NetSDKLib.NET_PARAM netParam = new NetSDKLib.NET_PARAM();
        netParam.nConnectTime = 10000;      // 登录时尝试建立链接的超时时间
        netParam.nGetConnInfoTime = 3000;   // 设置子连接的超时时间
        netParam.nGetDevInfoTime = 3000;//获取设备信息超时时间，为0默认1000ms
        hCNetSDK.CLIENT_SetNetworkParam(netParam);

        return true;
    }
}
