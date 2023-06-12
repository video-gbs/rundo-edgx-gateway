package com.runjian.media.manager.controller;

import com.runjian.common.config.response.CommonResponse;
import com.runjian.media.manager.dto.dto.MediaServerConfigDto;
import com.runjian.media.manager.dto.entity.MediaServerEntity;
import com.runjian.media.manager.dto.req.CreateServerReq;
import com.runjian.media.manager.dto.req.Gb28181ServerReq;
import com.runjian.media.manager.dto.resp.CreateServerPortRsp;
import com.runjian.media.manager.dto.resp.MediaDispatchInfoRsp;
import com.runjian.media.manager.dto.resp.MediaPlayInfoRsp;
import com.runjian.media.manager.service.IMediaRestfulApiService;
import com.runjian.media.manager.service.IMediaServerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test")
@Slf4j
public class TestMediaRestFulApi {

    @Autowired
    IMediaRestfulApiService mediaRestfulApiService;

    @Autowired
    IMediaServerService mediaServerService;

    /**
     * 注册
     * @param req
     * @return
     */
    @PostMapping(value = "/getMediaList",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<List<MediaPlayInfoRsp>> getMediaList(@RequestBody CreateServerReq req){//获取zlm流媒体配置
        MediaServerEntity defaultMediaServer = mediaServerService.getDefaultMediaServer();
        List<MediaPlayInfoRsp> mediaList = mediaRestfulApiService.getMediaList(req.getApp(), req.getStreamId(), defaultMediaServer);
        return CommonResponse.success(mediaList);
    }

    /**
     * 注册
     * @param req
     * @return
     */
    @PostMapping(value = "/getDispatchList",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<List<MediaDispatchInfoRsp>> getDispatchList(@RequestBody CreateServerReq req){//获取zlm流媒体配置
        MediaServerEntity defaultMediaServer = mediaServerService.getDefaultMediaServer();
        List<MediaDispatchInfoRsp> mediaList = mediaRestfulApiService.getDispatchList(req.getApp(), req.getStreamId(), defaultMediaServer);
        return CommonResponse.success(mediaList);
    }


    /**
     * 注册
     * @param req
     * @return
     */
    @PostMapping(value = "/openSDKServer",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<CreateServerPortRsp> openSDKServer(@RequestBody CreateServerReq req){//获取zlm流媒体配置
        MediaServerEntity defaultMediaServer = mediaServerService.getDefaultMediaServer();
        CreateServerPortRsp mediaList = mediaRestfulApiService.openSDKServer(req, defaultMediaServer);
        return CommonResponse.success(mediaList);
    }

    /**
     * 注册
     * @param key
     * @return
     */
    @PostMapping(value = "/closeSDKServer",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<Boolean> closeSDKServer(@RequestParam String key){//获取zlm流媒体配置
        MediaServerEntity defaultMediaServer = mediaServerService.getDefaultMediaServer();
        return CommonResponse.success(mediaRestfulApiService.closeSDKServer(key, defaultMediaServer));
    }

    /**
     * 注册
     * @param req
     * @return
     */
    @PostMapping(value = "/openRtpServer",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<CreateServerPortRsp> openRtpServer(@RequestBody Gb28181ServerReq req){//获取zlm流媒体配置
        MediaServerEntity defaultMediaServer = mediaServerService.getDefaultMediaServer();
        return CommonResponse.success(mediaRestfulApiService.openRtpServer(req, defaultMediaServer));
    }

    /**
     * 注册
     * @param key
     * @return
     */
    @PostMapping(value = "/closeRtpServer",consumes = MediaType.APPLICATION_JSON_VALUE,produces = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<Boolean> closeRtpServer(@RequestParam String key){//获取zlm流媒体配置
        MediaServerEntity defaultMediaServer = mediaServerService.getDefaultMediaServer();
        return CommonResponse.success(mediaRestfulApiService.closeRtpServer(key, defaultMediaServer));
    }
}
