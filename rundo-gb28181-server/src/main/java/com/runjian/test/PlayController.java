package com.runjian.test;

import com.runjian.common.commonDto.Gateway.req.PlayReq;
import com.runjian.common.constant.LogTemplate;
import com.runjian.service.IplayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * @author chenjialing
 */
@CrossOrigin
@RestController
@RequestMapping("/api/test")
@Tag(name = "点播测试")
@Slf4j
public class PlayController {
    @Autowired
    IplayService playService;

    @Operation(summary="点播")
    @RequestMapping("/play")
    public void play() {
        PlayReq playReq = new PlayReq();
        playReq.setChannelId("34020000001310000001");
        playReq.setDeviceId("34020000001180000241");
        playReq.setMsgId("12345678");
        playReq.setStreamMode("UDP");
        log.info(LogTemplate.PROCESS_LOG_TEMPLATE,"请求进入","无");
        playService.play(playReq);



    }
}
