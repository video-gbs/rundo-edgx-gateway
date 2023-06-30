package com.runjian.hik.module.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.runjian.common.constant.LogTemplate;
import com.runjian.common.constant.MarkConstant;
import com.runjian.common.utils.DateUtils;
import com.runjian.common.utils.FileUtil;
import com.runjian.common.utils.StringUtils;
import com.runjian.conf.PlayHandleConf;
import com.runjian.domain.dto.DeviceChannel;
import com.runjian.domain.dto.commder.*;
import com.runjian.entity.DeviceChannelEntity;
import com.runjian.entity.DeviceEntity;
import com.runjian.hik.module.service.ISdkCommderService;
import com.runjian.hik.module.service.SdkInitService;
//import com.runjian.hik.module.service.impl.callBack.FRealDataCallBack;
import com.runjian.hik.sdklib.HCNetSDK;
import com.runjian.hik.sdklib.SocketPointer;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.examples.win32.W32API;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.runjian.hik.sdklib.HCNetSDK.NET_DVR_GET_PICCFG_V30;

@Service
@Slf4j
@DependsOn("sdkInitService")
public class SdkCommderServiceImpl implements ISdkCommderService {

    @Autowired
    SdkInitService sdkInitService;

    private static HCNetSDK hCNetSDK;
    //预览句柄
    int  lPreviewHandle;

    //预览回调函数实现
    private StandardRealDataCallBack standardRealDataCallBack;

    private FRealDataCallBack fRealDataCallBack;


    private PlayBackCallBack playBackCallBack;



    @Autowired
    private PlayHandleConf playHandleConf;

    @PostConstruct
    public void init(){
        hCNetSDK = sdkInitService.getHCNetSDK();
        fRealDataCallBack = new FRealDataCallBack();
        playBackCallBack = new PlayBackCallBack();
    }
    public class PlayBackCallBack implements HCNetSDK.FPlayDataCallBack{

        @Override
        public void invoke(int lPlayHandle, int dwDataType, Pointer pBuffer, int dwBufSize, Pointer dwUser) {
            switch (dwDataType) {
                case HCNetSDK.NET_DVR_SYSHEAD: //系统头

                    break;
                case HCNetSDK.NET_DVR_STREAMDATA:   //码流数据
                    if (dwBufSize > 0) {
                        ByteBuffer byteBuffer = pBuffer.getByteBuffer(0, dwBufSize);
                        byte[] bytes = new byte[byteBuffer.remaining()];
                        byteBuffer.get(bytes, 0, bytes.length);

                        SocketPointer socketPointer = new SocketPointer();
                        Pointer pointer = socketPointer.getPointer();
                        pointer.write(0, dwUser.getByteArray(0, socketPointer.size()), 0, socketPointer.size());
                        socketPointer.read();
                        String socketHandle = socketPointer.socketHandle;
                        System.out.println(socketHandle);

                    }
            }
        }
    }

    public class FRealDataCallBack implements HCNetSDK.FRealDataCallBack_V30{

