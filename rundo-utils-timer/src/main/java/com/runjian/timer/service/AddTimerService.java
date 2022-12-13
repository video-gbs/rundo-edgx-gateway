package com.runjian.timer.service;

import com.runjian.common.config.exception.BusinessException;
import com.runjian.timer.constant.JobType;
import com.runjian.timer.vo.request.AddDatabaseReq;
import com.runjian.timer.vo.request.AddRestfulReq;
import com.runjian.timer.vo.response.AddTaskRsp;

/**
 * @author Miracle
 * @date 2022/4/24 11:01
 */
public interface AddTimerService {

    /**
     * 添加请求定时任务
     * @param request 请求操作定时任务请求体
     * @return
     */
    AddTaskRsp addRestfulTask(AddRestfulReq request) throws BusinessException;


    /**
     * 添加数据库定时任务
     * @param request 数据库操作定时任务请求体
     * @return 任务名称与任务组
     */
    AddTaskRsp addDatabaseTask(AddDatabaseReq request) throws BusinessException;

    /**
     * 添加任务
     * @param jobType
     * @param jobDataJson
     */
    void addTaskByJson(JobType jobType, String jobDataJson) throws BusinessException;
}
