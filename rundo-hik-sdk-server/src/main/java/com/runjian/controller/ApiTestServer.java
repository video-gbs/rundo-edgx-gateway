package com.runjian.controller;

import com.runjian.common.config.response.CommonResponse;
import com.runjian.common.validator.ValidatorService;
import com.runjian.domain.dto.commder.DeviceOnlineDto;
import com.runjian.domain.req.DeviceReq;
import com.runjian.domain.req.PlaySdkReq;
import com.runjian.entity.DeviceChannelEntity;
import com.runjian.hik.module.service.ISdkCommderService;
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


    @GetMapping(value = "/test/channelSync",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<List<DeviceChannelEntity>> channelSync(@RequestParam Long id){

        return deviceChannelService.channelSync(id);

    }


    @PostMapping(value = "/test/play",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<List<DeviceChannelEntity>> play(@RequestBody PlaySdkReq playSdkReq){
        iplayService.play(playSdkReq);
        return CommonResponse.success();

    }

    @GetMapping(value = "/test/playStop")
    public CommonResponse<Boolean> play(@RequestParam String streamId){

        return CommonResponse.success(iplayService.streamBye(streamId,null));

    }
}
