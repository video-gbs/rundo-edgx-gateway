package com.runjian.common.commonDto.Gateway.dto;

import lombok.Data;

@Data
public class GatewayTask {
    /**
     * 调度服务地址
     */
    private Long id;

    private Long threadId;

    private String msgId;



    private String businessKey;


    private Integer code;


    private String msg;

    private String msgIdList;

    private String msgType;


    private Integer status;

    private Integer sourceType;

    private String detail;

}
