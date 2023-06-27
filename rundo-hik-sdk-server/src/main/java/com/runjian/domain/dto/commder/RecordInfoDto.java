package com.runjian.domain.dto.commder;

import com.runjian.hik.sdklib.HCNetSDK;
import lombok.Data;

import java.util.List;

/**
 * 设备注册返回的信息
 * @author chenjialing
 */
@Data
public class RecordInfoDto {

    RecordAllItem recordAllItem;


    private int errorCode;


}
