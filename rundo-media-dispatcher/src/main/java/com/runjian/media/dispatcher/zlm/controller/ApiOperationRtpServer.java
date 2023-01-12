package com.runjian.media.dispatcher.zlm.controller;

import com.runjian.common.commonDto.Gb28181Media.BaseRtpServerDto;
import com.runjian.common.config.response.CommonResponse;
import com.runjian.common.validator.ValidatorService;
import com.runjian.media.dispatcher.zlm.dto.MediaServerItem;
import com.runjian.media.dispatcher.zlm.dto.SSRCInfo;
import com.runjian.media.dispatcher.zlm.service.ImediaServerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * 操作zlm开启与关闭rtp端口操作
 * @author chenjialing
 */
@RestController
@RequestMapping("/api")
@Slf4j
public class ApiOperationRtpServer {

    @Autowired
    ImediaServerService imediaServerService;

    @Autowired
    private ValidatorService validatorService;
    /**
     * 创建推流的端口
     */
    @PostMapping(value = "/media/openRtpServer",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<SSRCInfo> openRtpServer(@RequestBody BaseRtpServerDto baseRtpServerDto){
        validatorService.validateRequest(baseRtpServerDto);
        //获取zlm流媒体配置
        MediaServerItem defaultMediaServer = imediaServerService.getDefaultMediaServer();

        SSRCInfo ssrcInfo = imediaServerService.openRTPServer(defaultMediaServer, baseRtpServerDto.getStreamId(), baseRtpServerDto.getSsrcCheck(), baseRtpServerDto.getSsrc(), baseRtpServerDto.getPort());

        return ssrcInfo;
    }
}
