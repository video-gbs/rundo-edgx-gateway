package com.runjian.hik.module.service.impl;

import com.runjian.common.constant.LogTemplate;
import com.runjian.common.constant.MarkConstant;
import com.runjian.common.utils.FileUtil;
import com.runjian.common.utils.StringUtils;
import com.runjian.conf.PlayHandleConf;
import com.runjian.domain.dto.DeviceChannel;
import com.runjian.domain.dto.commder.ChannelInfoDto;
import com.runjian.domain.dto.commder.DeviceConfigDto;
import com.runjian.domain.dto.commder.DeviceLoginDto;
import com.runjian.domain.dto.commder.PlayInfoDto;
import com.runjian.entity.DeviceChannelEntity;
import com.runjian.hik.module.service.ISdkCommderService;
import com.runjian.hik.module.service.SdkInitService;
//import com.runjian.hik.module.service.impl.callBack.FRealDataCallBack;
import com.runjian.hik.sdklib.HCNetSDK;
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
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;

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



    @Autowired
    private PlayHandleConf playHandleConf;

    @PostConstruct
    public void init(){
        hCNetSDK = sdkInitService.getHCNetSDK();
        standardRealDataCallBack = new StandardRealDataCallBack();
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
    public PlayInfoDto play(int lUserId, int channelNum, int dwStreamType, int dwLinkMode) {

        HCNetSDK.NET_DVR_PREVIEWINFO strClientInfo = new HCNetSDK.NET_DVR_PREVIEWINFO();
        strClientInfo.read();
        //通道号
        strClientInfo.lChannel = channelNum;
        strClientInfo.hPlayWnd = null;
        //0-主码流，1-子码流，2-三码流，3-虚拟码流，以此类推
        strClientInfo.dwStreamType= 0;
        //连接方式：0- TCP方式，1- UDP方式，2- 多播方式，3- RTP方式，4- RTP/RTSP，5- RTP/HTTP，6- HRUDP（可靠传输） ，7- RTSP/HTTPS，8- NPQ
        strClientInfo.dwLinkMode=4;
        strClientInfo.bBlocked=1;
        strClientInfo.write();
        lPreviewHandle = hCNetSDK.NET_DVR_RealPlay_V40(lUserId, strClientInfo, null , null);
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
}
