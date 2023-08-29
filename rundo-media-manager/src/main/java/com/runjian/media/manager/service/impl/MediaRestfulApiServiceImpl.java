package com.runjian.media.manager.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.exception.BusinessException;
import com.runjian.common.config.response.CommonResponse;
import com.runjian.common.constant.LogTemplate;
import com.runjian.common.utils.RestTemplateUtil;
import com.runjian.media.manager.dto.dto.MediaServerConfigDto;
import com.runjian.media.manager.dto.entity.MediaServerEntity;
import com.runjian.media.manager.dto.req.CreateServerReq;
import com.runjian.media.manager.dto.req.Gb28181ServerReq;
import com.runjian.media.manager.dto.resp.CreateServerPortRsp;
import com.runjian.media.manager.dto.resp.MediaDispatchInfoRsp;
import com.runjian.media.manager.dto.resp.MediaPlayInfoRsp;
import com.runjian.media.manager.service.IMediaRestfulApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class MediaRestfulApiServiceImpl implements IMediaRestfulApiService {

    @Value("${mediaApi.getServerConfig}")
    private String getServerConfigApi;


    @Value("${mediaApi.setServerConfig}")
    private String setServerConfigApi;

    @Value("${mediaApi.getMediaList}")
    private String getMediaListApi;


    @Value("${mediaApi.getDispatchList}")
    private String getDispatchListApi;


    @Value("${mediaApi.openSDKServer}")
    private String openSdkServerApi;

    @Value("${mediaApi.closeSDKServer}")
    private String closeSdkServerApi;


    @Value("${mediaApi.openRtpServer}")
    private String openRtpServerApi;

    @Value("${mediaApi.closeRtpServer}")
    private String closeRtpServerApi;

    @Value("${mediaApi.startSendRtp}")
    private String startSendRtp;

    @Value("${mediaApi.stopSendRtp}")
    private String stopSendRtpApi;

    @Value("${mediaApi.TokenHeader}")
    private String tokenHeader;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public MediaServerEntity getMediaServerConfigApi(MediaServerEntity mediaServerEntity) {
        String url = String.format("http://%s:%s%s",  mediaServerEntity.getIp(), mediaServerEntity.getHttpPort(), getServerConfigApi);

        String result = RestTemplateUtil.get(url, makeTokenHeader(mediaServerEntity.getSecret()), restTemplate);
        if (ObjectUtils.isEmpty(result)) {
            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"流媒体服务连接","连接业务异常",result);

            return null;
        }
        CommonResponse commonResponse = JSONObject.parseObject(result, CommonResponse.class);
        if(commonResponse.getCode()!=BusinessErrorEnums.SUCCESS.getErrCode()){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"流媒体服务连接","连接业务异常",commonResponse);
            return null;
        }
        return JSONObject.parseObject(JSON.toJSONString(commonResponse.getData()), MediaServerEntity.class);

    }

    @Override
    public Boolean setMediaServerConfigApi(MediaServerConfigDto mediaServerConfigDto,MediaServerEntity mediaServerEntity) {
        String url = String.format("http://%s:%s%s",  mediaServerConfigDto.getHttpIp(), mediaServerConfigDto.getHttpPort(), setServerConfigApi);

        String result = RestTemplateUtil.postString(url, JSON.toJSONString(mediaServerConfigDto),makeTokenHeader(mediaServerEntity.getSecret()), restTemplate);
        if (ObjectUtils.isEmpty(result)) {
            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"流媒体服务连接","连接业务异常",result);

            throw new BusinessException(BusinessErrorEnums.MEDIA_ZLM_COLLECT_ERROR);
        }
        CommonResponse commonResponse = JSONObject.parseObject(result, CommonResponse.class);
        if(commonResponse.getCode()!=BusinessErrorEnums.SUCCESS.getErrCode()){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"流媒体服务连接","连接业务异常",commonResponse);
            throw new BusinessException(BusinessErrorEnums.MEDIA_ZLM_COLLECT_ERROR,commonResponse.getMsg());
        }
        return true;
    }

    @Override
    public List<MediaPlayInfoRsp> getMediaList(String app, String streamId,MediaServerEntity mediaServerEntity) {
        String url = String.format("http://%s:%s%s",  mediaServerEntity.getIp(), mediaServerEntity.getHttpPort(), getMediaListApi);

        HashMap<String, Object> stringStringHashMap = new HashMap<>();
        if(!ObjectUtils.isEmpty(app)){
            stringStringHashMap.put("app",app);
        }
        if(!ObjectUtils.isEmpty(streamId)){
            stringStringHashMap.put("streamId",streamId);
        }

        String result = RestTemplateUtil.getWithParams(url,makeTokenHeader(mediaServerEntity.getSecret()), stringStringHashMap,restTemplate);
        if (ObjectUtils.isEmpty(result)) {
            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"流媒体服务连接","连接业务异常",result);

            throw new BusinessException(BusinessErrorEnums.MEDIA_ZLM_COLLECT_ERROR);
        }
        CommonResponse commonResponse = JSONObject.parseObject(result, CommonResponse.class);
        if(commonResponse.getCode()!=BusinessErrorEnums.SUCCESS.getErrCode()){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"流媒体服务连接","连接业务异常",commonResponse);
            throw new BusinessException(BusinessErrorEnums.MEDIA_ZLM_COLLECT_ERROR,commonResponse.getMsg());
        }
        JSONArray dataArray = (JSONArray)commonResponse.getData();
        return JSONObject.parseArray(dataArray.toJSONString(),MediaPlayInfoRsp.class);
    }

    @Override
    public List<MediaDispatchInfoRsp> getDispatchList(String app, String streamId, MediaServerEntity mediaServerEntity) {
        String url = String.format("http://%s:%s%s",  mediaServerEntity.getIp(), mediaServerEntity.getHttpPort(), getDispatchListApi);

        HashMap<String, Object> stringStringHashMap = new HashMap<>();
        if(!ObjectUtils.isEmpty(app)){
            stringStringHashMap.put("app",app);
        }
        if(!ObjectUtils.isEmpty(streamId)){
            stringStringHashMap.put("streamId",streamId);
        }

        String result = RestTemplateUtil.getWithParams(url, makeTokenHeader(mediaServerEntity.getSecret()), stringStringHashMap,restTemplate);
        if (ObjectUtils.isEmpty(result)) {
            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"流媒体服务连接","连接业务异常",result);

            throw new BusinessException(BusinessErrorEnums.MEDIA_ZLM_COLLECT_ERROR);
        }
        CommonResponse commonResponse = JSONObject.parseObject(result, CommonResponse.class);
        if(commonResponse.getCode()!=BusinessErrorEnums.SUCCESS.getErrCode()){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"流媒体服务连接","连接业务异常",commonResponse);
            throw new BusinessException(BusinessErrorEnums.MEDIA_ZLM_COLLECT_ERROR,commonResponse.getMsg());
        }
        JSONArray dataArray = (JSONArray)commonResponse.getData();
        return JSONObject.parseArray(dataArray.toJSONString(),MediaDispatchInfoRsp.class);
    }

    @Override
    public CreateServerPortRsp openSDKServer(CreateServerReq createServerReq, MediaServerEntity mediaServerEntity) {
        String url = String.format("http://%s:%s%s",  mediaServerEntity.getIp(), mediaServerEntity.getHttpPort(), openSdkServerApi);
        HashMap<String, Object> stringStringHashMap = new HashMap<>();
        stringStringHashMap.put("app",createServerReq.getApp());
        stringStringHashMap.put("streamId",createServerReq.getStreamId());
        stringStringHashMap.put("port",createServerReq.getPort());
        stringStringHashMap.put("enableTcp",createServerReq.getEnableTcp());
        stringStringHashMap.put("enableMp4",createServerReq.getEnableMp4());


        String result = RestTemplateUtil.getWithParams(url, makeTokenHeader(mediaServerEntity.getSecret()),stringStringHashMap, restTemplate);
        if (ObjectUtils.isEmpty(result)) {
            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"流媒体服务连接","连接业务异常",result);

            throw new BusinessException(BusinessErrorEnums.MEDIA_ZLM_COLLECT_ERROR);
        }
        CommonResponse commonResponse = JSONObject.parseObject(result, CommonResponse.class);
        if(commonResponse.getCode()!=BusinessErrorEnums.SUCCESS.getErrCode()){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"流媒体服务连接","连接业务异常",commonResponse);
            throw new BusinessException(BusinessErrorEnums.MEDIA_ZLM_COLLECT_ERROR,commonResponse.getMsg());
        }
        return JSONObject.toJavaObject((JSONObject)commonResponse.getData(),CreateServerPortRsp.class);
    }

    @Override
    public Boolean closeSDKServer(Integer key, MediaServerEntity mediaServerEntity) {
        String url = String.format("http://%s:%s%s",  mediaServerEntity.getIp(), mediaServerEntity.getHttpPort(), closeSdkServerApi);
        HashMap<String, Object> stringStringHashMap = new HashMap<>();
        stringStringHashMap.put("key",key);

        String result = RestTemplateUtil.getWithParams(url, makeTokenHeader(mediaServerEntity.getSecret()),stringStringHashMap, restTemplate);
        if (ObjectUtils.isEmpty(result)) {
            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"流媒体服务连接","连接业务异常",result);

            throw new BusinessException(BusinessErrorEnums.MEDIA_ZLM_COLLECT_ERROR);
        }
        CommonResponse<Boolean> commonResponse = JSONObject.parseObject(result, CommonResponse.class);
        if(commonResponse.getCode()!=BusinessErrorEnums.SUCCESS.getErrCode()){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"流媒体服务连接","连接业务异常",commonResponse);
            throw new BusinessException(BusinessErrorEnums.MEDIA_ZLM_COLLECT_ERROR,commonResponse.getMsg());
        }
        return commonResponse.getData();
    }

    @Override
    public CreateServerPortRsp openRtpServer(Gb28181ServerReq gb28181ServerReq, MediaServerEntity mediaServerEntity) {
        String url = String.format("http://%s:%s%s",  mediaServerEntity.getIp(), mediaServerEntity.getHttpPort(), openRtpServerApi);
        HashMap<String, Object> stringStringHashMap = new HashMap<>();
        stringStringHashMap.put("app",gb28181ServerReq.getApp());
        stringStringHashMap.put("streamId",gb28181ServerReq.getStreamId());
        stringStringHashMap.put("port",gb28181ServerReq.getPort());
        stringStringHashMap.put("enableTcp",gb28181ServerReq.getEnableTcp());
        stringStringHashMap.put("enableMp4",gb28181ServerReq.getEnableMp4());
        stringStringHashMap.put("payload",96);


        String result = RestTemplateUtil.getWithParams(url, makeTokenHeader(mediaServerEntity.getSecret()),stringStringHashMap, restTemplate);
        if (ObjectUtils.isEmpty(result)) {
            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"流媒体服务连接","连接业务异常",result);

            throw new BusinessException(BusinessErrorEnums.MEDIA_ZLM_COLLECT_ERROR);
        }
        CommonResponse commonResponse = JSONObject.parseObject(result, CommonResponse.class);
        if(commonResponse.getCode()!=BusinessErrorEnums.SUCCESS.getErrCode()){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"流媒体服务连接","连接业务异常",commonResponse);
            throw new BusinessException(BusinessErrorEnums.MEDIA_ZLM_COLLECT_ERROR,commonResponse.getMsg());
        }
        return JSONObject.toJavaObject((JSONObject)commonResponse.getData(),CreateServerPortRsp.class);
    }

    @Override
    public Boolean closeRtpServer(Integer key, MediaServerEntity mediaServerEntity) {
        String url = String.format("http://%s:%s%s",  mediaServerEntity.getIp(), mediaServerEntity.getHttpPort(), closeRtpServerApi);
        HashMap<String, Object> stringStringHashMap = new HashMap<>();
        stringStringHashMap.put("key",key);

        String result = RestTemplateUtil.getWithParams(url, makeTokenHeader(mediaServerEntity.getSecret()),stringStringHashMap, restTemplate);
        if (ObjectUtils.isEmpty(result)) {
            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"流媒体服务连接","连接业务异常",result);

            throw new BusinessException(BusinessErrorEnums.MEDIA_ZLM_COLLECT_ERROR);
        }
        CommonResponse commonResponse = JSONObject.parseObject(result, CommonResponse.class);
        return commonResponse.getCode() == BusinessErrorEnums.SUCCESS.getErrCode();
    }

    public Map<String, String> makeTokenHeader(String secret) {
        Map<String, String> map = new HashMap<>(8);
        map.put(tokenHeader, secret);
        return map;
    }
}
