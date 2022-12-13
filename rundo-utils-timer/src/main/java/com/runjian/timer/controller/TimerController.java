package com.runjian.timer.controller;

import com.runjian.common.config.response.CommonResponse;
import com.runjian.common.config.exception.BusinessException;
import com.runjian.common.validator.ValidatorService;
import com.runjian.timer.constant.JobType;
import com.runjian.timer.service.AddTimerService;
import com.runjian.timer.service.TimerService;
import com.runjian.timer.vo.request.AddDatabaseReq;
import com.runjian.timer.vo.request.AddRestfulReq;
import com.runjian.timer.vo.request.TaskGroupReq;
import com.runjian.timer.vo.request.TaskKeyReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author Miracle
 * @date 2022/4/24 17:40
 */

@Slf4j
@RestController
@RequestMapping("/timer")
public class TimerController {

    @Autowired
    private ValidatorService validatorService;

    @Autowired
    private TimerService timerService;

    @Autowired
    private AddTimerService addTimerService;


    /**
     * 获取任务消息
     * @param taskGroup 分组名
     * @param taskName 任务名
     * @param jobTypeCode 任务类型 1-数据库操作型 2-请求型
     * @return
     * @throws BusinessException
     */
    @GetMapping("/data")
    public CommonResponse<Object> getTaskData(@RequestParam String taskGroup,
                                      @RequestParam String taskName,
                                      @RequestParam Integer jobTypeCode) throws BusinessException {
        return CommonResponse.success(timerService.getJob(taskGroup, taskName, JobType.getJobTypeByCode(jobTypeCode)));
    }

    /**
     * 获取全部任务信息
     * @return
     * @throws BusinessException
     */
    @GetMapping("/data/all")
    public CommonResponse<Map<Object, Object>> getTaskData(@RequestParam Long planId, @RequestParam String groupType) throws BusinessException {
        return CommonResponse.success(timerService.getAllJob(planId, groupType));
    }


    /**
     * 添加数据库定时任务
     * @param request 添加数据库任务请求体
     * @return
     * @throws BusinessException
     */
    @PostMapping("/add/database")
    public CommonResponse postAddDatabaseScheduler(@RequestBody AddDatabaseReq request) throws BusinessException {
        validatorService.validateRequest(request);
        addTimerService.addDatabaseTask(request);
        return CommonResponse.success();
    }

    /**
     * 添加请求定时任务
     * @param request 添加请求任务请求体
     * @return
     * @throws BusinessException
     */
    @PostMapping("/add/request")
    public CommonResponse postAddRequestScheduler(@RequestBody AddRestfulReq request) throws BusinessException {
        validatorService.validateRequest(request);
        addTimerService.addRestfulTask(request);
        return CommonResponse.success();
    }

    /**
     * 暂停定时任务
     * @param request 任务Key请求体
     * @return
     * @throws BusinessException
     */
    @PutMapping("/stop/job")
    public CommonResponse putStopJob(@RequestBody TaskKeyReq request) throws BusinessException {
        validatorService.validateRequest(request);
        timerService.stopJob(request.getTaskGroup(), request.getTaskName());
        return CommonResponse.success();
    }

    /**
     * 暂停所有任务
     * @return
     * @throws BusinessException
     */
    @PutMapping("/stop/group")
    public CommonResponse putStopGroup(@RequestBody TaskGroupReq request) throws BusinessException {
        validatorService.validateRequest(request);
        timerService.stopGroupJob(request.getTaskGroup());
        return CommonResponse.success();
    }

    /**
     * 恢复任务
     * @param request 任务Key请求体
     * @return
     * @throws BusinessException
     */
    @PutMapping("/resume/job")
    public CommonResponse putResumeJob(@RequestBody TaskKeyReq request) throws BusinessException {
        validatorService.validateRequest(request);
        timerService.resumeJob(request.getTaskGroup(), request.getTaskName());
        return CommonResponse.success();
    }

    /**
     * 恢复所有任务
     * @return
     * @throws BusinessException
     */
    @PutMapping("/resume/group")
    public CommonResponse putResumeGroup(@RequestBody TaskGroupReq request) throws BusinessException {
        timerService.resumeGroupJob(request.getTaskGroup());
        return CommonResponse.success();
    }

    /**
     * 删除任务
     * @param taskGroup 分组名
     * @param taskName 任务名
     * @param jobTypeCode 任务类型 1-数据库操作型 2-请求型
     * @return
     * @throws BusinessException
     */
    @DeleteMapping("/delete/job")
    public CommonResponse deleteJob( @RequestParam String taskGroup,@RequestParam String taskName, @RequestParam Integer jobTypeCode) throws BusinessException {
        timerService.deleteJob(taskGroup, taskName, JobType.getJobTypeByCode(jobTypeCode));
        return CommonResponse.success();
    }

    /**
     * 删除分组任务
     * @param taskGroup 分组名
     * @param jobTypeCode 任务类型 1-数据库操作型 2-请求型
     * @return
     * @throws BusinessException
     */
    @DeleteMapping("/delete/group")
    public CommonResponse deleteGroupJob(@RequestParam String taskGroup, @RequestParam Integer jobTypeCode) throws BusinessException {
        timerService.deleteGroupJob(taskGroup, JobType.getJobTypeByCode(jobTypeCode));
        return CommonResponse.success();
    }
}
