package com.runjian.timer.service.impl;

import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.exception.BusinessException;
import com.runjian.common.utils.ConstantUtils;
import com.runjian.timer.constant.JobType;
import com.runjian.timer.constant.RedisConstant;
import com.runjian.timer.service.TaskCacheService;
import com.runjian.timer.vo.JobData;
import com.runjian.timer.vo.request.AddDatabaseReq;
import com.runjian.timer.vo.request.AddRestfulReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Miracle
 * @date 2021/1/3 15:48
 */

@Slf4j
@Service
public class TaskCacheServiceImpl implements TaskCacheService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public Set<String> getGroupIndex() {
        Set<String> groupIndex = new HashSet<>();
        for (JobType jobType : JobType.values()){
            Set<String> members = redisTemplate.opsForSet().members(RedisConstant.TIMER_INDEX_GROUP_KEY + jobType.getMsg());
            if (Objects.nonNull(members) && members.size() > 0){
                groupIndex.addAll(members);
            }
        }
        return groupIndex;
    }

    @Override
    public Map<Object, Object> getJobGroupData(String taskGroup) {
        return redisTemplate.opsForHash().entries(taskGroup);
    }

    @Override
    public String getJobRequestJson(String taskGroup, String taskName, JobType jobType) {
        Object value = redisTemplate.opsForHash().get(jobType.getRedisKey() + taskGroup, taskName);
        if (Objects.isNull(value)){
            return null;
        }
        return value.toString();
    }

    @Override
    public void saveJob(JobData jobData) throws BusinessException {
        if (Objects.isNull(jobData)){
            throw new BusinessException(BusinessErrorEnums.VALID_BIND_EXCEPTION_ERROR);
        }
        String redisGroupName;
        try {
            if (jobData instanceof AddDatabaseReq){
                AddDatabaseReq request = (AddDatabaseReq) jobData;
                redisGroupName = JobType.DATABASE.getRedisKey() + request.getJobGroup();
                redisTemplate.opsForHash().put( redisGroupName,jobData.getJobName(), ConstantUtils.OBJECT_MAPPER.writeValueAsString(request));
                redisTemplate.opsForSet().add(RedisConstant.TIMER_INDEX_GROUP_KEY + JobType.DATABASE.getMsg(), redisGroupName);
            }else if (jobData instanceof AddRestfulReq){
                AddRestfulReq request = (AddRestfulReq) jobData;
                redisGroupName = JobType.RESTFUL.getRedisKey() + request.getJobGroup();
                redisTemplate.opsForHash().put(redisGroupName ,jobData.getJobName(), ConstantUtils.OBJECT_MAPPER.writeValueAsString(request));
                redisTemplate.opsForSet().add(RedisConstant.TIMER_INDEX_GROUP_KEY + JobType.RESTFUL.getMsg(), redisGroupName);
            }else {
                throw new BusinessException(BusinessErrorEnums.TIMER_SAVE_TO_JSON_ERROR, "不支持的保存类型");
            }

        }catch (Exception e) {
            throw new BusinessException(BusinessErrorEnums.TIMER_SAVE_TO_JSON_ERROR, e.getMessage());
        }
    }



    @Override
    public void deleteJob(String redisGroup, String name) {
        redisTemplate.opsForHash().delete(redisGroup, name);
    }

    @Override
    public void deleteJob(String group, String name, JobType jobType) {
        deleteJob(jobType.getRedisKey() + group, name);
    }

    @Override
    public void deleteGroupJob(String group, JobType jobType){
        String redisGroupName = jobType.getRedisKey() + group;
        redisTemplate.delete(redisGroupName);
        redisTemplate.opsForSet().remove(RedisConstant.TIMER_INDEX_GROUP_KEY + jobType.getMsg(), redisGroupName);
    }

}
