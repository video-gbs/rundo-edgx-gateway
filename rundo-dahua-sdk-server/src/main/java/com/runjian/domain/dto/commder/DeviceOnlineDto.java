package com.runjian.domain.dto.commder;

import com.runjian.entity.DeviceEntity;
import lombok.Data;
import org.json.JSONObject;

/**
 * 设备注册返回的信息
 * @author chenjialing
 */
@Data
public class DeviceOnlineDto {

    JSONObject deviceinfoV40;

    DeviceEntity deviceEntity;
}
