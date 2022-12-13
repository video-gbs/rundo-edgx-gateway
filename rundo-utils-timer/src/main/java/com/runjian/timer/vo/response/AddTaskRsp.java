package com.runjian.timer.vo.response;

import lombok.Data;
import org.quartz.JobKey;

/**
 * @author Miracle
 * @date 2022/4/24 11:02
 */
@Data
public class AddTaskRsp {

    /**
     * 任务名称
     */
    private String jobName;

    /**
     * 任务组
     */
    private String jobGroup;

    /**
     * 转化返回请求体
     * @param jobKey
     * @return
     */
    public static AddTaskRsp jobKeyToResponse(JobKey jobKey){
        AddTaskRsp response = new AddTaskRsp();
        response.setJobGroup(jobKey.getGroup());
        response.setJobName(jobKey.getName());
        return response;
    }
}
