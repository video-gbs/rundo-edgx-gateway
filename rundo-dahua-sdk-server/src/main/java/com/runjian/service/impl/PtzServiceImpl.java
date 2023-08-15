package com.runjian.service.impl;

import com.runjian.common.commonDto.Gateway.req.ChannelPtzControlReq;
import com.runjian.common.commonDto.Gateway.req.DragZoomControlReq;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.exception.BusinessException;
import com.runjian.common.config.response.CommonResponse;
import com.runjian.common.constant.PtzOperationTypeEnum;
import com.runjian.domain.dto.PlayCommonDto;
import com.runjian.domain.dto.commder.DeviceLoginDto;
import com.runjian.domain.dto.commder.PresetQueryDto;
import com.runjian.entity.DeviceChannelEntity;
import com.runjian.entity.DeviceEntity;
import com.runjian.sdk.module.service.ISdkCommderService;
import com.runjian.service.IDeviceChannelService;
import com.runjian.service.IDeviceService;
import com.runjian.service.IPtzService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class PtzServiceImpl implements IPtzService {
    @Autowired
    IDeviceService deviceService;

    @Autowired
    IDeviceChannelService deviceChannelService;

    @Autowired
    ISdkCommderService sdkCommderService;


    private ConcurrentHashMap<Integer,Integer> channelCommonderMap = new ConcurrentHashMap();

    private Integer degreeValue = 36;

    @Override
    public Integer ptzControl(ChannelPtzControlReq channelPtzControlReq) {
        //进行通道号获取
        CommonResponse<PlayCommonDto> playCommonDtoCommonResponse = playCommonCheck(channelPtzControlReq.getDeviceId(), channelPtzControlReq.getChannelId());
        PlayCommonDto commonData = playCommonDtoCommonResponse.getData();
        int channelNum = commonData.getChannelNum();
        int lUserId = commonData.getLUserId();
        //进行数据的获取
        //通用的值
        int oldOperationValue = channelPtzControlReq.getCmdValue();

        //255 与[1,7]的转化
        int operationValue = (int)Math.ceil(oldOperationValue/ degreeValue);
        //云台方向值
        int horizonSpeedValue = (int) channelPtzControlReq.getHorizonSpeed();
        int verticalSpeedValue = (int)channelPtzControlReq.getVerticalSpeed();
        int cmdCode = channelPtzControlReq.getCmdCode();

        PtzOperationTypeEnum ptzOperationTypeEnum = PtzOperationTypeEnum.getTypeByTypeId(cmdCode);
        if(ObjectUtils.isEmpty(ptzOperationTypeEnum)){
            throw new BusinessException(BusinessErrorEnums.PTZ_OPERATION_TYPE_NOT_FOUND);
        }
        Integer aBoolean = -1000;
        switch (ptzOperationTypeEnum){
            case PRESET_SET:
                //预置位设置
                aBoolean = sdkCommderService.presetControl(lUserId, channelNum, 8, oldOperationValue);
                break;
            case PRESET_INVOKE:
                //预置位调用

                aBoolean = sdkCommderService.presetControl(lUserId, channelNum, 39, oldOperationValue);
                break;
            case PRESET_DEL:
                //预置位删除

                aBoolean = sdkCommderService.presetControl(lUserId, channelNum, 9, oldOperationValue);
                break;
            case PTZ_LEFT:
                //左转


                aBoolean = sdkCommderService.ptzControl(lUserId, channelNum, 23, 0, operationValue);
                channelCommonderMap.put(channelNum,23);

                break;
            case PTZ_RIGHT:
                //右转

                aBoolean = sdkCommderService.ptzControl(lUserId, channelNum, 24, 0, operationValue);
                channelCommonderMap.put(channelNum,24);

                break;
            case PTZ_UP:
                //上

                aBoolean = sdkCommderService.ptzControl(lUserId, channelNum, 21, 0, operationValue);
                channelCommonderMap.put(channelNum,21);

                break;
            case PTZ_DOWN:
                //下
                aBoolean = sdkCommderService.ptzControl(lUserId, channelNum, 22, 0, operationValue);
                channelCommonderMap.put(channelNum,22);

                break;
            case PTZ_UPLEFT:
                //左上
                aBoolean = sdkCommderService.ptzControl(lUserId, channelNum, 25, 0, operationValue);
                channelCommonderMap.put(channelNum,25);

                break;
            case PTZ_UPRIGHT:
                //右上
                aBoolean = sdkCommderService.ptzControl(lUserId, channelNum, 26, 0, operationValue);
                channelCommonderMap.put(channelNum,26);

                break;
            case PTZ_DOWNLEFT:
                //左下
                aBoolean = sdkCommderService.ptzControl(lUserId, channelNum, 27, 0, operationValue);
                channelCommonderMap.put(channelNum,27);

                break;
            case PTZ_DOWNRIGHT:
                //右下
                aBoolean = sdkCommderService.ptzControl(lUserId, channelNum, 28, 0, operationValue);

                channelCommonderMap.put(channelNum,28);

                break;
            case ZOOM_IN:
                //倍率放大

                aBoolean = sdkCommderService.ptzControl(lUserId, channelNum, 11, 0, operationValue);

                channelCommonderMap.put(channelNum,11);
                break;
            case ZOOM_OUT:
                //倍率缩小
                aBoolean = sdkCommderService.ptzControl(lUserId, channelNum, 12, 0, operationValue);

                channelCommonderMap.put(channelNum,12);

                break;
            case PTZ_STOP:
                //ptz停止  获取上次的缓存指令
                Integer cmdLast = channelCommonderMap.get(channelNum);
                aBoolean = sdkCommderService.ptzControl(lUserId, channelNum, cmdLast, 1, operationValue);

                break;
            case IRIS_REDUCE:
                //光圈缩小

                aBoolean = sdkCommderService.ptzControl(lUserId, channelNum, 16, 0, operationValue);

                channelCommonderMap.put(channelNum,16);
                break;
            case IRIS_GROW:
                //光圈放大

                aBoolean = sdkCommderService.ptzControl(lUserId, channelNum, 15, 0, operationValue);

                channelCommonderMap.put(channelNum,15);
                break;
            case FOCUS_FAR:
                //聚焦近远

                aBoolean = sdkCommderService.ptzControl(lUserId, channelNum, 14, 0, operationValue);

                channelCommonderMap.put(channelNum,14);
                break;
            case FOCUS_NEAR:
                //聚焦近远

                aBoolean = sdkCommderService.ptzControl(lUserId, channelNum, 13, 0, operationValue);

                channelCommonderMap.put(channelNum,13);
                break;
            case IRISE_AND_FOCUS_STOP:
                //F1停止

                Integer cmdLastF1 = channelCommonderMap.get(channelNum);
                aBoolean = sdkCommderService.ptzControl(lUserId, channelNum, cmdLastF1, 1, operationValue);
                break;

            default:
                //信令操作异常

                break;
        }

        return aBoolean;


    }

    @Override
    public PresetQueryDto ptzPresetControl(String deviceId, String channelId, String msgId) {
        CommonResponse<PlayCommonDto> playCommonDtoCommonResponse = playCommonCheck(deviceId, channelId); PlayCommonDto commonData = playCommonDtoCommonResponse.getData();
        int channelNum = commonData.getChannelNum();
        int lUserId = commonData.getLUserId();

        return sdkCommderService.presetList(lUserId,channelNum);
    }

    @Override
    public Integer dragZoomControl(DragZoomControlReq dragZoomControlReq) {
        CommonResponse<PlayCommonDto> playCommonDtoCommonResponse = playCommonCheck(dragZoomControlReq.getDeviceId(), dragZoomControlReq.getChannelId());
        PlayCommonDto commonData = playCommonDtoCommonResponse.getData();
        int channelNum = commonData.getChannelNum();
        int lUserId = commonData.getLUserId();

        return sdkCommderService.Zoom3DControl(lUserId, channelNum, dragZoomControlReq.getLengthx(), dragZoomControlReq.getLengthy(), dragZoomControlReq.getMidpointx(), dragZoomControlReq.getMidpointy(), dragZoomControlReq.getDragType());
    }


    private CommonResponse<PlayCommonDto> playCommonCheck(String deviceId, String channelId){
        //获取设备信息luserId
        long encodeId = Long.parseLong(deviceId);
        DeviceEntity deviceEntity = deviceService.getById(encodeId);
        if(ObjectUtils.isEmpty(deviceEntity)){
            throw new BusinessException(BusinessErrorEnums.DB_DEVICE_NOT_FOUND);
        }else {
            if(deviceEntity.getOnline() != 1){
                throw new BusinessException(BusinessErrorEnums.DB_DEVICE_NOT_FOUND);
            }
        }
        DeviceLoginDto login = sdkCommderService.login(deviceEntity.getIp(), deviceEntity.getPort(), deviceEntity.getUsername(), deviceEntity.getPassword());
        if(login.getErrorCode() != 0){
            throw new BusinessException(BusinessErrorEnums.DEVICE_LOGIN_ERROR);
        }
        int lUserId = login.getLUserId();
        //获取通道信息

        long channelDbId = Long.parseLong(channelId);
        DeviceChannelEntity deviceChannelEntity = deviceChannelService.getById(channelDbId);
        if(ObjectUtils.isEmpty(deviceChannelEntity)){
            throw new BusinessException(BusinessErrorEnums.DB_CHANNEL_NOT_FOUND);
        }else {
            if(deviceChannelEntity.getOnline() != 1){
                throw new BusinessException(BusinessErrorEnums.CHANNEL_OFFLINE);

            }
        }

        PlayCommonDto playCommonDto = new PlayCommonDto();
        playCommonDto.setLUserId(lUserId);
        playCommonDto.setChannelNum(deviceChannelEntity.getChannelNum());
        return CommonResponse.success(playCommonDto);

    }
}
