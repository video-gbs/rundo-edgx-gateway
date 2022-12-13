package com.runjian.domain.dto;


import lombok.Data;


/**
 * @author Miracle
 * @date 2022/9/6 9:33
 */
@Data
public class DeviceCompatibleDto {

    /**
     * 通道id
     */
    private int id;

    private String deviceId;

    /**
     * 0是华为nvr800
     */
    private int type;

    private int deleted;

}
