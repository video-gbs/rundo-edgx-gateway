package com.runjian.sdk.module.jnaDto;

import lombok.Data;

@Data
public class AlarmIntellectRespDto {


    private String eventId;

    private Long eventTime;

    /**
     * 1告警发生，2告警消除
     */
    private int eventType;


    /**
     * 告警的信息
     */
    private int eventCode;

    private String eventTitle;

    /**
     *  0 无图片（几乎不可能吧），1 公网url地址（如果原始平台给的是内网url，需边缘节点上传到公网的oss后，把url替换掉，再上报给 rich-acc系统），2 图片内容的base64码 |
     */
    private int imageType;


    private String captureImage;

    private String deviceId;

    private String channelId;





}
