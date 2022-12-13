package com.runjian.common.constant;

import lombok.Data;

import java.io.Serializable;

/**
 * 设备录像同步的缓存信息
 * @author chenjialing
 */
@Data
public class DownloadStreamInfoDto implements Serializable {

    private String app;

    private String stream;

    private String deviceId;

    private String channelId;
    /**
     * 下载进度
     */
    private double progress;
    /**
     * 浏览器id
     */
    private  String browserId;

    /**
     * username
     */
    private  String username;


}
