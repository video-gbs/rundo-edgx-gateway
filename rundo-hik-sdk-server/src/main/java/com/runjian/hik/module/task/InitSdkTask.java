package com.runjian.hik.module.task;

import com.runjian.hik.module.service.SdkInitService;
import com.runjian.hik.module.util.OsUtils;
import com.runjian.hik.sdklib.HCNetSDK;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Objects;

/**
 * Project     hikvision-sdk-integration
 * Package     com.ramble.hikvisionsdkintegration.task
 * Class       InitSdkTask
 * Date        2023/3/10 17:40
 * Author      MingliangChen
 * Email       cnaylor@163.com
 * Description
 */

@Slf4j
public class InitSdkTask implements Runnable {

    /**
     * 装配 sdk 所需依赖
     */
    private static HCNetSDK hCNetSDK = SdkInitService.hCNetSDK;

    @Override
    public void run() {
        try {
            if (Objects.equals(OsUtils.getOsName(), "linux")) {
                log.info("InitSdk-is-linux");
                String userDir = System.getProperty("user.dir");
                log.info("InitSdk-userDir={}", userDir);
                String osPrefix = OsUtils.getOsPrefix();
                if (osPrefix.toLowerCase().startsWith("linux-i386")) {
                    HCNetSDK.BYTE_ARRAY ptrByteArray1 = new HCNetSDK.BYTE_ARRAY(256);
                    HCNetSDK.BYTE_ARRAY ptrByteArray2 = new HCNetSDK.BYTE_ARRAY(256);
                    //这里是库的绝对路径，请根据实际情况修改，注意改路径必须有访问权限
                    //linux 下， 库加载参考：OsUtils.getLoadLibrary()
                    String strPath1 = System.getProperty("user.dir") + File.separator + "hikvision" + File.separator + "linux" + File.separator + "x32"+ File.separator + "libcrypto.so.1.1";
                    String strPath2 = System.getProperty("user.dir") + File.separator + "hikvision" + File.separator + "linux" + File.separator + "x32"+ File.separator + "libssl.so.1.1";

                    System.arraycopy(strPath1.getBytes(), 0, ptrByteArray1.byValue, 0, strPath1.length());
                    ptrByteArray1.write();
                    hCNetSDK.NET_DVR_SetSDKInitCfg(3, ptrByteArray1.getPointer());

                    System.arraycopy(strPath2.getBytes(), 0, ptrByteArray2.byValue, 0, strPath2.length());
                    ptrByteArray2.write();
                    hCNetSDK.NET_DVR_SetSDKInitCfg(4, ptrByteArray2.getPointer());
                    //linux 下， 库加载参考：OsUtils.getLoadLibrary()
                    String strPathCom = System.getProperty("user.dir") + File.separator + "hikvision" + File.separator + "linux" + File.separator + "x32"+ File.separator + "HCNetSDKCom/";
                    HCNetSDK.NET_DVR_LOCAL_SDK_PATH struComPath = new HCNetSDK.NET_DVR_LOCAL_SDK_PATH();
                    System.arraycopy(strPathCom.getBytes(), 0, struComPath.sPath, 0, strPathCom.length());
                    struComPath.write();
                    hCNetSDK.NET_DVR_SetSDKInitCfg(2, struComPath.getPointer());
                } else if (osPrefix.toLowerCase().startsWith("linux-amd64")) {
                    HCNetSDK.BYTE_ARRAY ptrByteArray1 = new HCNetSDK.BYTE_ARRAY(256);
                    HCNetSDK.BYTE_ARRAY ptrByteArray2 = new HCNetSDK.BYTE_ARRAY(256);
                    //这里是库的绝对路径，请根据实际情况修改，注意改路径必须有访问权限
                    //linux 下， 库加载参考：OsUtils.getLoadLibrary()
                    String strPath1 = System.getProperty("user.dir") + File.separator + "hikvision" + File.separator + "linux" + File.separator + "x64"+ File.separator + "libcrypto.so.1.1";
                    String strPath2 = System.getProperty("user.dir") + File.separator + "hikvision" + File.separator + "linux" + File.separator + "x64"+ File.separator + "libssl.so.1.1";

                    System.arraycopy(strPath1.getBytes(), 0, ptrByteArray1.byValue, 0, strPath1.length());
                    ptrByteArray1.write();
                    hCNetSDK.NET_DVR_SetSDKInitCfg(3, ptrByteArray1.getPointer());

                    System.arraycopy(strPath2.getBytes(), 0, ptrByteArray2.byValue, 0, strPath2.length());
                    ptrByteArray2.write();
                    hCNetSDK.NET_DVR_SetSDKInitCfg(4, ptrByteArray2.getPointer());

                    String strPathCom = System.getProperty("user.dir") + File.separator + "hikvision" + File.separator + "linux" + File.separator + "x64"+ File.separator + "HCNetSDKCom/";
                    //linux 下， 库加载参考：OsUtils.getLoadLibrary()
                    HCNetSDK.NET_DVR_LOCAL_SDK_PATH struComPath = new HCNetSDK.NET_DVR_LOCAL_SDK_PATH();
                    System.arraycopy(strPathCom.getBytes(), 0, struComPath.sPath, 0, strPathCom.length());
                    struComPath.write();
                    hCNetSDK.NET_DVR_SetSDKInitCfg(2, struComPath.getPointer());
                } else {
                    log.info("osPrefix={}", osPrefix);
                }
            }
            //初始化sdk
            boolean isOk = hCNetSDK.NET_DVR_Init();
            hCNetSDK.NET_DVR_SetConnectTime(10, 1);
            hCNetSDK.NET_DVR_SetReconnect(100, true);
            if (!isOk) {
                log.error("=================== InitSDK init fail ===================");
            } else {
                log.info("============== InitSDK init success ====================");
            }
        } catch (Exception e) {
            log.error("InitSDK-error,e={}", e.getMessage());
            e.printStackTrace();
        }
    }
}
