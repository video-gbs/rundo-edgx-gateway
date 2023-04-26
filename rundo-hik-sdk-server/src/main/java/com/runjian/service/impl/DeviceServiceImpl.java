package com.runjian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.runjian.common.constant.LogTemplate;
import com.runjian.domain.dto.Device;
import com.runjian.entity.DeviceEntity;
import com.runjian.hik.module.service.SdkInitService;
import com.runjian.hik.sdklib.HCNetSDK;
import com.runjian.mapper.DeviceMapper;
import com.runjian.service.IDeviceService;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Service
@Slf4j
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, DeviceEntity> implements IDeviceService {
    private static HCNetSDK hCNetSDK = SdkInitService.hCNetSDK;
    @Autowired
    DeviceMapper deviceMapper;
    
    static int lUserId = -1;//用户句柄
    static int lDChannel;  //预览通道号

    @Override
    public void online(String ip, short port, String user, String psw) {
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

        lUserId = hCNetSDK.NET_DVR_Login_V40(m_strLoginInfo, m_strDeviceInfo);
        if (lUserId== -1) {
            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"sdk登陆失败",m_strLoginInfo,hCNetSDK.NET_DVR_GetLastError());
        }

        LambdaQueryWrapper<DeviceEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DeviceEntity::getLUserId,lUserId);
        DeviceEntity one = deviceMapper.selectOne(queryWrapper);
        DeviceEntity deviceEntity = new DeviceEntity();
        deviceEntity.setLUserId(lUserId);
        deviceEntity.setIp(ip);
        deviceEntity.setPort(port);
        deviceEntity.setOnline(lUserId<0?0:1);
        deviceEntity.setPassword(psw);
        deviceEntity.setUserName(user);

        if(ObjectUtils.isEmpty(one)){

            deviceMapper.insert(deviceEntity);
        }else {
            LambdaQueryWrapper<DeviceEntity> updateWrapper = new LambdaQueryWrapper<>();
            updateWrapper.eq(DeviceEntity::getLUserId,lUserId);
            deviceMapper.update(deviceEntity,updateWrapper);
        }

        deviceInfo(lUserId);
        nvrDeviceInfo(lUserId);
    }

    @Override
    public void offline(int lUserId) {
        boolean b = hCNetSDK.NET_DVR_Logout(lUserId);
        if(b){
            LambdaQueryWrapper<DeviceEntity> updateWrapper = new LambdaQueryWrapper<>();
            updateWrapper.eq(DeviceEntity::getLUserId,lUserId);
            DeviceEntity deviceEntity = new DeviceEntity();
            deviceEntity.setOnline(0);
            deviceMapper.update(deviceEntity,updateWrapper);
        }else {
            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"sdk注销失败",lUserId,hCNetSDK.NET_DVR_GetLastError());
        }

    }

    @Override
    public void deviceInfo(int lUserId) {
        IntByReference ibrBytesReturned = new IntByReference(0);//获取IP接入配置参数
        HCNetSDK.NET_DVR_DEVICECFG_V40 devicecfgV40 = new HCNetSDK.NET_DVR_DEVICECFG_V40();
        devicecfgV40.write();
        //lpIpParaConfig 接收数据的缓冲指针
        Pointer lpIpParaConfig = devicecfgV40.getPointer();
        boolean bRet = hCNetSDK.NET_DVR_GetDVRConfig(lUserId, HCNetSDK.NET_DVR_GET_DEVICECFG_V40, 0, lpIpParaConfig, devicecfgV40.size(), ibrBytesReturned);
        devicecfgV40.read();
        if(bRet){
            String name = new String(devicecfgV40.sDVRName).trim();
            String serialNumber = new String(devicecfgV40.sSerialNumber).trim();
            DeviceEntity deviceEntity = new DeviceEntity();
            deviceEntity.setName(name);
            deviceEntity.setSerialNumber(serialNumber);
            LambdaQueryWrapper<DeviceEntity> updateWrapper = new LambdaQueryWrapper<>();
            updateWrapper.eq(DeviceEntity::getLUserId,lUserId);
            deviceMapper.update(deviceEntity,updateWrapper);
        }else {
            //失败
            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"设备信息获取失败",lUserId,hCNetSDK.NET_DVR_GetLastError());
        }



    }

    @Override
    public void nvrDeviceInfo(int lUserId) {
        IntByReference ibrBytesReturned = new IntByReference(0);//获取IP接入配置参数
        HCNetSDK.NET_DVR_IPPARACFG_V40 devicecfgV40 = new HCNetSDK.NET_DVR_IPPARACFG_V40();
        devicecfgV40.write();
        //lpIpParaConfig 接收数据的缓冲指针
        Pointer lpIpParaConfig = devicecfgV40.getPointer();
        boolean bRet = hCNetSDK.NET_DVR_GetDVRConfig(lUserId, HCNetSDK.NET_DVR_GET_IPPARACFG_V40, 0, lpIpParaConfig, devicecfgV40.size(), ibrBytesReturned);
        devicecfgV40.read();
        //lpIpParaConfig 接收数据的缓冲指针
//        Pointer lpIpParaConfig = m_strIpparaCfg.getPointer();
//        bRet = hCNetSDK.NET_DVR_GetDVRConfig(lUserId, HCNetSDK.NET_DVR_GET_IPPARACFG_V40, i, lpIpParaConfig, m_strIpparaCfg.size(), ibrBytesReturned);
//        m_strIpparaCfg.read();
    }
}
