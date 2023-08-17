package com.runjian.sdk.module.event;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.exception.BusinessException;
import com.runjian.common.config.response.CommonResponse;
import com.runjian.common.constant.LogTemplate;
import com.runjian.common.utils.BeanUtil;
import com.runjian.common.utils.DateUtils;
import com.runjian.common.utils.RestTemplateUtil;
import com.runjian.sdk.module.jnaDto.AlarmIntellectDto;
import com.runjian.sdk.module.jnaDto.AlarmIntellectRespDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.nio.file.Files;

/**
 * 网关注册上线订阅
 */
@Component
@Slf4j
public class AlarmIntellectListener implements ApplicationListener<AlarmIntellectEvent> {


    /**
     * 0使用http,1使用mq通知
     */
    @Value("${alarm-push-list.type:0}")
    private int pushType;

    @Value("${alarm-push-list.pushUrl}")
    private String pushUrl;



    @Autowired
    RestTemplate restTemplate;

    @Override
    public void onApplicationEvent(AlarmIntellectEvent event) {
        AlarmIntellectDto alarmIntellectDto = event.getAllarmIntellectDto();
        //设置全局的bean的值 方便以后获取
        AlarmIntellectRespDto alarmIntellectRespDto = new AlarmIntellectRespDto();
        long eventStamp = DateUtils.yyyy_MM_dd_HH_mm_ssToTimestamp(alarmIntellectDto.getEventTime());
        BeanUtil.copyProperties(alarmIntellectDto,alarmIntellectRespDto);
        alarmIntellectRespDto.setEventTime(eventStamp);

        String captureImageUrl = alarmIntellectDto.getCaptureImageUrl();
        //处理图片.
        String image="";
        try{
            File imageFile = new File(captureImageUrl);
            byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
            byte[] encodedBytes = Base64.encodeBase64(imageBytes);
            image = new String(encodedBytes);
        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"图片处理失败",captureImageUrl,e);
        }

        alarmIntellectRespDto.setCaptureImage(image);

        //进行告警信息推送
        String result = RestTemplateUtil.postString(pushUrl, JSON.toJSONString(alarmIntellectRespDto),null, restTemplate);
        if (ObjectUtils.isEmpty(result)) {
            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"告警推送","连接业务异常",result);

            return;
        }
        CommonResponse commonResponse = JSONObject.parseObject(result, CommonResponse.class);
        if(commonResponse.getCode()!=BusinessErrorEnums.SUCCESS.getErrCode()){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"告警推送","处理返回异常",commonResponse);
        }

    }

}
