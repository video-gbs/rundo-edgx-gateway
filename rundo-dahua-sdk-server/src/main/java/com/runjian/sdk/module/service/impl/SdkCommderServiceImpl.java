package com.runjian.sdk.module.service.impl;

import com.runjian.common.constant.LogTemplate;
import com.runjian.common.constant.MarkConstant;
import com.runjian.common.utils.DateUtils;
import com.runjian.common.utils.StringUtils;
import com.runjian.common.utils.XmlUtil;

import com.runjian.domain.dto.commder.*;
import com.runjian.sdk.module.service.ISdkCommderService;
import com.runjian.sdk.module.service.SdkInitService;
import com.runjian.sdk.sdklib.NetSDKLib;
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

    @Autowired
    SdkInitService sdkInitService;

    private static NetSDKLib hCNetSDK;
    @PostConstruct
    public void init(){
        hCNetSDK = sdkInitService.getHCNetSDK();
    }

    @Override
    public DeviceLoginDto login(String ip, short port, String user, String psw) {
        return null;
    }

    @Override
    public DeviceLoginOutDto logout(int lUserId) {
        return null;
    }

    @Override
    public DeviceConfigDto deviceConfig(int lUserId) {
        return null;
    }

    @Override
    public RecordInfoDto recordList(int lUserId, int lChannel, String startTime, String endTime) {
        return null;
    }

    @Override
    public Integer ptzControl(int lUserId, int lChannel, int dwPTZCommand, int dwStop, int dwSpeed) {
        return null;
    }

    @Override
    public PresetQueryDto presetList(int lUserId, int lChannel) {
        return null;
    }

    @Override
    public Integer presetControl(int lUserId, int lChannel, int commond, int presetNum) {
        return null;
    }

    @Override
    public Integer Zoom3DControl(int lUserId, int lChannel, int xTop, int yTop, int xBottom, int yBottom, int dragType) {
        return null;
    }

    @Override
    public Integer playBackControl(int lPlayHandle, int dwControlCode, int value) {
        return null;
    }

    @Override
    public Integer remoteControl(int lUserId, int dwCommand, String loginHandle) {
        return null;
    }
}
