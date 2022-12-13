package com.runjian.timer.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.runjian.common.validator.ValidationResult;
import com.runjian.common.validator.ValidatorFunction;
import com.runjian.timer.vo.request.AddDatabaseReq;
import com.runjian.timer.vo.request.AddRestfulReq;
import lombok.Data;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author Miracle
 * @date 2022/4/24 11:28
 */
@Data
public class JobData implements Serializable, ValidatorFunction {

    /**
     * 任务名称
     */
    @NotBlank(message = "任务名称不能为空")
    @Size(min = 1, max = 50, message = "任务名称过长")
    private String jobName;

    /**
     * 任务分组
     */
    @NotBlank(message = "任务分组名称不能为空")
    @Size(min = 1, max = 50, message = "任务分组名称过长")
    private String jobGroup;

    /**
     * 定时语句
     */
    @NotBlank(message = "cron语句不能为空")
    @Size(min = 1, max = 30, message = "cron语句过长")
    private String jobCron;


    /**
     * 任务优先级
     */
    private Integer jobPriority;

    /**
     * 开始时间
     */
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private LocalDateTime jobStartTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private LocalDateTime jobEndTime;

    @Override
    public void validEvent(ValidationResult result, Object data, StringRedisTemplate redisTemplate) {
        if (Objects.nonNull(this.jobEndTime)){
            LocalDateTime startTime;
            if (Objects.isNull(this.jobStartTime)){
                startTime = LocalDateTime.now();
            }else {
                startTime = this.jobStartTime;
            }
            if (jobEndTime.isBefore(startTime)){
                result.setHasErrors(true);
                result.getErrorMsgMap().put("时间校验异常", "结束时间在开始时间之前");
            }
        }
    }
}
