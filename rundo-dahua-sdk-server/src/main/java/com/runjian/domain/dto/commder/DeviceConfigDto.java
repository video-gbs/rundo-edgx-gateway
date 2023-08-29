package com.runjian.domain.dto.commder;

import lombok.Data;
import org.json.JSONObject;

/**
 * 设备注册返回的信息
 * @author chenjialing
 */
@Data
public class DeviceConfigDto {

    JSONObject devicecfgV40;

    private int errorCode = 0;
}
