package com.runjian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.CommonResponse;
import com.runjian.common.constant.LogTemplate;
import com.runjian.common.constant.MarkConstant;
import com.runjian.conf.constant.DeviceTypeEnum;
import com.runjian.domain.dto.commder.ChannelInfoDto;
import com.runjian.domain.dto.commder.DeviceConfigDto;
import com.runjian.domain.dto.commder.DeviceLoginDto;
import com.runjian.domain.dto.commder.DeviceOnlineDto;
import com.runjian.entity.DeviceEntity;
import com.runjian.hik.module.service.ISdkCommderService;
import com.runjian.hik.module.service.SdkInitService;
import com.runjian.hik.module.util.DeviceUtils;
import com.runjian.hik.sdklib.HCNetSDK;
import com.runjian.mapper.DeviceChannelMapper;
import com.runjian.mapper.DeviceMapper;
import com.runjian.service.IDeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Service
@Slf4j
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, DeviceEntity> implements IDeviceService {
    private static HCNetSDK hCNetSDK ;
    @Autowired
    ISdkCommderService iSdkCommderService;
    @Autowired
    DeviceMapper deviceMapper;


    @Autowired
    DeviceChannelMapper deviceChannelMapper;
    static int lDChannel;  //预览通道号

    @Override
    public DeviceOnlineDto online(String ip, short port, String user, String psw) {

        DeviceLoginDto login = iSdkCommderService.login(ip, port, user, psw);
        if(login.getErrorCode() != 0){
            //登陆失败
            return  null;
        }
        int lUserId = login.getLUserId();
        LambdaQueryWrapper<DeviceEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DeviceEntity::getIp,ip);
        queryWrapper.eq(DeviceEntity::getPort,port);
        DeviceEntity one = deviceMapper.selectOne(queryWrapper);
        DeviceEntity deviceEntity = new DeviceEntity();
        deviceEntity.setLUserId(lUserId);
        deviceEntity.setIp(ip);
        deviceEntity.setPort(port);
        deviceEntity.setOnline(lUserId<0?0:1);
        deviceEntity.setPassword(psw);
        deviceEntity.setUsername(user);
        deviceEntity.setManufacturer(MarkConstant.HIK_MANUFACTURER);
        HCNetSDK.NET_DVR_DEVICEINFO_V40 deviceinfoV40 = login.getDeviceinfoV40();


        deviceEntity.setCharset(DeviceUtils.getCharset(deviceinfoV40.byCharEncodeType));
        if(ObjectUtils.isEmpty(one)){

            deviceMapper.insert(deviceEntity);
        }else {
            deviceEntity.setId(one.getId());
            deviceMapper.updateById(deviceEntity);
        }

        deviceInfo(deviceEntity,lUserId);
        DeviceOnlineDto deviceOnlineDto = new DeviceOnlineDto();
        deviceOnlineDto.setDeviceinfoV40(login.getDeviceinfoV40());
        deviceOnlineDto.setDeviceEntity(deviceEntity);
        return deviceOnlineDto;
    }


    @Override
    public CommonResponse<Long> add(String ip, short port, String user, String psw) {
        DeviceLoginDto login = iSdkCommderService.login(ip, port, user, psw);
        if(login.getErrorCode() != 0){
            //登陆失败
            return  CommonResponse.failure(BusinessErrorEnums.SDK_OPERATION_FAILURE,BusinessErrorEnums.SDK_OPERATION_FAILURE.getErrMsg()+login.getErrorCode());
        }
        int lUserId = login.getLUserId();
        LambdaQueryWrapper<DeviceEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DeviceEntity::getIp,ip);
        queryWrapper.eq(DeviceEntity::getPort,port);
        DeviceEntity one = deviceMapper.selectOne(queryWrapper);
        DeviceEntity deviceEntity = new DeviceEntity();
        deviceEntity.setLUserId(lUserId);
        deviceEntity.setIp(ip);
        deviceEntity.setPort(port);
        deviceEntity.setOnline(lUserId<0?0:1);
        deviceEntity.setPassword(psw);
        deviceEntity.setUsername(user);
        deviceEntity.setManufacturer(MarkConstant.HIK_MANUFACTURER);
        HCNetSDK.NET_DVR_DEVICEINFO_V40 deviceinfoV40 = login.getDeviceinfoV40();


        deviceEntity.setCharset(DeviceUtils.getCharset(deviceinfoV40.byCharEncodeType));
        if(ObjectUtils.isEmpty(one)){

            deviceMapper.insert(deviceEntity);
        }else {
            deviceEntity.setId(one.getId());
            deviceMapper.updateById(deviceEntity);
        }

        deviceInfo(deviceEntity,lUserId);
        DeviceOnlineDto deviceOnlineDto = new DeviceOnlineDto();
        deviceOnlineDto.setDeviceinfoV40(login.getDeviceinfoV40());
        deviceOnlineDto.setDeviceEntity(deviceEntity);
        return CommonResponse.success(deviceEntity.getId());
    }

    @Override
    public void startOnline() {
        List<DeviceEntity> deviceEntities = deviceMapper.selectList(null);
        for (DeviceEntity deviceEntity :deviceEntities){
            online(deviceEntity.getIp(),deviceEntity.getPort(),deviceEntity.getUsername(),deviceEntity.getPassword());
        }
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
    public void deviceInfo(DeviceEntity deviceEntity, int lUserId) {

        DeviceConfigDto deviceConfigDto = iSdkCommderService.deviceConfig(lUserId);
        if(deviceConfigDto.getErrorCode() != 0){
            //失败
            return;
        }
        HCNetSDK.NET_DVR_DEVICECFG_V40 devicecfgV40 = deviceConfigDto.getDevicecfgV40();
        String name = new String(devicecfgV40.sDVRName).trim();
        String serialNumber = new String(devicecfgV40.sSerialNumber).trim();
        //获取设备类型
        short wDevClass = devicecfgV40.wDevClass;
        DeviceTypeEnum deviceTypeEnum = DeviceUtils.checkDeviceType(wDevClass);
        deviceEntity.setName(name);
        deviceEntity.setSerialNumber(serialNumber);
        deviceEntity.setDeviceType(deviceTypeEnum.getCode());
        deviceMapper.updateById(deviceEntity);

    }

    @Override
    public CommonResponse<List<DeviceEntity>> deviceList() {
        return CommonResponse.success(deviceMapper.selectList(null));
    }
}
