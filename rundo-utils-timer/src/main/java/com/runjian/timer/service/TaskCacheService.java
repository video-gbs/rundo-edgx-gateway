package com.runjian.timer.service;


import com.runjian.common.config.exception.BusinessException;
import com.runjian.timer.constant.JobType;
import com.runjian.timer.vo.JobData;

import java.util.Map;
import java.util.Set;

/**
 * redis缓存服务
 * @author Miracle
 * @date 2021/1/3 15:46
 */
public interface TaskCacheService {

    /**
     * 获取缓存目录
     * @return
     */
    Set<String> getGroupIndex();

    /**
     * 根据组名获取
     * @param redisGroupKey
     * @return
     */
    Map<Object, Object> getJobGroupData(String redisGroupKey);

    /**
     * 获取任务请求信息
     * @param taskGroup
     * @param taskName
     * @param jobType
     * @return
     */
    String getJobRequestJson(String taskGroup, String taskName, JobType jobType);

    /**
     * 保存任务
     */
    void saveJob(JobData jobData) throws BusinessException;

    void deleteJob(String redisGroup, String name);


    /**
     * 删除任务
     * @param taskGroup
     * @param taskName
     * @param jobType
     */
    void deleteJob(String taskGroup, String taskName, JobType jobType);

    /**
     * 批量删除
     * @param group
     * @param jobType
     */
    void deleteGroupJob(String group, JobType jobType);

}
