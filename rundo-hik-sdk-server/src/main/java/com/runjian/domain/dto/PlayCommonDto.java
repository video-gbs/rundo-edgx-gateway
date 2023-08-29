package com.runjian.domain.dto;

import lombok.Data;

/**
 * @author chenjialing
 */
@Data
public class PlayCommonDto {

    private int lUserId;
    private int channelNum;
    /**
     * 设备ip
     */
    private String deviceIp;
    /**
     * 登陆密码
     */
    private String devicepassword;
    /**
     * 设备端口
     */
    private long devicePort;
    /**
     * 设备登陆用户
     */
    private String deviceUser;
}
