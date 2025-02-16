package com.runjian.sdk.module.service.impl;



import com.runjian.common.constant.LogTemplate;
import com.runjian.common.utils.StringUtils;
import com.runjian.domain.dto.commder.*;
import com.runjian.entity.DeviceChannelEntity;
import com.runjian.sdk.common.SavePath;
import com.runjian.sdk.module.LoginModule;
import com.runjian.sdk.module.event.AlarmIntellectEvent;
import com.runjian.sdk.module.jnaDto.AlarmIntellectDto;
import com.runjian.sdk.module.service.ISdkCommderService;
import com.runjian.sdk.sdklib.NetSDKLib;
import com.runjian.sdk.sdklib.NetSDKLib.*;
import com.runjian.sdk.sdklib.ToolKits;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.runjian.sdk.sdklib.NetSDKLib.EVENT_IVS_CROSSLINEDETECTION;
import static com.runjian.sdk.sdklib.NetSDKLib.EVENT_IVS_CROSSREGIONDETECTION;


@Service
@Slf4j
@DependsOn("sdkInitService")
public class SdkCommderServiceImpl implements ISdkCommderService {

//    @Autowired
//    SdkInitService sdkInitService;

    private static NetSDKLib hCNetSDK = LoginModule.netsdk;


    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    private ConcurrentHashMap<String, Long> loginHanderMap = new ConcurrentHashMap();
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
            }
        }
        if(lUserId == 0){

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

            m_AnalyzerDataCB mAnalyzerDataCB = new m_AnalyzerDataCB();
            LLong lLong = hCNetSDK.CLIENT_RealLoadPictureEx(m_hLoginHandle, -1, NetSDKLib.EVENT_IVS_ALL,
            1, mAnalyzerDataCB, null, null);
            if( lLong.longValue() != 0  ) {
                System.out.println("CLIENT_RealLoadPictureEx Success  ChannelId : \n" + -1);
            } else {
                System.err.println("CLIENT_RealLoadPictureEx Failed!" + ToolKits.getErrorCodePrint());
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
    public ChannelInfoDto channelSync(long lUserId, int channelNum) {

        LLong lhandle = new LLong(lUserId);


//        LLong m_hLoginHandle=hCNetSDK.CLIENT_GetDevConfig(lhandle, hCNetSDK.NET_DEV_CHANNELCFG,channelNum);
        return null;
    }

    @Override
    public ChannelInfoDto getNvrChannelList(long lUserId, String charset) {
        // 显示源信息数组初始化
        int nCameraCount = 60;
        int nMaxVideoInputCount = 10; //视频输入通道最大数
        NET_MATRIX_CAMERA_INFO[]  cameras = new NET_MATRIX_CAMERA_INFO[nCameraCount];
        // 视频输入通道信息数组初始化
        NetSDKLib.NET_VIDEO_INPUTS[] inputs= new NetSDKLib.NET_VIDEO_INPUTS[nMaxVideoInputCount];
        for (int j = 0; j < nMaxVideoInputCount; j++) {
            inputs[j] = new NetSDKLib.NET_VIDEO_INPUTS();
        }
        for(int i = 0; i < nCameraCount; i++) {
            NetSDKLib.NET_MATRIX_CAMERA_INFO camera = new NetSDKLib.NET_MATRIX_CAMERA_INFO();
            NetSDKLib.NET_REMOTE_DEVICE device = new NetSDKLib.NET_REMOTE_DEVICE();
            device.nMaxVideoInputCount = nMaxVideoInputCount;// 视频输入通道最大数
            device.pstuVideoInputs = new Memory(inputs[0].size() * nMaxVideoInputCount);
            device.pstuVideoInputs.clear(inputs[0].size() * nMaxVideoInputCount);
            // 将数组内存拷贝到Pointer
            ToolKits.SetStructArrToPointerData(inputs, device.pstuVideoInputs);
            camera.stuRemoteDevice = device;
            cameras[i] = camera;
        }

        /*
         *  入参
         */
        NetSDKLib.NET_IN_MATRIX_GET_CAMERAS stuIn = new NetSDKLib.NET_IN_MATRIX_GET_CAMERAS();

        /*
         *  出参
         */
        NET_OUT_MATRIX_GET_CAMERAS stuOut = new NET_OUT_MATRIX_GET_CAMERAS();
        stuOut.nMaxCameraCount = nCameraCount;
        stuOut.pstuCameras = new Memory(cameras[0].size() * nCameraCount);
        stuOut.pstuCameras.clear(cameras[0].size() * nCameraCount);

        ToolKits.SetStructArrToPointerData(cameras, stuOut.pstuCameras);  // 将数组内存拷贝到Pointer
        LLong loginHandle = new LLong(lUserId);   //登陆句柄
        ChannelInfoDto channelInfoDto = new ChannelInfoDto();
        List<DeviceChannelEntity> deviceChannelEntities = new ArrayList<>();
        if(hCNetSDK.CLIENT_MatrixGetCameras(loginHandle, stuIn, stuOut, 5000)) {
            ToolKits.GetPointerDataToStructArr(stuOut.pstuCameras, cameras);  // 将 Pointer 的内容 输出到   数组

            for(int j = 0; j < stuOut.nRetCameraCount; j++) {
                if(cameras[j].bRemoteDevice == 1) {
                    if(cameras[j].stuRemoteDevice.bEnable == 0){
                        continue;
                    }
                    ToolKits.GetPointerDataToStructArr(cameras[j].stuRemoteDevice.pstuVideoInputs, inputs);  // 将 Pointer 的内容 输出到   数组
                    DeviceChannelEntity deviceChannelEntity = new DeviceChannelEntity();
                    deviceChannelEntity.setChannelNum(cameras[j].nUniqueChannel);
                    deviceChannelEntity.setIp(new String(cameras[j].stuRemoteDevice.szIp).trim());
                    deviceChannelEntity.setPort(cameras[j].stuRemoteDevice.nPort);
                    deviceChannelEntity.setPassword(new String(cameras[j].stuRemoteDevice.szPwd).trim());
                    deviceChannelEntity.setManufacturer("dahua");
                    deviceChannelEntity.setIsIpChannel(1);


                    for (int i = 0; i < cameras[j].stuRemoteDevice.nRetVideoInputCount; i++) {
                        deviceChannelEntity.setChannelName(StringUtils.getStringFromByte(inputs[i].szChnName,charset));
                    }
                    deviceChannelEntities.add(deviceChannelEntity);
                }
            }
            QueryChannelState(loginHandle, deviceChannelEntities);
            channelInfoDto.setChannelList(deviceChannelEntities);
        } else {
            int i = LoginModule.netsdk.CLIENT_GetLastError();
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "通道同步", "失败，状态码：", i);
            String errorMsg = ToolKits.getErrorCodePrint();
            channelInfoDto.setErrorCode(i);
            channelInfoDto.setErrorMsg(errorMsg);
            return channelInfoDto;

        }

        return channelInfoDto;
    }

    private void QueryChannelState(LLong loginHandle, List<DeviceChannelEntity> deviceChannelEntities) {
        int nQueryType = NetSDKLib.NET_QUERY_GET_CAMERA_STATE;

        // 入参
        NET_IN_GET_CAMERA_STATEINFO stIn = new NET_IN_GET_CAMERA_STATEINFO();
        stIn.bGetAllFlag = 1; // 1-true,查询所有摄像机状态

        // 摄像机通道信息   // 通道个数
        int chnCount = 10;
        NET_CAMERA_STATE_INFO[] cameraInfo = new NET_CAMERA_STATE_INFO[chnCount];
        for(int i = 0; i < chnCount; i++) {
            cameraInfo[i] = new NET_CAMERA_STATE_INFO();
        }

        // 出参
        NET_OUT_GET_CAMERA_STATEINFO stOut = new NET_OUT_GET_CAMERA_STATEINFO();
        stOut.nMaxNum = chnCount;
        stOut.pCameraStateInfo = new Memory(cameraInfo[0].size() * chnCount);
        stOut.pCameraStateInfo.clear(cameraInfo[0].size() * chnCount);

        ToolKits.SetStructArrToPointerData(cameraInfo, stOut.pCameraStateInfo);  // 将数组内存拷贝到Pointer

        stIn.write();
        stOut.write();
        boolean bRet = hCNetSDK.CLIENT_QueryDevInfo(loginHandle, nQueryType, stIn.getPointer(), stOut.getPointer(), null, 3000);
        stIn.read();
        stOut.read();

        if(bRet) {
            ToolKits.GetPointerDataToStructArr(stOut.pCameraStateInfo, cameraInfo);  // 将 Pointer 的内容 输出到   数组

            System.out.println("查询到的摄像机通道状态有效个数：" + stOut.nValidNum);

            for(int i = 0; i < stOut.nValidNum; i++) {
                for (DeviceChannelEntity one:deviceChannelEntities)
                    if(i == one.getChannelNum()){
                        one.setOnline(getChannelState(cameraInfo[i].emConnectionState));
                    }
            }
        } else {
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "通道同步", "失败", ToolKits.getErrorCodeShow());
        }
    }
    private int getChannelState(int state) {
        String channelState = "";
        int online = 0;
        switch (state) {
            case EM_CAMERA_STATE_TYPE.EM_CAMERA_STATE_TYPE_UNKNOWN:
                channelState = "未知";
                break;
            case EM_CAMERA_STATE_TYPE.EM_CAMERA_STATE_TYPE_CONNECTING:
                channelState = "正在连接";
                break;
            case EM_CAMERA_STATE_TYPE.EM_CAMERA_STATE_TYPE_CONNECTED:
                channelState = "已连接";
                online = 1;
                break;
            case EM_CAMERA_STATE_TYPE.EM_CAMERA_STATE_TYPE_UNCONNECT:
                channelState = "未连接";
                break;
            case EM_CAMERA_STATE_TYPE.EM_CAMERA_STATE_TYPE_EMPTY:
                channelState = "未连接";
                break;
            case EM_CAMERA_STATE_TYPE.EM_CAMERA_STATE_TYPE_DISABLE:
                channelState = "通道未配置,无信息";
                break;
            default:
                channelState = "通道有配置,但被禁用";
                break;
        }
        return online;
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


    public class m_AnalyzerDataCB implements  NetSDKLib.fAnalyzerDataCallBack{

        @Override
        public int invoke(LLong lAnalyzerHandle, int dwAlarmType, Pointer pAlarmInfo, Pointer pBuffer, int dwBufSize, Pointer dwUser, int nSequence, Pointer reserved) throws UnsupportedEncodingException {
            String strFileName="";
            if(pBuffer != null && dwBufSize > 0) {
                strFileName = SavePath.getSavePath().getSaveCapturePath();
                byte[] buf = pBuffer.getByteArray(0, dwBufSize);
                ByteArrayInputStream byteArrInput = new ByteArrayInputStream(buf);
                try {
                    BufferedImage bufferedImage = ImageIO.read(byteArrInput);
                    if (bufferedImage == null) {
                        return 0;
                    }
                    ImageIO.write(bufferedImage, "jpg", new File(strFileName));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            int anInt = 0;
            if(!ObjectUtils.isEmpty(dwUser)){
                anInt = dwUser.getInt(0);

            }
            AlarmIntellectDto alarmIntellectDto = new AlarmIntellectDto();
            alarmIntellectDto.setImageType(2);
            alarmIntellectDto.setCaptureImageUrl(strFileName);
            alarmIntellectDto.setId(anInt);
            //智能分析回调  区域入侵与绊线入侵
            if(dwAlarmType == EVENT_IVS_CROSSLINEDETECTION){
                //绊线入侵
                DEV_EVENT_CROSSLINE_INFO info = new NetSDKLib.DEV_EVENT_CROSSLINE_INFO();
                ToolKits.GetPointerDataToStruct(pAlarmInfo, 0, info);
                alarmIntellectDto.setEventId(String.valueOf(info.nEventID));
                alarmIntellectDto.setEventTime(info.UTC.toStringTime1());
                byte bEventAction = info.bEventAction;
                int eventType = 0;
                if(bEventAction==0 || bEventAction==1){
                    eventType = 1;
                }else {
                    eventType = 2;
                }
                alarmIntellectDto.setEventType(eventType);
                alarmIntellectDto.setEventCode(dwAlarmType);
                alarmIntellectDto.setChannelId(info.nChannelID);
                String tittle = new String(info.szName);
                alarmIntellectDto.setEventTitle("绊线入侵事件");
                AlarmIntellectEvent event = new AlarmIntellectEvent(this);
                event.setAllarmIntellectDto(alarmIntellectDto);
                applicationEventPublisher.publishEvent(event);




            }else if(dwAlarmType == EVENT_IVS_CROSSREGIONDETECTION){
                DEV_EVENT_CROSSREGION_INFO info = new NetSDKLib.DEV_EVENT_CROSSREGION_INFO();
                ToolKits.GetPointerDataToStruct(pAlarmInfo, 0, info);
                alarmIntellectDto.setEventId(String.valueOf(info.nEventID));
                alarmIntellectDto.setEventTime(info.UTC.toStringTime1());
                byte bEventAction = info.bEventAction;
                int eventType = 0;
                if(bEventAction==0 || bEventAction==1){
                    eventType = 1;
                }else {
                    eventType = 2;
                }
                alarmIntellectDto.setEventType(eventType);
                alarmIntellectDto.setEventCode(dwAlarmType);
                alarmIntellectDto.setChannelId(info.nChannelID);
                String tittle = new String(info.szName);
                alarmIntellectDto.setEventTitle("区域入侵事件");
                AlarmIntellectEvent event = new AlarmIntellectEvent(this);
                event.setAllarmIntellectDto(alarmIntellectDto);
                applicationEventPublisher.publishEvent(event);
            }



            log.info(String.valueOf(dwAlarmType));

            return 0;
        }
    }
    @Override
    public Integer intellectAlarm(long lUserId, int channelNm,int dwUser) {
        int bNeedPicture = 1; // 是否需要图片
        m_AnalyzerDataCB mAnalyzerDataCB = new m_AnalyzerDataCB();
        LLong lhandle = new LLong(lUserId);
        IntByReference intByReference = new IntByReference(20044);
        Pointer dwuser = intByReference.getPointer();

        LLong lLong = hCNetSDK.CLIENT_RealLoadPictureEx(lhandle, channelNm, NetSDKLib.EVENT_IVS_ALL,
                bNeedPicture, mAnalyzerDataCB, dwuser, null);
        if( lLong.longValue() != 0  ) {
            System.out.println("CLIENT_RealLoadPictureEx Success  ChannelId : \n" + channelNm);
            return hCNetSDK.CLIENT_GetLastError();
        } else {
            System.err.println("CLIENT_RealLoadPictureEx Failed!" + ToolKits.getErrorCodePrint());
        }

        return 0;
    }
}
