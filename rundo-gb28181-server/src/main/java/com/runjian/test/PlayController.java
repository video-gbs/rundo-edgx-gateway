package com.runjian.test;

import com.runjian.common.commonDto.Gateway.req.PlayReq;
import com.runjian.service.IplayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
public class PlayController {
    @Autowired
    IplayService playService;

    @Operation(summary="点播")
    @RequestMapping("/play")
    public void play() {
        PlayReq playReq = new PlayReq();
        playReq.setChannelId("34020000001320000009");
        playReq.setDeviceId("34020000001320000010");
        playReq.setEnableAudio(false);
        playReq.setMsgId("12345678");
        playReq.setSsrcCheck(Boolean.TRUE);
        playReq.setStreamMode("UDP");

        playService.play(playReq);



    }
}
