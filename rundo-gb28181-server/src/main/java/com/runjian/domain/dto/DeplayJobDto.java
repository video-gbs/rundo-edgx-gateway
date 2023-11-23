package com.runjian.domain.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class DeplayJobDto<T> implements Serializable {
    /**
     * 延时任务执行所需参数
     */
    private T param;
    /**
     * 延时任务执行类
     */
    private Class clazz;
}
