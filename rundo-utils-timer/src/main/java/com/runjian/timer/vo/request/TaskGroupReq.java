package com.runjian.timer.vo.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @author Miracle
 * @date 2022/4/25 10:40
 */
@Data
public class TaskGroupReq {
    @NotBlank(message = "任务组不能为空")
    @Size(max = 50, min = 1, message = "非法任务组")
    private String taskGroup;
}
