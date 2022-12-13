package com.runjian.timer.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.exception.BusinessException;
import com.runjian.common.utils.ConstantUtils;
import com.runjian.timer.constant.JobType;
import com.runjian.timer.constant.RedisConstant;
import com.runjian.timer.service.AddTimerService;
import com.runjian.timer.service.TaskCacheService;
import com.runjian.timer.service.TimerService;
import com.runjian.timer.vo.request.AddDatabaseReq;
import com.runjian.timer.vo.request.AddRestfulReq;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author Miracle
 * @date 2022/4/24 16:12
 */
@Slf4j
@Service
public class TimerServiceImpl implements TimerService {

    @Autowired
    private SchedulerFactoryBean schedulerFactoryBean;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private TaskCacheService taskCacheService;

    @Autowired
    private AddTimerService addTimerService;



    @PostConstruct
    public void init() throws SchedulerException {
        // 启动定时器
        schedulerFactoryBean.getScheduler().start();
        // 尝试从redis中恢复任务
        recoverAllJob();
    }


    @Override
    public Object getJob(String taskGroup, String taskName, JobType jobType) throws BusinessException {
        String jobRequestJson = taskCacheService.getJobRequestJson(taskGroup, taskName, jobType);
        if (Objects.isNull(jobRequestJson)) {
            throw new BusinessException(BusinessErrorEnums.VALID_NO_OBJECT_FOUND);
        }
        try {
            switch (jobType) {
                case RESTFUL:
                    return ConstantUtils.OBJECT_MAPPER.readValue(jobRequestJson, AddRestfulReq.class);
                case DATABASE:
                    return ConstantUtils.OBJECT_MAPPER.readValue(jobRequestJson, AddDatabaseReq.class);
                default:
                    throw new BusinessException(BusinessErrorEnums.TIMER_SELECT_ERROR, "不支持的请求类型");
            }
        } catch (JsonProcessingException e) {
            throw new BusinessException(BusinessErrorEnums.TIMER_SELECT_ERROR, e.getMessage());
        }

    }

    @Override
    public Map<Object, Object> getAllJob(Long planId, String groupType) {
        Map<Object, Object> objectMap = new HashMap<>();
        for (JobType jobType : JobType.values()) {
            objectMap.putAll(taskCacheService.getJobGroupData(jobType.getRedisKey() + groupType + planId));
        }
        return objectMap;
    }

    @Override
    public void stopGroupJob(String taskGroup) throws BusinessException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        try {
            scheduler.pauseJobs(GroupMatcher.jobGroupEquals(taskGroup));
        } catch (SchedulerException e) {
            throw new BusinessException(BusinessErrorEnums.TIMER_STOP_ERROR);
        }
    }

    @Override
    public void stopJob(String taskGroup, String taskName) throws BusinessException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        try {
            scheduler.pauseJob(JobKey.jobKey(taskName, taskGroup));
        } catch (SchedulerException e) {
            throw new BusinessException(BusinessErrorEnums.TIMER_STOP_ERROR);
        }
    }

    @Override
    public void resumeGroupJob(String taskGroup) throws BusinessException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        try {
            scheduler.resumeJobs(GroupMatcher.jobGroupEquals(taskGroup));
        } catch (SchedulerException e) {
            throw new BusinessException(BusinessErrorEnums.TIMER_RESUME_ERROR);
        }
    }

    @Override
    public void resumeJob(String taskGroup, String taskName) throws BusinessException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        try {
            scheduler.resumeJob(JobKey.jobKey(taskName, taskGroup));
        } catch (SchedulerException e) {
            throw new BusinessException(BusinessErrorEnums.TIMER_RESUME_ERROR);
        }
    }

    @Override
    public void deleteGroupJob(String taskGroup, JobType jobType) throws BusinessException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        try {
            scheduler.deleteJobs(new ArrayList<>(scheduler.getJobKeys(GroupMatcher.jobGroupEquals(taskGroup))));
            taskCacheService.deleteGroupJob(taskGroup, jobType);
        } catch (SchedulerException e) {
            throw new BusinessException(BusinessErrorEnums.TIMER_DELETE_ERROR);
        }
    }

    @Override
    public void deleteJob(String taskGroup, String taskName, JobType jobType) throws BusinessException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        try {
            scheduler.deleteJob(JobKey.jobKey(taskName, taskGroup));

            taskCacheService.deleteJob(taskGroup, taskName, jobType);
        } catch (SchedulerException e) {
            throw new BusinessException(BusinessErrorEnums.TIMER_DELETE_ERROR);
        }
    }

    @Override
    public void resetJob(String taskGroup, String taskName, String cronString) throws BusinessException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        CronTriggerImpl trigger = null;
        try {
            // todo 不支持redis回滚
            scheduler.deleteJob(JobKey.jobKey(taskName, taskGroup));
            TriggerKey triggerKeys = TriggerKey.triggerKey(taskName, taskGroup);
            trigger = new CronTriggerImpl();
            trigger.setCronExpression(cronString);
            trigger.setKey(triggerKeys);
            scheduler.rescheduleJob(triggerKeys, trigger);
        } catch (SchedulerException | ParseException e) {
            throw new BusinessException(BusinessErrorEnums.TIMER_RESUME_ERROR, e.getMessage());
        }
    }

    @Override
    public void recoverAllJob() throws BusinessException {
        // 获取目录
        LocalDateTime nowTime = LocalDateTime.now();
        for (JobType jobType : JobType.values()){
            log.warn("{} Task Data In Recovery", jobType.getMsg());
            Set<String> taskIndex = redisTemplate.opsForSet().members(RedisConstant.TIMER_INDEX_GROUP_KEY + jobType.getMsg());

            if (Objects.isNull(taskIndex) || taskIndex.size() == 0){
                log.warn("{} Task NO DATA", jobType.getMsg());
                continue;
            }

            for (String redisGroupName : taskIndex){
                Map<Object, Object> groupTask = redisTemplate.opsForHash().entries(redisGroupName);
                for (Map.Entry<Object, Object> nameRequest : groupTask.entrySet()){
                    String jobName = nameRequest.getKey().toString();

                    JSONObject jobDataJsonOb = JSONObject.parseObject(nameRequest.getValue().toString());
                    LocalDateTime jobEndTime = jobDataJsonOb.getObject("jobEndTime", LocalDateTime.class);

                    // 判断是否已过期
                    if (Objects.nonNull(jobEndTime) && jobEndTime.isBefore(nowTime)){
                        taskCacheService.deleteJob(redisGroupName, jobName);
                        break;
                    }
                    addTimerService.addTaskByJson(jobType, nameRequest.getValue().toString());
                }
            }
            log.warn("{} Task Data Recover Finish", jobType.getMsg());
        }
    }
}
