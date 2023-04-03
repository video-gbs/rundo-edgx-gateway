package com.runjian.common.commonDto.Gateway.req;

import com.runjian.common.constant.PresetOperationTypeEnum;
import lombok.Data;

/**
 * ptz/设备控制操作请求指令
 * @author chenjialing
 */
@Data
public class PresetControlReq {

    String deviceId;
    String channelId;
    private String presetId;
    private String presetOperationType;


}
