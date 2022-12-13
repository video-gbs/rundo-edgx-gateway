package com.runjian.timer.handle;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.runjian.common.config.response.CommonResponse;
import com.runjian.common.constant.LogTemplate;
import com.runjian.common.utils.ConstantUtils;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.exception.BusinessException;
import com.runjian.common.utils.JsonUtils;
import com.runjian.timer.constant.RestfulMethodType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.springframework.http.*;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Miracle
 * @date 2022/4/24 9:20
 */
@Data
@Slf4j
@Component
public class RestfulHandle extends QuartzJobBean {

    /**
     * 请求方法
     */
    private String method;

    /**
     * 请求链接
     */
    private String url;

    /**
     * 参数
     */
    private Map<String, Object> parameter;


    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "定时任务", "执行RESTFUL处理中" , this);
        JobKey jobKey = context.getJobDetail().getKey();
        if (Objects.isNull(parameter)){
            parameter = new HashMap<>(0);
        }
        try{
            sendRequest();
        }
        catch (Exception e){
            log.info(LogTemplate.ERROR_LOG_MSG_TEMPLATE, "定时任务", "执行RESTFUL处理失败" , this, e.getMessage());
            JobExecutionException e2 = new JobExecutionException(e);
            if (e instanceof BusinessException){
                throw e2;
            }
            // true 表示 Quartz 会自动取消所有与这个 job 有关的 trigger，从而避免再次运行 job
            // e2.setUnscheduleAllTriggers(true);
            // 停止调用此特定作业运行的触发器
            // e2.setUnscheduleFiringTrigger(true);
        }
        log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "定时任务", "执行RESTFUL处理成功" , this);
    }

    /**
     * 发送请求
     * @return
     * @throws BusinessException
     */
    private void sendRequest() throws BusinessException {
        JSONObject jsonObject;
        try {

            HttpHeaders headers = new HttpHeaders();
            headers.add("Accept", "application/json");
            ResponseEntity<CommonResponse> responseEntity;
            switch (RestfulMethodType.getTypeByMag(getMethod())){
                case GET:
                    // request访问
                    UriComponentsBuilder getBuilder = UriComponentsBuilder.fromHttpUrl(getUrl());
                    for (Map.Entry<String, Object> entry : getParameter().entrySet()){
                        getBuilder.queryParam(entry.getKey(), entry.getValue());
                    }
                    responseEntity = ConstantUtils.REST_TEMPLATE.exchange(getBuilder.build().toUri(), HttpMethod.GET, new HttpEntity<>(null, headers), CommonResponse.class);
                    break;
                case PUT:
                    jsonObject = JsonUtils.toJsonObj(getParameter());
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    responseEntity = ConstantUtils.REST_TEMPLATE.exchange(getUrl(), HttpMethod.PUT, new HttpEntity<>(jsonObject.toJSONString(), headers), CommonResponse.class);
                    break;
                case POST:
                    jsonObject = JsonUtils.toJsonObj(getParameter());
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    responseEntity = ConstantUtils.REST_TEMPLATE.exchange(getUrl(), HttpMethod.POST, new HttpEntity<>(jsonObject.toJSONString(), headers), CommonResponse.class);
                    break;
                case DELETE:
                    UriComponentsBuilder delBuilder = UriComponentsBuilder.fromHttpUrl(getUrl());
                    for (Map.Entry<String, Object> entry : getParameter().entrySet()){
                        delBuilder.queryParam(entry.getKey(), entry.getValue());
                    }
                    responseEntity = ConstantUtils.REST_TEMPLATE.exchange(delBuilder.build().toUri(), HttpMethod.DELETE, new HttpEntity<>(null, headers), CommonResponse.class);
                    break;
                default:
                    throw new BusinessException(BusinessErrorEnums.TIMER_RUN_ERROR, "Restful定时任务：执行异常,未知的restful方法处理");
            }
            if (!responseEntity.hasBody() || responseEntity.getBody().getCode() != 0){
                throw new BusinessException(BusinessErrorEnums.TIMER_RUN_ERROR, "Restful定时任务：执行异常,请求未正确返回");
            }
        }catch (JSONException e) {
            e.printStackTrace();
            throw new BusinessException(BusinessErrorEnums.TIMER_RUN_ERROR, "Restful定时任务：执行异常," + e.getMessage() );
        }catch (ResourceAccessException e){
            throw new BusinessException(BusinessErrorEnums.TIMER_RUN_ERROR, "Restful定时任务：执行异常," + e.getMessage());
        }


    }

}
