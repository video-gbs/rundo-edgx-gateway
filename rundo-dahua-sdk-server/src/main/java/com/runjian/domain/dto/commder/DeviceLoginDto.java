package com.runjian.domain.dto.commder;

import lombok.Data;
import org.json.JSONObject;

/**
 * 设备注册返回的信息
 * @author chenjialing
 */
@Data
public class DeviceLoginDto {

    JSONObject deviceinfoV40;

    private int lUserId;

    private int errorCode = -1;


}