        /**
         * 点播回调
         */
        //预览回调
        @Override
        public void invoke(int lRealHandle, int dwDataType, ByteByReference pBuffer, int dwBufSize, Pointer pUser) {
            switch (dwDataType) {
                case HCNetSDK.NET_DVR_SYSHEAD: //系统头

                    break;
                case HCNetSDK.NET_DVR_STREAMDATA:   //码流数据
                    if (dwBufSize > 0) {
                        try {



                            SocketPointer socketPointer = new SocketPointer();
                            Pointer pointer = socketPointer.getPointer();
                            socketPointer.read();
                            socketPointer.write();
                            pUser.write(0, pointer.getByteArray(0, socketPointer.size()), 0, socketPointer.size());
                            if(ObjectUtils.isEmpty(socketPointer)){
                                log.info(LogTemplate.PROCESS_LOG_TEMPLATE,"码流回调","连接错误，初始化失败");
                                return;
                            }

                            Socket socket = (Socket) playHandleConf.getSocketHanderMap().get(socketPointer);
                            if(ObjectUtils.isEmpty(socket)){
                                log.info(LogTemplate.PROCESS_LOG_TEMPLATE,"码流回调","连接暂时超时");
                                return;
                            }
                            ByteBuffer byteBuffer = pBuffer.getPointer().getByteBuffer(0, dwBufSize);
                            byte[] bytes = new byte[byteBuffer.remaining()];
                            byteBuffer.get(bytes, 0, bytes.length);

                            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                            dataOutputStream.write(bytes);
                        } catch (Exception e) {
                            //socket连接失败 进行流关闭
                            PlayInfoDto playInfoDto = stopPlay(lRealHandle);
                            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"自研流媒体服务连接","socket发送异常",playInfoDto);
                        }
                    }
            }
        }
    }

    public class StandardRealDataCallBack implements HCNetSDK.FStdDataCallBack{
        /**
         * 点播回调
         */
        //预览回调
        @Override
        public void invoke(int lRealHandle, int dwDataType, ByteByReference pBuffer, int dwBufSize, int dwUser) {
            switch (dwDataType) {
                case HCNetSDK.NET_DVR_SYSHEAD: //系统头

                    break;
                case HCNetSDK.NET_DVR_STREAMDATA:   //码流数据
                    if (dwBufSize > 0) {
                        try {
                            Socket socket = (Socket) playHandleConf.getSocketHanderMap().get(lRealHandle);
                            if(ObjectUtils.isEmpty(socket)){
                                log.info(LogTemplate.PROCESS_LOG_TEMPLATE,"码流回调","连接暂时超时");
                                return;
                            }
                            ByteBuffer byteBuffer = pBuffer.getPointer().getByteBuffer(0, dwBufSize);
                            byte[] bytes = new byte[byteBuffer.remaining()];
                            byteBuffer.get(bytes, 0, bytes.length);
                            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                            dataOutputStream.write(bytes);
                            FileUtil.save2File("./test.es",bytes);

                        } catch (Exception e) {
                            //socket连接失败 进行流关闭
                            PlayInfoDto playInfoDto = stopPlay(lRealHandle);
                            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"自研流媒体服务连接--socket发送异常",e,playInfoDto);
                        }
                    }
                    break;
                case HCNetSDK.NET_DVR_STD_VIDEODATA:
                    System.out.println(dwBufSize);
                    break;
            }
        }

    }





    @Override
    public DeviceLoginDto login(String ip, short port, String user, String psw) {
        int lUserId = -1;//用户句柄
        //设备注册
        HCNetSDK.NET_DVR_USER_LOGIN_INFO m_strLoginInfo = new HCNetSDK.NET_DVR_USER_LOGIN_INFO();//设备登录信息
        HCNetSDK.NET_DVR_DEVICEINFO_V40 m_strDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V40();//设备信息
        HCNetSDK.NET_DVR_DEVICEINFO_V30 m_strDeviceInfo3 =  new HCNetSDK.NET_DVR_DEVICEINFO_V30();//设备信息
        String m_sDeviceIP = ip;//设备ip地址
        m_strLoginInfo.sDeviceAddress = new byte[HCNetSDK.NET_DVR_DEV_ADDRESS_MAX_LEN];
        System.arraycopy(m_sDeviceIP.getBytes(), 0, m_strLoginInfo.sDeviceAddress, 0, m_sDeviceIP.length());

        String m_sUsername = user;//设备用户名
        m_strLoginInfo.sUserName = new byte[HCNetSDK.NET_DVR_LOGIN_USERNAME_MAX_LEN];
        System.arraycopy(m_sUsername.getBytes(), 0, m_strLoginInfo.sUserName, 0, m_sUsername.length());

        String m_sPassword = psw;//设备密码
        m_strLoginInfo.sPassword = new byte[HCNetSDK.NET_DVR_LOGIN_PASSWD_MAX_LEN];
        System.arraycopy(m_sPassword.getBytes(), 0, m_strLoginInfo.sPassword, 0, m_sPassword.length());

        m_strLoginInfo.wPort = port;
        m_strLoginInfo.bUseAsynLogin = false; //是否异步登录：0- 否，1- 是
//        m_strLoginInfo.byLoginMode=0;  //ISAPI登录
        m_strLoginInfo.write();

        lUserId =  hCNetSDK.NET_DVR_Login_V40(m_strLoginInfo, m_strDeviceInfo);
        DeviceLoginDto deviceLoginDto = new DeviceLoginDto();
        deviceLoginDto.setLUserId(lUserId);

        if (lUserId== -1) {
            int errorCode = hCNetSDK.NET_DVR_GetLastError();
            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"sdk登陆失败",m_strLoginInfo,hCNetSDK.NET_DVR_GetLastError());
            deviceLoginDto.setErrorCode(errorCode);
            return deviceLoginDto;
        }
        deviceLoginDto.setDeviceinfoV40(m_strDeviceInfo);
        return  deviceLoginDto;

    }


    @Override
    public DeviceLoginOutDto logout(int lUserId) {
        boolean b = hCNetSDK.NET_DVR_Logout(lUserId);
        DeviceLoginOutDto deviceLoginOutDto = new DeviceLoginOutDto();
        deviceLoginOutDto.setResult(b);
        if(!b){
            int errorCode = hCNetSDK.NET_DVR_GetLastError();
            deviceLoginOutDto.setErrorCode(errorCode);
        }
        return deviceLoginOutDto;
    }

    @Override
    public DeviceConfigDto deviceConfig(int lUserId) {
        IntByReference ibrBytesReturned = new IntByReference(0);//获取IP接入配置参数
        HCNetSDK.NET_DVR_DEVICECFG_V40 devicecfgV40 = new HCNetSDK.NET_DVR_DEVICECFG_V40();
        devicecfgV40.write();
        //lpIpParaConfig 接收数据的缓冲指针
        Pointer lpIpParaConfig = devicecfgV40.getPointer();
        boolean bRet = hCNetSDK.NET_DVR_GetDVRConfig(lUserId, HCNetSDK.NET_DVR_GET_DEVICECFG_V40, 0, lpIpParaConfig, devicecfgV40.size(), ibrBytesReturned);
        devicecfgV40.read();
        DeviceConfigDto deviceConfigDto = new DeviceConfigDto();
        if (bRet) {
            deviceConfigDto.setDevicecfgV40(devicecfgV40);
        } else {
            int errorCode = hCNetSDK.NET_DVR_GetLastError();
            deviceConfigDto.setErrorCode(errorCode);
            //失败
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "设备配置获取失败", lUserId, hCNetSDK.NET_DVR_GetLastError());
        }
        //字符编码类型（SDK所有接口返回的字符串编码类型，透传接口除外）：0- 无字符编码信息(老设备)，1- GB2312(简体中文)，2- GBK，3- BIG5(繁体中文)，4- Shift_JIS(日文)，5- EUC-KR(韩文)，6- UTF-8，7- ISO8859-1，8- ISO8859-2，9- ISO8859-3，…，依次类推，21- ISO8859-15(西欧)
        return deviceConfigDto;
    }

    @Override
    public ChannelInfoDto getIpcChannelList(int lUserId, HCNetSDK.NET_DVR_DEVICECFG_V40 devicecfgV40) {
        int total = devicecfgV40.byChanNum;
        int channel = devicecfgV40.byStartChan;
        ArrayList<DeviceChannelEntity> deviceChannelList = new ArrayList<>();
        for (int i = 0; i < total; ++i, ++channel)
        {
            IntByReference ibrBytesReturned = new IntByReference(0);//获取IP接入配置参数
            boolean bRet;
            HCNetSDK.NET_DVR_PICCFG_V30 picInfo = new HCNetSDK.NET_DVR_PICCFG_V30();
            picInfo.write();

            Pointer lpIpParaConfig = picInfo.getPointer();
            bRet = hCNetSDK.NET_DVR_GetDVRConfig(lUserId, HCNetSDK.NET_DVR_GET_PICCFG_V30, channel, lpIpParaConfig, picInfo.size(), ibrBytesReturned);
            if (!bRet)
            {
                long lRet = hCNetSDK.NET_DVR_GetLastError();
                log.error(LogTemplate.ERROR_LOG_TEMPLATE, "设备配置获取失败", lUserId, hCNetSDK.NET_DVR_GetLastError());
                continue;
            }

            DeviceChannelEntity deviceChannel = new DeviceChannelEntity();
            String sChanName = StringUtils.getGbkStringFromByte(picInfo.sChanName);
            deviceChannel.setChannelName(sChanName);
            deviceChannel.setManufacturer(MarkConstant.HIK_MANUFACTURER);
            deviceChannel.setPtzType(0);
            deviceChannel.setIsIpChannel(0);
            deviceChannel.setChannelType(1);
            deviceChannel.setOnline(1);
            deviceChannel.setChannelNum(channel);
            deviceChannelList.add(deviceChannel);
        }
        ChannelInfoDto channelInfoDto = new ChannelInfoDto();
        channelInfoDto.setChannelList(deviceChannelList);
        return channelInfoDto;
    }

    @Override
    public ChannelInfoDto getDvrChannelList(int lUserId, HCNetSDK.NET_DVR_DEVICECFG_V40 devicecfgV40) {
        //模拟加ip通道
        ChannelInfoDto ipcChannelList = getIpcChannelList(lUserId, devicecfgV40);
        ChannelInfoDto nvrChannelList = getNvrChannelList(lUserId, devicecfgV40);

        ArrayList<DeviceChannelEntity> deviceChannelEntities = new ArrayList<>();
        deviceChannelEntities.addAll(ipcChannelList.getChannelList());
        deviceChannelEntities.addAll(nvrChannelList.getChannelList());

        ChannelInfoDto channelInfoDto = new ChannelInfoDto();
        channelInfoDto.setChannelList(deviceChannelEntities);

        return channelInfoDto;
    }

    @Override
    public ChannelInfoDto getNvrChannelList(int lUserId, HCNetSDK.NET_DVR_DEVICECFG_V40 devicecfgV40) {
        //仅处理ip通道
        IntByReference ibrBytesReturned = new IntByReference(0);//获取IP接入配置参数
        boolean bRet;

        HCNetSDK.NET_DVR_IPPARACFG_V40 m_strIpparaCfg = new HCNetSDK.NET_DVR_IPPARACFG_V40();
        m_strIpparaCfg.write();
        //lpIpParaConfig 接收数据的缓冲指针
        Pointer lpIpParaConfig = m_strIpparaCfg.getPointer();
        bRet = hCNetSDK.NET_DVR_GetDVRConfig(lUserId, HCNetSDK.NET_DVR_GET_IPPARACFG_V40, 0, lpIpParaConfig, m_strIpparaCfg.size(), ibrBytesReturned);
        m_strIpparaCfg.read();

        if(!bRet){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "设备通道配置取失败", lUserId, hCNetSDK.NET_DVR_GetLastError());
            return  null;
        }
        //总组数
        int iGroupNum = m_strIpparaCfg.dwGroupNum;
        //数字通道个数
        int dwDChanNum = m_strIpparaCfg.dwDChanNum;
        //起始数字通道
        int dwStartDChan = m_strIpparaCfg.dwStartDChan;

        int count = 0;
        int currentChannel = dwStartDChan;

        ArrayList<DeviceChannelEntity> deviceChannelList = new ArrayList<>();
        for (int i = 0; i < iGroupNum; ++i)
        {


            HCNetSDK.NET_DVR_IPPARACFG_V40 m_strIpparaCfgi = new HCNetSDK.NET_DVR_IPPARACFG_V40();
            m_strIpparaCfgi.write();
            //lpIpParaConfig 接收数据的缓冲指针
            Pointer lpIpParaConfigi = m_strIpparaCfgi.getPointer();
            bRet = hCNetSDK.NET_DVR_GetDVRConfig(lUserId, HCNetSDK.NET_DVR_GET_IPPARACFG_V40, i, lpIpParaConfigi, m_strIpparaCfgi.size(), ibrBytesReturned);
            m_strIpparaCfgi.read();
            if (!bRet)
            {
                log.error(LogTemplate.ERROR_LOG_TEMPLATE, "设备通道配置取失败", lUserId, hCNetSDK.NET_DVR_GetLastError());
                return null;
            }

            //数组从0开始  下一组时时64
            int currentGroupFirstChannel = i * 64;

            //64是每一组的固定长度（根据海康SDK获得）
            for (int j = 0; j < 64 && count < dwDChanNum; ++j, ++count, ++currentChannel)
            {
                m_strIpparaCfgi.struStreamMode[j].read();
                if (0 == m_strIpparaCfgi.struStreamMode[j].uGetStream.struChanInfo.byIPID){
                    continue;
                }

                int channel = currentChannel;
                int ipcInfoIndex = m_strIpparaCfgi.struStreamMode[j].uGetStream.struChanInfo.byIPID - 1 - currentGroupFirstChannel;

                HCNetSDK.NET_DVR_PICCFG_V30 picInfo = new HCNetSDK.NET_DVR_PICCFG_V30();
                picInfo.write();

                Pointer picInfoConfig = picInfo.getPointer();
                bRet = hCNetSDK.NET_DVR_GetDVRConfig(lUserId, HCNetSDK.NET_DVR_GET_PICCFG_V30, channel, picInfoConfig, picInfo.size(), ibrBytesReturned);
                picInfo.read();
                if (!bRet)
                {
                    long lRet = hCNetSDK.NET_DVR_GetLastError();
                    log.error(LogTemplate.ERROR_LOG_TEMPLATE, "设备配置获取失败", lUserId, hCNetSDK.NET_DVR_GetLastError());
                    continue;
                }
                DeviceChannelEntity deviceChannel = new DeviceChannelEntity();

                String sChanName = StringUtils.getGbkStringFromByte(picInfo.sChanName);
                String name = sChanName;
                deviceChannel.setChannelName(name);
                deviceChannel.setManufacturer(MarkConstant.HIK_MANUFACTURER);
                deviceChannel.setPtzType(0);
                deviceChannel.setIsIpChannel(1);
                deviceChannel.setChannelType(1);
                deviceChannel.setChannelNum(channel);
                deviceChannel.setIp(new String(m_strIpparaCfgi.struIPDevInfo[ipcInfoIndex].struIP.sIpV4).trim());
                deviceChannel.setPort(m_strIpparaCfgi.struIPDevInfo[ipcInfoIndex].wDVRPort);
                deviceChannel.setPassword(new String(m_strIpparaCfgi.struIPDevInfo[ipcInfoIndex].sPassword).trim());
                deviceChannel.setOnline(m_strIpparaCfgi.struStreamMode[ipcInfoIndex].uGetStream.struChanInfo.byEnable);
                deviceChannelList.add(deviceChannel);
            }

        }
        ChannelInfoDto channelInfoDto = new ChannelInfoDto();
        channelInfoDto.setChannelList(deviceChannelList);
        return channelInfoDto;
    }

    @Override
    public PlayInfoDto play(int lUserId, int channelNum, int dwStreamType, int dwLinkMode,SocketPointer socketPointer) {

        HCNetSDK.NET_DVR_PREVIEWINFO strClientInfo = new HCNetSDK.NET_DVR_PREVIEWINFO();
        strClientInfo.read();
        //通道号
        strClientInfo.lChannel = channelNum;
        strClientInfo.hPlayWnd = 0;
        //0-主码流，1-子码流，2-三码流，3-虚拟码流，以此类推
        strClientInfo.dwStreamType= dwStreamType;
        //连接方式：0- TCP方式，1- UDP方式，2- 多播方式，3- RTP方式，4- RTP/RTSP，5- RTP/HTTP，6- HRUDP（可靠传输） ，7- RTSP/HTTPS，8- NPQ
        strClientInfo.dwLinkMode=4;
        strClientInfo.bBlocked=1;
        strClientInfo.write();

        Pointer pointer = socketPointer.getPointer();
        socketPointer.write();

        lPreviewHandle = hCNetSDK.NET_DVR_RealPlay_V40(lUserId, strClientInfo, fRealDataCallBack , pointer);
        PlayInfoDto playInfoDto = new PlayInfoDto();
        playInfoDto.setLPreviewHandle(lPreviewHandle);
        if(lPreviewHandle <= -1){
            //预览失败
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "点播失败", lUserId, hCNetSDK.NET_DVR_GetLastError());
            playInfoDto.setErrorCode(hCNetSDK.NET_DVR_GetLastError());
            return playInfoDto;
        }
        return playInfoDto;
    }

    @Override
    public int playStandardCallBack(int lPreviewHandle) {
        int errorCode = 0;

        if(!hCNetSDK.NET_DVR_SetStandardDataCallBack(lPreviewHandle, standardRealDataCallBack, 0)){
            //回调数据失败
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "点播失败", "点播回调失败", hCNetSDK.NET_DVR_GetLastError());
            errorCode = hCNetSDK.NET_DVR_GetLastError();
        }

        return errorCode;
    }

    @Override
    public PlayInfoDto stopPlay(int lPreviewHandle) {
        boolean b = hCNetSDK.NET_DVR_StopRealPlay(lPreviewHandle);
        PlayInfoDto playInfoDto = new PlayInfoDto();
        playInfoDto.setLPreviewHandle(lPreviewHandle);
        int errorCode = b?0: hCNetSDK.NET_DVR_GetLastError();
        playInfoDto.setErrorCode(errorCode);
        return playInfoDto;
    }

    @Override
    public RecordInfoDto recordList(int lUserId, int lChannel, String startTime, String endTime) {
        RecordInfoDto recordInfoDto = new RecordInfoDto();
        HCNetSDK.NET_DVR_FILECOND_V40 struFileCond = new HCNetSDK.NET_DVR_FILECOND_V40();
        struFileCond.read();
        struFileCond.lChannel= lChannel; //通道号 NVR设备路数小于32路的起始通道号从33开始，依次增加
        struFileCond.byFindType=0;  //录象文件类型 0=定时录像
        //起始时间
        struFileCond.struStartTime.dwYear= DateUtils.stringToYear(startTime);
        struFileCond.struStartTime.dwMonth=DateUtils.stringToMonth(startTime);
        struFileCond.struStartTime.dwDay=DateUtils.stringToDay(startTime);
        struFileCond.struStartTime.dwHour=DateUtils.stringToHour(startTime);
        struFileCond.struStartTime.dwMinute=DateUtils.stringToMinuts(startTime);
        struFileCond.struStartTime.dwSecond=DateUtils.stringToSeconds(startTime);
        //停止时间
        struFileCond.struStopTime.dwYear=DateUtils.stringToYear(endTime);
        struFileCond.struStopTime.dwMonth=DateUtils.stringToMonth(endTime);
        struFileCond.struStopTime.dwDay=DateUtils.stringToDay(endTime);
        struFileCond.struStopTime.dwHour=DateUtils.stringToHour(endTime);
        struFileCond.struStopTime.dwMinute=DateUtils.stringToMinuts(endTime);
        struFileCond.struStopTime.dwSecond=DateUtils.stringToSeconds(endTime);
        struFileCond.write();
        int  FindFileHandle=hCNetSDK.NET_DVR_FindFile_V40(lUserId,struFileCond);
        if (FindFileHandle<=-1)
        {
            int errorCode = hCNetSDK.NET_DVR_GetLastError();
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "录像查找失败", "录像查找失败", errorCode);
            recordInfoDto.setErrorCode(errorCode);
            return recordInfoDto;
        }

        List<HCNetSDK.NET_DVR_FINDDATA_V40> netDvrFindDataV40List = new ArrayList<>();

        ArrayList<RecordItem> recordItems = new ArrayList<>();
        RecordAllItem recordAllItem = new RecordAllItem();
        int sum = 0;
        while (true) {
            HCNetSDK.NET_DVR_FINDDATA_V40 struFindData = new HCNetSDK.NET_DVR_FINDDATA_V40();
            long State = hCNetSDK.NET_DVR_FindNextFile_V40(FindFileHandle, struFindData);
            if (State <= -1) {
                log.error(LogTemplate.ERROR_LOG_TEMPLATE, "查找失败，错误码为", "录像查找失败", hCNetSDK.NET_DVR_GetLastError());
                break;

            }else if (State==1000){
                sum++;
                //获取文件信息成功
                struFindData.read();
                RecordItem recordItem = new RecordItem();
                String strFileName= StringUtils.getUtf8StringFromByte(struFindData.sFileName);
                //起始时间
                String struStartTime = struFindData.struStartTime.toStringStandardTime();
                String struStopTime = struFindData.struStopTime.toStringStandardTime();
                recordItem.setName(strFileName);
                recordItem.setStartTime(struStartTime);
                recordItem.setEndTime(struStopTime);
                recordItem.setFileSize(String.valueOf(struFindData.dwFileSize));
                recordItems.add(recordItem);
                continue;

            }else if (State==1001) {
                //未查找到文件
                System.out.println("未查找到文件");
                break;

            }
            else if (State==1002) {
                //正在查找请等待
                System.out.println("正在查找，请等待");
                continue;

            }

            else if (State==1003)
            {
                //没有更多的文件，查找结束
                System.out.println("没有更多的文件，查找结束");
                break;

            }
            else if (State==1004)
            {
                //查找文件时异常
                System.out.println("没有更多的文件，查找结束");
                break;

            }
            else if (State==1005)
            {
                //查找文件超时

                System.out.println("没有更多的文件，查找结束");
                break;

            }else {
                break;
            }
        }

        recordAllItem.setSumNum(sum);
        recordAllItem.setRecordList(recordItems);
        recordAllItem.setName("");
        boolean b_CloseHandle=hCNetSDK.NET_DVR_FindClose_V30(FindFileHandle);
        if (!b_CloseHandle) {
            int errorCode = hCNetSDK.NET_DVR_GetLastError();
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "录像关闭失败", "录像查找失败", errorCode);
            recordInfoDto.setErrorCode(errorCode);
            return recordInfoDto;
        }
        recordInfoDto.setRecordAllItem(recordAllItem);

        return recordInfoDto;
    }

    @Override
    public PlayInfoDto playBack(int lUserId, int channelNum, String startTime, String endTime, SocketPointer socketPointer) {
        HCNetSDK.NET_DVR_VOD_PARA net_dvr_vod_para = new HCNetSDK.NET_DVR_VOD_PARA();
        net_dvr_vod_para.dwSize=net_dvr_vod_para.size();
        //通道号
        net_dvr_vod_para.struIDInfo.dwChannel=channelNum;
        //起始时间
        net_dvr_vod_para.struBeginTime.dwYear= DateUtils.stringToYear(startTime);
        net_dvr_vod_para.struBeginTime.dwMonth=DateUtils.stringToMonth(startTime);
        net_dvr_vod_para.struBeginTime.dwDay=DateUtils.stringToDay(startTime);
        net_dvr_vod_para.struBeginTime.dwHour=DateUtils.stringToHour(startTime);
        net_dvr_vod_para.struBeginTime.dwMinute=DateUtils.stringToMinuts(startTime);
        net_dvr_vod_para.struBeginTime.dwSecond=DateUtils.stringToSeconds(startTime);
        //停止时间
        net_dvr_vod_para.struEndTime.dwYear=DateUtils.stringToYear(endTime);
        net_dvr_vod_para.struEndTime.dwMonth=DateUtils.stringToMonth(endTime);
        net_dvr_vod_para.struEndTime.dwDay=DateUtils.stringToDay(endTime);
        net_dvr_vod_para.struEndTime.dwHour=DateUtils.stringToHour(endTime);
        net_dvr_vod_para.struEndTime.dwMinute=DateUtils.stringToMinuts(endTime);
        net_dvr_vod_para.struEndTime.dwSecond=DateUtils.stringToSeconds(endTime);
        net_dvr_vod_para.hWnd=null; // 回放的窗口句柄，若置为空，SDK仍能收到码流数据，但不解码显示
        net_dvr_vod_para.write();

        int iPlayBack=hCNetSDK.NET_DVR_PlayBackByTime_V40(lUserId,net_dvr_vod_para);
        PlayInfoDto playInfoDto = new PlayInfoDto();
        playInfoDto.setLPreviewHandle(iPlayBack);
        if (iPlayBack<=-1)
        {
            int errorCode = hCNetSDK.NET_DVR_GetLastError();
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "录像回放", "按时间回放失败", errorCode);
            playInfoDto.setErrorCode(errorCode);
            return playInfoDto;
        }

        //开启取流
        boolean bCrtl=hCNetSDK.NET_DVR_PlayBackControl(iPlayBack, HCNetSDK.NET_DVR_PLAYSTART, 0, null);
        if(!bCrtl){
            int errorCode = hCNetSDK.NET_DVR_GetLastError();
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "录像回放", "开始播放失败", errorCode);
            playInfoDto.setErrorCode(errorCode);
            return playInfoDto;
        }
        //设置回调
        Pointer pointer = socketPointer.getPointer();
        socketPointer.write();
        boolean bRet=hCNetSDK.NET_DVR_SetPlayDataCallBack_V40(iPlayBack,playBackCallBack,pointer);
        if(!bRet){
            int errorCode = hCNetSDK.NET_DVR_GetLastError();
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "录像回放", "设置回调失败", errorCode);
            playInfoDto.setErrorCode(errorCode);
            return playInfoDto;
        }
        return playInfoDto;

    }

    @Override
    public Integer ptzControl(int lUserId, int lChannel, int dwPTZCommand, int dwStop,int dwSpeed) {

        //dwSpeed 取值1至7
        boolean b = hCNetSDK.NET_DVR_PTZControlWithSpeed_Other(lUserId, lChannel, dwPTZCommand, dwStop,dwSpeed);
        if(!b){
            int error = hCNetSDK.NET_DVR_GetLastError();
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "ptz操作", "ptz操作失败", hCNetSDK.NET_DVR_GetLastError());
            return error;
        }

        return 0;
    }

    @Override
    public PresetQueryDto presetList(int lUserId, int lChannel) {
        PresetQueryDto presetQueryDto = new PresetQueryDto();

        IntByReference ibrBytesReturned = new IntByReference(0);//获取IP接入配置参数
        HCNetSDK.NET_DVR_PRESET_NAME presetObj = new HCNetSDK.NET_DVR_PRESET_NAME();
        presetObj.write();

        boolean bRet = hCNetSDK.NET_DVR_GetDVRConfig(lUserId, HCNetSDK.NET_DVR_GET_PRESET_NAME, lChannel, presetObj.getPointer(), presetObj.size(), ibrBytesReturned);
        presetObj.read();
        if(!bRet){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "预置位操作", "预置位操作失败", hCNetSDK.NET_DVR_GetLastError());
        }
        short wPresetNum = presetObj.wPresetNum;


        return presetQueryDto;
    }

    @Override
    public Integer presetControl(int lUserId, int lChannel, int commond, int presetNum) {


        boolean b = hCNetSDK.NET_DVR_PTZPreset_Other(lUserId, lChannel, commond, presetNum);

        if(!b){
            int error = hCNetSDK.NET_DVR_GetLastError();
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "预置位操作", "预置位操作失败", hCNetSDK.NET_DVR_GetLastError());
            return error;
        }

        return 0;
    }

    @Override
    public Integer Zoom3DControl(int lUserId, int lChannel, int xTop, int yTop,int xBottom,int yBottom,int dragType) {

        HCNetSDK.NET_DVR_POINT_FRAME struPtzPos;
        //放大-1 缩小-2
        if(dragType == 1){
            // 开始放大
            struPtzPos = new HCNetSDK.NET_DVR_POINT_FRAME();
            struPtzPos.xTop = xTop;
            struPtzPos.yTop = yTop;
            struPtzPos.xBottom = xBottom;
            struPtzPos.yBottom = yBottom;
        }else {
            //缩小
            struPtzPos = new HCNetSDK.NET_DVR_POINT_FRAME();
        }


        boolean bRet = hCNetSDK.NET_DVR_PTZSelZoomIn_EX(lUserId, lChannel,struPtzPos);
        if (!bRet)
        {
            int error = hCNetSDK.NET_DVR_GetLastError();
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "预置位操作", "预置位操作失败", hCNetSDK.NET_DVR_GetLastError());
            return error;
        }


        return 0;
    }

    @Override
    public Integer playBackControl(int lPlayHandle, int dwControlCode, int value) {
        Boolean res = false;

        if(dwControlCode == HCNetSDK.NET_DVR_PLAYPAUSE){
            //暂停
            res = hCNetSDK.NET_DVR_PlayBackControl(lPlayHandle, HCNetSDK.NET_DVR_PLAYPAUSE, 0,null);
        }else if(dwControlCode == HCNetSDK.NET_DVR_PLAYRESTART){
            //恢复
            res = hCNetSDK.NET_DVR_PlayBackControl(lPlayHandle, HCNetSDK.NET_DVR_PLAYRESTART, 0,null);
        }else if(dwControlCode == HCNetSDK.NET_DVR_PLAYFAST){
            //快放
            res = hCNetSDK.NET_DVR_PlayBackControl(lPlayHandle, HCNetSDK.NET_DVR_PLAYFAST, value,null);
        }else if(dwControlCode == HCNetSDK.NET_DVR_PLAYNORMAL){

            //正常放
            res = hCNetSDK.NET_DVR_PlayBackControl(lPlayHandle, HCNetSDK.NET_DVR_PLAYNORMAL, 0,null);
        }

        if(!res){
            int error = hCNetSDK.NET_DVR_GetLastError();
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "回放控制操作", "回放控制操作，类型："+dwControlCode, error);
            return error;
        }
        return 0;
    }
}
