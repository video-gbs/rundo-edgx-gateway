package com.runjian.controller;

import com.runjian.common.commonDto.Gb28181Media.req.GatewayStreamNotify;
import com.runjian.common.config.response.CommonResponse;
import com.runjian.common.validator.ValidatorService;
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
}
