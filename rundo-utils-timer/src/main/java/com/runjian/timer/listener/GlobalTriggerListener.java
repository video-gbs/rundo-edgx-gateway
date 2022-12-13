package com.runjian.timer.listener;


import com.runjian.timer.constant.JobType;
import com.runjian.timer.service.TaskCacheService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Objects;

/**
 * @author Miracle
 * @date 2022/4/25 9:19
 */
@Slf4j
@Component
public class GlobalTriggerListener implements JobListener {

    @Autowired
    private TaskCacheService taskCacheService;

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext jobExecutionContext) {

    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext jobExecutionContext) {

    }

    @Override
    public void jobWasExecuted(JobExecutionContext jobExecutionContext, JobExecutionException e) {
        Object jobType = jobExecutionContext.getJobDetail().getJobDataMap().get("jobType");
        JobKey key = jobExecutionContext.getJobDetail().getKey();
        if (Objects.isNull(jobType)){
            log.error("{} -> TaskGroup:{} TaskName:{} RunResult:{}", getName(), key.getGroup(), key.getName() , "找不到操作类型");
        }

        // 判断是否执行完毕
        if (Objects.isNull(jobExecutionContext.getNextFireTime())){
            taskCacheService.deleteJob(key.getGroup(), key.getName(), (JobType) jobType);
            log.info("{} -> TaskGroup:{} TaskName:{} RunResult:{}", getName(), key.getGroup(), key.getName() , "定时任务已执行完成，已删除缓存信息");
        }
    }
}
