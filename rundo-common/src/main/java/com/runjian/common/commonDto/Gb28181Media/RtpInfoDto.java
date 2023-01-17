package com.runjian.common.commonDto.Gb28181Media;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 国标28181相关的创建端口的参数
 * @author chenjialing
 */
@Data
public class RtpInfoDto {

    @NotNull(message = "流id不得为null")
    private String streamId;

    @NotNull(message = "流媒体id不得为null")
    private String mediaServerId;

    @NotNull(message = "流媒体应用不得为null")
    private String app;




}
