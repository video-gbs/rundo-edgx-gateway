package com.runjian.timer.service;

import com.runjian.common.config.exception.BusinessException;
import com.runjian.timer.constant.JobType;

import java.util.Map;

/**
 * @author Miracle
 * @date 2022/4/24 16:09
 */
public interface TimerService {

    /**
     *  获取定时任务信息
     * @param taskGroup 任务分组
     * @param taskName 任务名称
     * @param jobType 任务类型
     */
    Object getJob(String taskGroup, String taskName, JobType jobType) throws BusinessException;

    /**
     * 获取全部定时任务
     * @return
     */
    Map<Object, Object> getAllJob(Long planId, String groupType);

    /**
     * 暂停所有的任务
     * @return 操作结果
     * @param taskGroup 任务分组
     */
    void stopGroupJob(String taskGroup) throws BusinessException;

    /**
     * 暂停任务
     * @param taskGroup 任务分组
     * @param taskName 任务名称
     * @return
     */
    void stopJob(String taskGroup, String taskName) throws BusinessException;

    /**
     * 恢复所有的任务
     * @return
     */
    void resumeGroupJob(String taskGroup) throws BusinessException;

    /**
     * 恢复任务
     * @param taskGroup 任务分组
     * @param taskName 任务名称
     * @return
     */
    void resumeJob(String taskGroup, String taskName) throws BusinessException;

    /**
     * 删除组任务
     * @param taskGroup 任务分组
     * @param jobType 任务类型
     */
    void deleteGroupJob(String taskGroup, JobType jobType) throws BusinessException;

    /**
     * 删除任务
     * @param taskGroup 任务分组
     * @param taskName 任务名称
     * @return
     */
    void deleteJob(String taskGroup, String taskName, JobType jobType) throws BusinessException;

    /**
     * 修改任务时间
     * @param taskGroup 任务分组
     * @param taskName 任务名称
     * @param cronString cron字符串
     */
    void resetJob(String taskGroup, String taskName, String cronString) throws BusinessException;

    /**
     * 恢复任务
     */
    void recoverAllJob() throws BusinessException;
}
