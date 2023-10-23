package com.runjian.controller;

import com.runjian.common.commonDto.Gb28181Media.req.GatewayStreamNotify;
import com.runjian.common.commonDto.StreamPlayDto;
import com.runjian.common.config.response.CommonResponse;
import com.runjian.common.validator.ValidatorService;
import com.runjian.gb28181.bean.Device;
import com.runjian.gb28181.transmit.cmd.ISIPCommander;
import com.runjian.service.IDeviceService;
import com.runjian.service.IplayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * 网关服务通知
 * @author chenjialing
 */
@RestController
@RequestMapping("/api")
@Slf4j
public class ApiOperationRtpServer {




    @Autowired
    private ValidatorService validatorService;

    @Autowired
    private IplayService iplayService;

    @Autowired
    private IDeviceService deviceService;

    @Autowired
    private ISIPCommander sipCommander;

    //查看流是否存在
    /**
     *  bye流
     */
    @GetMapping(value = "/test/streamBye",produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<Boolean> streamNotifyServer(@RequestParam String streamId,@RequestParam String callId){

        return CommonResponse.success(iplayService.testStreamBye(streamId, callId));
    }

    /**
     *  * @param	guardCmdStr SetGuard：布防，ResetGuard：撤防
     * @param deviceId
     * @return
     */
    @GetMapping(value = "/test/testAlarm",produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<Boolean> testAlarm(@RequestParam String deviceId,@RequestParam String guardCmdStr){

        try {
            deviceService.guardAlarm(deviceId,guardCmdStr);
        }catch (Exception e){
            log.info("布防指令",e);
        }
        return CommonResponse.success();
    }

    /**
     *  * @param	guardCmdStr SetGuard：布防，ResetGuard：撤防
     * @param deviceId
     * @return
     */
    @GetMapping(value = "/test/testBroadCast",produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<Boolean> testBroadCast(@RequestParam String deviceId,@RequestParam String channelId){

        try {
            Device device = deviceService.getDevice(deviceId);
            sipCommander.audioBroadcastCmd(device,channelId,null,null);

        }catch (Exception e){
            log.info("语音广播",e);
        }
        return CommonResponse.success();
    }

    @GetMapping(value = "/test/testCatalogSync",produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<Boolean> testCatalogSync(@RequestParam String deviceId){

        try {
            Device device = deviceService.getDevice(deviceId);
            deviceService.sync(device,null);

        }catch (Exception e){
            log.info("通道同步",e);
        }
        return CommonResponse.success();
    }

    @PostMapping(value = "/test/testBye",produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<Boolean> testBye(@RequestBody StreamPlayDto streamPlayDto){

        try {
            iplayService.streamBye(streamPlayDto,null);

        }catch (Exception e){
            log.info("通道同步",e);
        }
        return CommonResponse.success();
    }
}
