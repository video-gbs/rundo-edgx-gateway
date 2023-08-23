package com.runjian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.exception.BusinessException;
import com.runjian.common.config.response.CommonResponse;
import com.runjian.common.constant.GatewayBusinessMsgType;
import com.runjian.common.constant.LogTemplate;
import com.runjian.common.constant.MarkConstant;
import com.runjian.conf.constant.DeviceTypeEnum;
import com.runjian.domain.dto.DeviceChannel;
import com.runjian.domain.dto.commder.*;
import com.runjian.entity.DeviceChannelEntity;
import com.runjian.entity.DeviceEntity;
import com.runjian.hik.module.service.ISdkCommderService;
import com.runjian.hik.module.util.DeviceUtils;
import com.runjian.hik.sdklib.HCNetSDK;
import com.runjian.mapper.DeviceChannelMapper;
import com.runjian.mapper.DeviceMapper;
import com.runjian.mq.gatewayBusiness.asyncSender.GatewayBusinessAsyncSender;
import com.runjian.service.IDeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, DeviceEntity> implements IDeviceService {
    private static HCNetSDK hCNetSDK ;
    @Autowired
    ISdkCommderService iSdkCommderService;
    @Resource
    DeviceMapper deviceMapper;


    @Resource
    DeviceChannelMapper deviceChannelMapper;
    static int lDChannel;  //预览通道号
    @Autowired
    GatewayBusinessAsyncSender gatewayBusinessAsyncSender;

    @Override
    public DeviceOnlineDto online(String ip, short port, String user, String psw) {

        DeviceLoginDto login = iSdkCommderService.login(ip, port, user, psw);
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

        if(!ObjectUtils.isEmpty(deviceinfoV40)){
            deviceEntity.setCharset(DeviceUtils.getCharset(deviceinfoV40.byCharEncodeType));
        }
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
    public DeviceEntity getOne(Long id) {
        return deviceMapper.selectById(id);
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

        if(!ObjectUtils.isEmpty(deviceinfoV40)){
            deviceEntity.setCharset(DeviceUtils.getCharset(deviceinfoV40.byCharEncodeType));
        }
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
    public Boolean offline(long encodeId) {
        //查找对应的登陆luserid
        DeviceEntity deviceEntityOne = deviceMapper.selectById(encodeId);
        if(ObjectUtils.isEmpty(deviceEntityOne)){
            throw new BusinessException(BusinessErrorEnums.DB_NOT_FOUND);
        }
        DeviceLoginDto login = iSdkCommderService.login(deviceEntityOne.getIp(), deviceEntityOne.getPort(), deviceEntityOne.getUsername(), deviceEntityOne.getPassword());
        if(login.getErrorCode() != 0){
            //登陆失败
//            throw new BusinessException(BusinessErrorEnums.SDK_OPERATION_FAILURE);
            return false;
        }
        int lUserId = login.getLUserId();

        DeviceLoginOutDto logout = iSdkCommderService.logout(lUserId);
        boolean result = logout.isResult();
        if(result){
            LambdaQueryWrapper<DeviceEntity> updateWrapper = new LambdaQueryWrapper<>();
            updateWrapper.eq(DeviceEntity::getId,deviceEntityOne.getId());
            DeviceEntity deviceEntity = new DeviceEntity();
            deviceEntity.setOnline(0);
            deviceMapper.update(deviceEntity,updateWrapper);
        }else {
            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"sdk注销失败",lUserId,hCNetSDK.NET_DVR_GetLastError());
            return false;
        }
        return true;
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
    public List<DeviceEntity> deviceList() {
        LambdaQueryWrapper<DeviceEntity> deviceEntityLambdaQueryWrapper = new LambdaQueryWrapper<>();
        deviceEntityLambdaQueryWrapper.eq(DeviceEntity::getDeleted,0);
        return deviceMapper.selectList(deviceEntityLambdaQueryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deviceDelete(long encodeId) {

        //可以删除
        Boolean offline = offline(encodeId);
        if(offline){
            //退出正常 进行设备和通道的删除
            deviceMapper.deleteById(encodeId);

            LambdaQueryWrapper<DeviceChannelEntity> deviceChannelEntityLambdaQueryWrapper = new LambdaQueryWrapper<>();
            deviceChannelEntityLambdaQueryWrapper.eq(DeviceChannelEntity::getEncodeId,encodeId);
            deviceChannelMapper.delete(deviceChannelEntityLambdaQueryWrapper);

        }
    }

    @Override
    public void deviceDeleteRecover(long encodeId) {
        //可以删除
        DeviceEntity deviceEntity = new DeviceEntity();
        deviceEntity.setId(encodeId);
        deviceEntity.setDeleted(0);
        deviceMapper.updateById(deviceEntity);

        DeviceChannelEntity deviceChannel = new DeviceChannelEntity();
        deviceChannel.setDeleted(0);
        LambdaQueryWrapper<DeviceChannelEntity> deviceChannelEntityLambdaQueryWrapper = new LambdaQueryWrapper<>();
        deviceChannelEntityLambdaQueryWrapper.eq(DeviceChannelEntity::getEncodeId,encodeId);
        deviceChannelMapper.update(deviceChannel,deviceChannelEntityLambdaQueryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deviceSoftDelete(long encodeId) {
        //可以删除
        DeviceEntity deviceEntity = new DeviceEntity();
        deviceEntity.setId(encodeId);
        deviceEntity.setDeleted(1);
        deviceMapper.updateById(deviceEntity);

        DeviceChannelEntity deviceChannel = new DeviceChannelEntity();
        deviceChannel.setDeleted(1);
        LambdaQueryWrapper<DeviceChannelEntity> deviceChannelEntityLambdaQueryWrapper = new LambdaQueryWrapper<>();
        deviceChannelEntityLambdaQueryWrapper.eq(DeviceChannelEntity::getEncodeId,encodeId);
        deviceChannelMapper.update(deviceChannel,deviceChannelEntityLambdaQueryWrapper);
    }


    private void deviceStatusMsg(DeviceEntity deviceEntity,int errorCode){
        if(errorCode!=0){
            deviceEntity.setOnline(0);
            deviceMapper.updateById(deviceEntity);

            //通道下线
            DeviceChannelEntity deviceChannel = new DeviceChannelEntity();
            deviceChannel.setOnline(0);
            LambdaQueryWrapper<DeviceChannelEntity> deviceChannelEntityLambdaQueryWrapper = new LambdaQueryWrapper<>();
            deviceChannelEntityLambdaQueryWrapper.eq(DeviceChannelEntity::getEncodeId,deviceEntity.getId());
            deviceChannelMapper.update(deviceChannel,deviceChannelEntityLambdaQueryWrapper);
        }else {
            deviceEntity.setOnline(1);
            deviceMapper.updateById(deviceEntity);
        }
        CommonResponse<DeviceEntity> success = CommonResponse.success(deviceEntity);
        gatewayBusinessAsyncSender.sendforAllScene(success, null, GatewayBusinessMsgType.REGISTER);
    }

    @Async("taskExecutor")
    @Override
    public void checkDeviceStatus(DeviceEntity deviceEntity) {
        //登陆
        DeviceLoginDto login = iSdkCommderService.login(deviceEntity.getIp(), deviceEntity.getPort(), deviceEntity.getUsername(), deviceEntity.getPassword());
        if(login.getErrorCode()!=0){
            //
            deviceStatusMsg(deviceEntity,login.getErrorCode());
            return;
        }
        String loginHandle = deviceEntity.getIp()+":"+deviceEntity.getPort();
        Integer errorCode = iSdkCommderService.remoteControl(login.getLUserId(), HCNetSDK.NET_DVR_CHECK_USER_STATUS, loginHandle);
        deviceStatusMsg(deviceEntity,errorCode);


    }
}
