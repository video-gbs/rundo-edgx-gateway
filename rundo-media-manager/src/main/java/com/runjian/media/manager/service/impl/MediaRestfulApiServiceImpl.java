package com.runjian.media.manager.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.exception.BusinessException;
import com.runjian.common.config.response.CommonResponse;
import com.runjian.common.constant.LogTemplate;
import com.runjian.common.utils.RestTemplateUtil;
import com.runjian.media.manager.dto.dto.MediaServerConfigDto;
import com.runjian.media.manager.dto.entity.MediaServerEntity;
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
import java.util.Map;

@Service
@Slf4j
public class MediaRestfulApiServiceImpl implements IMediaRestfulApiService {

    @Value("${mediaApi.getServerConfig}")
    private String getServerConfigApi;


    @Value("${mediaApi.setServerConfig}")
    private String setServerConfigApi;

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
            return false;
        }
        return true;
    }

    public Map<String, String> makeTokenHeader(String secret) {
        Map<String, String> map = new HashMap<>(8);
        map.put(tokenHeader, secret);
        return map;
    }
}
