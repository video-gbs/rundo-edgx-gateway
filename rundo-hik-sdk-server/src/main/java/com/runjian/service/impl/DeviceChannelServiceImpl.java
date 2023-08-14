package com.runjian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.runjian.common.commonDto.Gateway.req.PlayReq;
import com.runjian.common.commonDto.Gateway.req.RecordInfoReq;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.exception.BusinessException;
import com.runjian.common.config.response.CommonResponse;
import com.runjian.common.constant.BusinessSceneConstants;
import com.runjian.common.constant.GatewayBusinessMsgType;
import com.runjian.common.constant.LogTemplate;
import com.runjian.conf.constant.DeviceTypeEnum;
import com.runjian.domain.dto.CatalogSyncDto;
import com.runjian.domain.dto.DeviceChannel;
import com.runjian.domain.dto.PlayCommonDto;
import com.runjian.domain.dto.commder.*;
import com.runjian.entity.DeviceChannelEntity;
import com.runjian.entity.DeviceEntity;
import com.runjian.hik.module.service.ISdkCommderService;
import com.runjian.hik.module.util.DeviceUtils;
import com.runjian.hik.sdklib.HCNetSDK;
import com.runjian.mapper.DeviceChannelMapper;
import com.runjian.mapper.DeviceMapper;
import com.runjian.service.IDeviceChannelService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DeviceChannelServiceImpl extends ServiceImpl<DeviceChannelMapper, DeviceChannelEntity> implements IDeviceChannelService {
    @Autowired
    ISdkCommderService iSdkCommderService;

    @Resource
    DeviceMapper deviceMapper;


    @Resource
    DeviceChannelMapper deviceChannelMapper;

    @Override
    public boolean resetChannelsForcatalog(String deviceId, List<DeviceChannel> deviceChannelList) {
        return false;
    }

    @Override
    public void cleanChannelsForDevice(String deviceId) {

    }

    @Override
    public DeviceChannel getOne(String deviceId, String channelId) {
        return null;
    }

    @Override
    public RecordAllItem recordInfo(RecordInfoReq recordInfoReq) {

        CommonResponse<PlayCommonDto> playCommonDtoCommonResponse = playCommonCheck(recordInfoReq);
        PlayCommonDto data = playCommonDtoCommonResponse.getData();
        //获取设备配置信息
        RecordInfoDto recordInfoDto = iSdkCommderService.recordList(data.getLUserId(), data.getChannelNum(), recordInfoReq.getStartTime(), recordInfoReq.getEndTime());
        if(recordInfoDto.getErrorCode() < 0){
            //获取失败
            throw new BusinessException(BusinessErrorEnums.DB_CHANNEL_NOT_FOUND,String.valueOf(recordInfoDto.getErrorCode()));
        }
        return recordInfoDto.getRecordAllItem();

    }

    private CommonResponse<PlayCommonDto> playCommonCheck(RecordInfoReq recordInfoReq){
        //获取设备信息luserId
        long encodeId = Long.parseLong(recordInfoReq.getDeviceId());
        DeviceEntity deviceEntity = deviceMapper.selectById(encodeId);
        if(ObjectUtils.isEmpty(deviceEntity)){
            throw new BusinessException(BusinessErrorEnums.DB_DEVICE_NOT_FOUND);
        }else {
            if(deviceEntity.getOnline() != 1){
                throw new BusinessException(BusinessErrorEnums.DB_DEVICE_NOT_FOUND);
            }
        }
        DeviceLoginDto login = iSdkCommderService.login(deviceEntity.getIp(), deviceEntity.getPort(), deviceEntity.getUsername(), deviceEntity.getPassword());
        if(login.getErrorCode() != 0){
            throw new BusinessException(BusinessErrorEnums.DEVICE_LOGIN_ERROR);
        }

        //获取通道信息

        long channelId = Long.parseLong(recordInfoReq.getChannelId());
        DeviceChannelEntity deviceChannelEntity = deviceChannelMapper.selectById(channelId);
        if(ObjectUtils.isEmpty(deviceChannelEntity)){
            throw new BusinessException(BusinessErrorEnums.DB_CHANNEL_NOT_FOUND);
        }else {
            if(deviceChannelEntity.getOnline() != 1){
                throw new BusinessException(BusinessErrorEnums.CHANNEL_OFFLINE);

            }
        }

        PlayCommonDto playCommonDto = new PlayCommonDto();
        playCommonDto.setLUserId(login.getLUserId());
        playCommonDto.setChannelNum(deviceChannelEntity.getChannelNum());
        return CommonResponse.success(playCommonDto);

    }

    @Override
    public CommonResponse<CatalogSyncDto> channelSync(Long id) {

        //获取设备的信息重新登陆
        DeviceEntity deviceEntity = deviceMapper.selectById(id);
        DeviceLoginDto login = iSdkCommderService.login(deviceEntity.getIp(), deviceEntity.getPort(), deviceEntity.getUsername(), deviceEntity.getPassword());
        if(login.getErrorCode() != 0){
            throw new BusinessException(BusinessErrorEnums.DEVICE_LOGIN_ERROR);
        }
        int lUserId = login.getLUserId();
        //获取设备配置信息
        DeviceConfigDto deviceConfigDto = iSdkCommderService.deviceConfig(lUserId);
        if(deviceConfigDto.getErrorCode() != 0){
            //失败
            return CommonResponse.failure(BusinessErrorEnums.SDK_OPERATION_FAILURE,BusinessErrorEnums.SDK_OPERATION_FAILURE.getErrMsg()+deviceConfigDto.getErrorCode());
        }
        HCNetSDK.NET_DVR_DEVICECFG_V40 devicecfgV40 = deviceConfigDto.getDevicecfgV40();
        //获取设备类型
        short wDevClass = devicecfgV40.wDevClass;
        DeviceTypeEnum deviceTypeEnum = DeviceUtils.checkDeviceType(wDevClass);
        ChannelInfoDto channelInfoDto = new ChannelInfoDto();
        switch (deviceTypeEnum){
            case HIKVISION_DVR:

                channelInfoDto = iSdkCommderService.getDvrChannelList(lUserId, devicecfgV40);
                break;
            case HIKVISION_NVR:
                channelInfoDto = iSdkCommderService.getNvrChannelList(lUserId, devicecfgV40);
                break;

            case HIKVISION_IPC:
                channelInfoDto = iSdkCommderService.getIpcChannelList(lUserId, devicecfgV40);
                break;

            default:
                //暂不进行接入
                return CommonResponse.failure(BusinessErrorEnums.SDK_OPERATION_FAILURE,"暂不支持改设备类型的处理");
        }


        List<DeviceChannelEntity> channelList = channelInfoDto.getChannelList();

        if(channelInfoDto.getErrorCode() == 0){
            LambdaQueryWrapper<DeviceChannelEntity> updateLambdaQueryWrapper = new LambdaQueryWrapper<>();
            updateLambdaQueryWrapper.eq(DeviceChannelEntity::getEncodeId,id);
            List<DeviceChannelEntity> deviceChannelDbList = deviceChannelMapper.selectList(updateLambdaQueryWrapper);
            String ip = deviceEntity.getIp();
            short port = deviceEntity.getPort();
            String password = deviceEntity.getPassword();
            if(ObjectUtils.isEmpty(deviceChannelDbList)){
                for(DeviceChannelEntity deviceChannel: channelList){
                    deviceChannel.setIp(ip);
                    deviceChannel.setPort(port);
                    deviceChannel.setPassword(password);
                    deviceChannel.setEncodeId(id);
                    deviceChannelMapper.insert(deviceChannel);
                }
            }else {
                List<Integer> channelNumList = channelList.stream().map(DeviceChannelEntity::getChannelNum).collect(Collectors.toList());
                List<Integer> channelDbNumList = deviceChannelDbList.stream().map(DeviceChannelEntity::getChannelNum).collect(Collectors.toList());

                for(DeviceChannelEntity deviceChannel: channelList){
                    deviceChannel.setIp(ip);
                    deviceChannel.setPort(port);
                    deviceChannel.setPassword(password);
                    deviceChannel.setEncodeId(id);


                    for (DeviceChannelEntity channelDb: deviceChannelDbList){
                        //需要删除的
                        if(!channelNumList.contains(channelDb.getChannelNum())){
                            deviceChannelMapper.deleteById(channelDb.getId());
                            continue;

                        }
                        //需要编辑的
                        if(channelDb.getChannelNum().equals(deviceChannel.getChannelNum())){
                            //编辑
                            deviceChannel.setId(channelDb.getId());
                            deviceChannelMapper.updateById(deviceChannel);
                            continue;
                        }

                        //新增
                        if(!channelDbNumList.contains(deviceChannel.getChannelNum())){
                            deviceChannelMapper.insert(deviceChannel);

                        }

                    }

                }
            }




        }else {
            return CommonResponse.failure(BusinessErrorEnums.SDK_OPERATION_FAILURE,BusinessErrorEnums.SDK_OPERATION_FAILURE.getErrMsg()+channelInfoDto.getErrorCode());
        }
        CatalogSyncDto catalogSyncDto = new CatalogSyncDto();
        catalogSyncDto.setNum(channelList.size());
        catalogSyncDto.setTotal(channelList.size());
        catalogSyncDto.setChannelDetailList(channelList);
        return CommonResponse.success(catalogSyncDto);

    }

    @Override
    public void channelHardDelete(long channelDbId) {
            //进行设备离线推出


        //删除通道
        deviceChannelMapper.deleteById(channelDbId);

    }

    @Override
    public void channelSoftDelete(long channelDbId){
        LambdaQueryWrapper<DeviceChannelEntity> deviceChannelEntityLambdaQueryWrapper = new LambdaQueryWrapper<>();
        deviceChannelEntityLambdaQueryWrapper.eq(DeviceChannelEntity::getId,channelDbId);
        deviceChannelEntityLambdaQueryWrapper.eq(DeviceChannelEntity::getDeleted,1);

        DeviceChannelEntity deviceChannelEntity = new DeviceChannelEntity();
        deviceChannelEntity.setId(channelDbId);
        deviceChannelEntity.setDeleted(1);
        //软删除通道
        deviceChannelMapper.updateById(deviceChannelEntity);


    }
}
