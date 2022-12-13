package com.runjian.timer.service.impl;

import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.exception.BusinessException;
import com.runjian.common.utils.ConstantUtils;
import com.runjian.common.utils.DateUtils;
import com.runjian.timer.constant.JobType;
import com.runjian.timer.handle.DatabaseHandle;
import com.runjian.timer.handle.RestfulHandle;
import com.runjian.timer.listener.GlobalTriggerListener;
import com.runjian.timer.service.TaskCacheService;
import com.runjian.timer.service.AddTimerService;
import com.runjian.timer.vo.JobData;
import com.runjian.timer.vo.request.AddDatabaseReq;
import com.runjian.timer.vo.request.AddRestfulReq;
import com.runjian.timer.vo.response.AddTaskRsp;
import org.quartz.*;
import org.quartz.impl.matchers.EverythingMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author Miracle
 * @date 2022/4/24 11:06
 */

@Service
public class AddTimerServiceImpl implements AddTimerService {

    @Autowired
    private SchedulerFactoryBean schedulerFactoryBean;

    @Autowired
    private TaskCacheService taskCacheService;

    @Autowired
    private GlobalTriggerListener globalTriggerListener;

    @Override
    public AddTaskRsp addRestfulTask(AddRestfulReq request) throws BusinessException {
        // 配置任务处理类
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("method",request.getMethod());
        jobDataMap.put("url",request.getUrl());
        jobDataMap.put("parameter",request.getParameter());
        jobDataMap.put("jobType", JobType.RESTFUL);
        JobDetail jobDetail = JobBuilder.newJob(RestfulHandle.class)
                .withIdentity(request.getJobName(), request.getJobGroup())
                .usingJobData(jobDataMap)
                .build();

        // 提交任务
        submitTimerTask(jobDetail, request);

        // 保存redis数据
        taskCacheService.saveJob(request);
        return AddTaskRsp.jobKeyToResponse(jobDetail.getKey());
    }


    @Override
    public AddTaskRsp addDatabaseTask(AddDatabaseReq request) throws BusinessException {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("username",request.getUsername());
        jobDataMap.put("password",request.getPassword());
        jobDataMap.put("url",request.getUrl());
        jobDataMap.put("sqlString",request.getSqlString());
        jobDataMap.put("jobType", JobType.DATABASE);
        JobDetail jobDetail = JobBuilder.newJob(DatabaseHandle.class)
                .withIdentity(request.getJobName(), request.getJobGroup())
                .usingJobData(jobDataMap)
                .build();

        // 提交任务
        submitTimerTask(jobDetail, request);
        // 保存redis数据
        taskCacheService.saveJob(request);
        return AddTaskRsp.jobKeyToResponse(jobDetail.getKey());
    }

    @Override
    public void addTaskByJson(JobType jobType, String jobDataJson) throws BusinessException {
        try{
            switch (jobType){
                case DATABASE:
                    AddDatabaseReq addDatabaseReq = ConstantUtils.OBJECT_MAPPER.readValue(jobDataJson, AddDatabaseReq.class);
                    addDatabaseTask(addDatabaseReq);
                    break;
                case RESTFUL:
                    AddRestfulReq addRestfulReq = ConstantUtils.OBJECT_MAPPER.readValue(jobDataJson, AddRestfulReq.class);
                    addRestfulTask(addRestfulReq);
                    break;
                default:
                    throw new BusinessException(BusinessErrorEnums.TIMER_ADD_ERROR, "不支持的定时器类型");
            }
        }catch (Exception ex){
            throw new BusinessException(BusinessErrorEnums.TIMER_ADD_ERROR, ex.getMessage());
        }

    }

    /**
     * 提交定时任务
     * @param jobDetail 任务配置
     * @param jobData 任务信息
     * @return
     */
    private JobKey submitTimerTask(JobDetail jobDetail, JobData jobData) throws BusinessException {
        // 设置异常次数，默认为0
        TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger().forJob(jobDetail).withIdentity(jobData.getJobName(),jobData.getJobGroup());
        // 判断是否有设置优先级
        if (Objects.nonNull(jobData.getJobPriority())){
            triggerBuilder.withPriority(jobData.getJobPriority());
        }
        // 判断是否有开始时间
        if (Objects.nonNull(jobData.getJobStartTime())){
            triggerBuilder.startAt(DateUtils.localDateTime2Date(jobData.getJobStartTime()));
        }else {
            triggerBuilder.startNow();
        }

        // 判断是否有结束时间
        if (Objects.nonNull(jobData.getJobEndTime())){
            triggerBuilder.endAt(DateUtils.localDateTime2Date(jobData.getJobEndTime()));
        }
        // 选择计时器类型
        triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(jobData.getJobCron()).withMisfireHandlingInstructionDoNothing()).build();
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        try {
            scheduler.getListenerManager().addJobListener(globalTriggerListener, EverythingMatcher.allJobs());
            scheduler.scheduleJob(jobDetail, triggerBuilder.build());
        } catch (SchedulerException e) {
            throw new BusinessException(BusinessErrorEnums.TIMER_START_ERROR);
        }
        return jobDetail.getKey();
    }
}
