package com.runjian.sdk.module.service.impl;



import com.runjian.common.constant.LogTemplate;
import com.runjian.domain.dto.commder.*;
import com.runjian.sdk.module.LoginModule;
import com.runjian.sdk.module.service.ISdkCommderService;
import com.runjian.sdk.module.service.SdkInitService;
import com.runjian.sdk.sdklib.NetSDKLib;
import com.runjian.sdk.sdklib.NetSDKLib.*;
import com.runjian.sdk.sdklib.ToolKits;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


@Service
@Slf4j
@DependsOn("sdkInitService")
public class SdkCommderServiceImpl implements ISdkCommderService {

//    @Autowired
//    SdkInitService sdkInitService;

    private static NetSDKLib hCNetSDK = LoginModule.netsdk;


    private ConcurrentHashMap<String,Long> loginHanderMap = new ConcurrentHashMap();
//    @PostConstruct
//    public void init(){
//        hCNetSDK = sdkInitService.getHCNetSDK();
//    }

    @Override
    public DeviceLoginDto login(String ip, int port, String user, String psw) {
        long lUserId = 0;//用户句柄
        String loginHandle = ip+":"+port;
        DeviceLoginDto deviceLoginDto = new DeviceLoginDto();
        deviceLoginDto.setLUserId(lUserId);
        if(!ObjectUtils.isEmpty(loginHanderMap)){

            long i = loginHanderMap.get(loginHandle);
            if(!ObjectUtils.isEmpty(i)){
                lUserId = i;
                deviceLoginDto.setLUserId(lUserId);
                deviceLoginDto.setErrorCode(0);
            }else {
                lUserId = 0;
            }
        }
        if(lUserId <= 0){

            //入参
            NetSDKLib.NET_IN_LOGIN_WITH_HIGHLEVEL_SECURITY pstInParam=new NetSDKLib.NET_IN_LOGIN_WITH_HIGHLEVEL_SECURITY();
            pstInParam.nPort=port;
            pstInParam.szIP=ip.getBytes();
            pstInParam.szPassword=psw.getBytes();
            pstInParam.szUserName=user.getBytes();
            //出参
            NET_OUT_LOGIN_WITH_HIGHLEVEL_SECURITY pstOutParam=new NET_OUT_LOGIN_WITH_HIGHLEVEL_SECURITY();

            pstOutParam.stuDeviceInfo=new NetSDKLib.NET_DEVICEINFO_Ex();
            //m_hLoginHandle = netsdk.CLIENT_LoginEx2(m_strIp, m_nPort, m_strUser, m_strPassword, 0, null, m_stDeviceInfo, nError);
            LLong m_hLoginHandle=hCNetSDK.CLIENT_LoginWithHighLevelSecurity(pstInParam, pstOutParam);
            lUserId = m_hLoginHandle.longValue();
            if(lUserId == 0) {
                //失败
                int errorCode = hCNetSDK.CLIENT_GetLastError();
                log.error(LogTemplate.ERROR_LOG_TEMPLATE,"sdk登陆失败",pstInParam,errorCode);
                deviceLoginDto.setErrorCode(errorCode);
                return deviceLoginDto;
            }

            deviceLoginDto.setLUserId(lUserId);
            deviceLoginDto.setErrorCode(0);
            loginHanderMap.put(loginHandle,lUserId);
            deviceLoginDto.setDeviceinfoV40(pstOutParam.stuDeviceInfo);
        }else {
            return deviceLoginDto;
        }


        return deviceLoginDto;
    }

    @Override
    public DeviceLoginOutDto logout(long lUserId) {
        return null;
    }

    @Override
    public DeviceConfigDto deviceConfig(long lUserId) {
        return null;
    }

    @Override
    public RecordInfoDto recordList(long lUserId, int lChannel, String startTime, String endTime) {
        return null;
    }

    @Override
    public Integer ptzControl(long lUserId, int lChannel, int dwPTZCommand, int dwStop, int dwSpeed) {
        return null;
    }

    @Override
    public PresetQueryDto presetList(long lUserId, int lChannel) {
        return null;
    }

    @Override
    public Integer presetControl(long lUserId, int lChannel, int commond, int presetNum) {
        return null;
    }

    @Override
    public Integer Zoom3DControl(long lUserId, int lChannel, int xTop, int yTop, int xBottom, int yBottom, int dragType) {
        return null;
    }

    @Override
    public Integer playBackControl(int lPlayHandle, int dwControlCode, int value) {
        return null;
    }

    @Override
    public Integer remoteControl(long lUserId, int dwCommand, String loginHandle) {
        return null;
    }
}
