package com.runjian.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.exception.BusinessException;
import com.runjian.common.config.response.CommonResponse;
import com.runjian.common.constant.LogTemplate;
import com.runjian.common.utils.RestTemplateUtil;
import com.runjian.entity.PlayBackToolEntity;
import com.runjian.entity.PlayToolEntity;
import com.runjian.service.IMediaToolRestfulApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

@Service
@Slf4j
public class MediaToolRestfulApiServiceImpl implements IMediaToolRestfulApiService {


    @Autowired
    private RestTemplate restTemplate;

    @Value("${mdeia-tool-uri-list.livestreamPlay}")
    private String livestreamPlayUrl;

    @Value("${mdeia-tool-uri-list.backstreamPlay}")
    private String backstreamPlayUrl;

    @Value("${mdeia-tool-uri-list.streamBye}")
    private String streamByeUrl;
    @Override
    public CommonResponse<Integer> liveStreamDeal(PlayToolEntity playToolEntity) {
        String result = RestTemplateUtil.postString(livestreamPlayUrl, JSON.toJSONString(playToolEntity),null, restTemplate);
        if (ObjectUtils.isEmpty(result)) {
            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"流媒体工具服务连接","连接业务异常",result);

            throw new BusinessException(BusinessErrorEnums.MEDIA_ZLM_COLLECT_ERROR);
        }
        CommonResponse commonResponse = JSONObject.parseObject(result, CommonResponse.class);
        if(commonResponse.getCode()!=BusinessErrorEnums.SUCCESS.getErrCode()){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"流媒体工具服务","连接业务异常",commonResponse);
            throw new BusinessException(BusinessErrorEnums.MEDIA_ZLM_COLLECT_ERROR,commonResponse.getMsg());
        }

        return commonResponse;
    }

    @Override
    public CommonResponse<Integer> backStreamDeal(PlayBackToolEntity playToolEntity) {
        String result = RestTemplateUtil.postString(backstreamPlayUrl, JSON.toJSONString(playToolEntity),null, restTemplate);
        if (ObjectUtils.isEmpty(result)) {
            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"流媒体工具服务连接","连接业务异常",result);

            throw new BusinessException(BusinessErrorEnums.MEDIA_ZLM_COLLECT_ERROR);
        }
        CommonResponse commonResponse = JSONObject.parseObject(result, CommonResponse.class);
        if(commonResponse.getCode()!=BusinessErrorEnums.SUCCESS.getErrCode()){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"流媒体工具服务","连接业务异常",commonResponse);
            throw new BusinessException(BusinessErrorEnums.MEDIA_ZLM_COLLECT_ERROR,commonResponse.getMsg());
        }

        return commonResponse;
    }

    @Override
    public CommonResponse<Boolean> streamToolBye(Integer playHandle) {
        HashMap<String, Object> stringStringHashMap = new HashMap<>();
        stringStringHashMap.put("playHandle",playHandle);
        String result = RestTemplateUtil.getWithParams(streamByeUrl,null, stringStringHashMap,restTemplate);
        if (ObjectUtils.isEmpty(result)) {
            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"流媒体服务连接","连接业务异常",result);
            return CommonResponse.failure(BusinessErrorEnums.MEDIA_ZLM_COLLECT_ERROR);
        }
        return JSONObject.parseObject(result, CommonResponse.class);
    }
}
