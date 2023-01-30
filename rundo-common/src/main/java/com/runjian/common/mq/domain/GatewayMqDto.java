package com.runjian.common.mq.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.CommonResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 网关传输消息体
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GatewayMqDto{


    /**
     * 网关序列号
     */
    private String serialNum;

    /**
     * 消息类型
     */
    private String msgType;

    /**
     * 消息ID
     */
    private String msgId;

    /**
     * time
     */
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private LocalDateTime time;

    /**
     * 消息码
     */
    private int code = BusinessErrorEnums.SUCCESS.getErrCode();

    /**
     * 消息
     */
    private String msg =BusinessErrorEnums.SUCCESS.toString();
    /**
     * 数据
     */
    private Object data;

}

