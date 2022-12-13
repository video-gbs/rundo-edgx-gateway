package com.runjian.common.config.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author Miracle
 * @date 2020/2/19 15:07
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = -7072123996206431364L;

    /**
     * 请求状态
     */
    private final Integer state;

    /**
     * 通用异常
     */
    private final BusinessErrorEnums businessErrorEnums;

    /**
     * 详细异常消息
     */
    private final Object errDetail;


    public BusinessException(BusinessErrorEnums businessErrorEnums){
        super();
        this.state = businessErrorEnums.getState();
        this.businessErrorEnums = businessErrorEnums;
        this.errDetail = null;
    }

    public BusinessException(BusinessErrorEnums businessErrorEnums, String errDetail){
        super();
        this.state = businessErrorEnums.getState();
        this.errDetail = errDetail;
        this.businessErrorEnums = businessErrorEnums;
    }
}
