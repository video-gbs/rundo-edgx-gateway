package com.runjian.common.commonDto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * @author chenjialing
 */
@Data
public class StreamPlayDto {
    /**
     * 流id
     */
    private String streamId;

    @NotNull(message = "设备id不得为null")
    private String deviceId;

    @NotNull(message = "通道id不得为null")
    private String channelId;
    /**
     * 视频流0，音频流1
     */
    private Integer mediaType;

}
