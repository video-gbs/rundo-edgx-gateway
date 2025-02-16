package com.runjian.controller;

import com.runjian.common.config.response.CommonResponse;
import com.runjian.common.validator.ValidatorService;
import com.runjian.domain.dto.CatalogSyncDto;
import com.runjian.domain.dto.commder.PlayInfoDto;
import com.runjian.domain.dto.commder.PresetQueryDto;
import com.runjian.domain.dto.commder.RecordInfoDto;
import com.runjian.domain.req.DeviceReq;
import com.runjian.domain.req.PlaySdkReq;
import com.runjian.domain.req.RecordInfoSdkReq;
import com.runjian.entity.DeviceChannelEntity;
import com.runjian.sdk.module.service.ISdkCommderService;
import com.runjian.service.IDeviceChannelService;
import com.runjian.service.IDeviceService;
import com.runjian.service.IplayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 网关服务通知
 * @author chenjialing
 */
@RestController
@RequestMapping("/api")
@Slf4j
public class ApiTestServer {



    @Autowired
    private ValidatorService validatorService;

    @Autowired
    private IDeviceService deviceService;

    @Autowired
    private IDeviceChannelService deviceChannelService;

    @Autowired
    private ISdkCommderService sdkCommderService;

    @Autowired
    private IplayService iplayService;
    //查看流是否存在
    /**
     *  查看流是否存在
     */
    @PostMapping(value = "/test/login",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<Long> register(@RequestBody DeviceReq deviceReq){
        return deviceService.add(deviceReq.getIp(), deviceReq.getPort(), deviceReq.getUser(), deviceReq.getPsw());

    }


    @GetMapping(value = "/test/alarm",produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<Integer> alarm(@RequestParam long lUserId, @RequestParam int channelNm){

        return CommonResponse.success(sdkCommderService.intellectAlarm(lUserId,channelNm,0));

    }


    @GetMapping(value = "/test/channelSync",produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<CatalogSyncDto> channelSync(@RequestParam Long id){

        return deviceChannelService.channelSync(id);

    }


    @PostMapping(value = "/test/play",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<List<DeviceChannelEntity>> play(@RequestBody PlaySdkReq playSdkReq){
//        iplayService.play(null);
        return CommonResponse.success();

    }

    @GetMapping(value = "/test/playStop")
    public CommonResponse<Boolean> play(@RequestParam String streamId){

        return CommonResponse.success(iplayService.streamBye(streamId));

    }

    @PostMapping(value = "/test/recordInfo",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<RecordInfoDto> recordInfo(@RequestBody RecordInfoSdkReq recordInfoSdkReq){

        RecordInfoDto recordInfoDto = sdkCommderService.recordList(recordInfoSdkReq.getLUserId(),recordInfoSdkReq.getLChannel(),recordInfoSdkReq.getStartTime(), recordInfoSdkReq.getEndTime());
        return CommonResponse.success(recordInfoDto);

    }

    @PostMapping(value = "/test/playBack")
    public CommonResponse<PlayInfoDto> playBack(@RequestBody RecordInfoSdkReq recordInfoSdkReq){

        return CommonResponse.success(null);

    }

    @GetMapping(value = "/test/ptz")
    public CommonResponse<Integer> ptz(@RequestParam long lUserId,@RequestParam int lChannel,@RequestParam int dwPTZCommand, @RequestParam int dwStop,@RequestParam int dwSpeed){


        return CommonResponse.success(sdkCommderService.ptzControl(lUserId,lChannel,dwPTZCommand,dwStop,dwSpeed));

    }


    @GetMapping(value = "/test/preset")
    public CommonResponse<PresetQueryDto> preset(@RequestParam long lUserId, @RequestParam int lChannel){


        return CommonResponse.success(sdkCommderService.presetList(lUserId,lChannel));

    }
}
