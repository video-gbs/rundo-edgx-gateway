package com.runjian.media.dispatcher.zlm.dto;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 平台账号信息返回消息体
 * */
@Data
public class PlatformAccountRsp {

    private Integer id;

    private String platformId;

    private String platformName;

    private String secretKey;

    private String description;

    private String contact;

    private String contactMobile;

    private Integer enable;

    private String queueId;

    private Integer queueState;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
