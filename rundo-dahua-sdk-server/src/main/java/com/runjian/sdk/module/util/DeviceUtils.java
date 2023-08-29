package com.runjian.sdk.module.util;

import com.runjian.conf.constant.DeviceTypeEnum;
import com.runjian.sdk.module.contant.DeviceCharsetEnum;
import org.springframework.util.ObjectUtils;

import java.util.Objects;

/**
 * @author chenjialing
 */
public class DeviceUtils {

    //设备大类备是属于哪个产品线，0 保留，1-50 DVR，51-100 DVS，101-150 NVR，151-200 IPC，65534 其他，具体分类方法见《设备类型对应序列号和类型值.docx》

    public static DeviceTypeEnum checkDeviceType(short wDevClass){

        if(wDevClass == 0){

            return DeviceTypeEnum.HIKVISION_UNKNOWN;
        }else if(1<= wDevClass &&  wDevClass<=50) {

            return DeviceTypeEnum.HIKVISION_DVR;
        }else if(51<= wDevClass && wDevClass<=100){

            return DeviceTypeEnum.HIKVISION_DVS;
        }else if(101<= wDevClass && wDevClass<=150){
            return DeviceTypeEnum.HIKVISION_NVR;
        }else if(151<= wDevClass && wDevClass<=200){
            return DeviceTypeEnum.HIKVISION_IPC;
        }else {
            return DeviceTypeEnum.HIKVISION_UNKNOWN;
        }
    }

    public static String getCharset(int charsetType){
        DeviceCharsetEnum charsetEnum = DeviceCharsetEnum.getTypeByTypeId(charsetType);
        if(ObjectUtils.isEmpty(charsetEnum)){
            return null;
        }
        return charsetEnum.getTypeName();
    }
}
