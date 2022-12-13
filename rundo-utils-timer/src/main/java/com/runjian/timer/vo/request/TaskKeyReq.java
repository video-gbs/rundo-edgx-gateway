package com.runjian.timer.vo.request;


import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;


/**
 * 任务Key请求体
 * @author Miracle
 * @date 2019/7/9 14:01
 */
@Data
public class TaskKeyReq {

    @NotBlank(message = "任务名称不能为空")
    @Size(max = 50, min = 1, message = "非法任务名称")
    private String taskName;

    @NotBlank(message = "任务组不能为空")
    @Size(max = 50, min = 1, message = "非法任务组")
    private String taskGroup;

}
