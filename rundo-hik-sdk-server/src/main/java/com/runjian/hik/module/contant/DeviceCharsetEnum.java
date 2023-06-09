package com.runjian.hik.module.contant;

import com.runjian.common.constant.DragRoomTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.nio.charset.StandardCharsets;

/**
 * @author chenjialing
 */
@Getter
@ToString
@AllArgsConstructor
public enum DeviceCharsetEnum {
    //字符编码类型（SDK所有接口返回的字符串编码类型，透传接口除外）：0- 无字符编码信息(老设备)，1- GB2312(简体中文)，2- GBK，3- BIG5(繁体中文)，4- Shift_JIS(日文)，5- EUC-KR(韩文)，6- UTF-8，7- ISO8859-1，8- ISO8859-2，9- ISO8859-3，…，依次类推，21- ISO8859-15(西欧)
    GB2312(1, "GB2312"),
    GBK(2, "GBK"),
    BIG5(3, "BIG5"),
    Shift_JIS(4, "Shift_JIS"),
    EUC_KR(5, "EUC-KR"),
    UTF8(6, "UTF-8"),



    ;
    private final Integer code;
    private final String typeName;

    public static DeviceCharsetEnum getTypeByTypeId(Integer id){
        for (DeviceCharsetEnum value : values()){
            if (value.code.equals(id)){
                return value;
            }
        }
        return null;
    }
}
