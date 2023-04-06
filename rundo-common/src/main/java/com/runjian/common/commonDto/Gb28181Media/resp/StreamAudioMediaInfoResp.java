package com.runjian.common.commonDto.Gb28181Media.resp;

import lombok.Data;

import java.util.List;

/**
 * 视频编码信息
 * @author chenjialing
 */
@Data
public class StreamAudioMediaInfoResp {

    /**
     * 音频通道数
     */
   private Integer channels;
    /**
     *  # H264 = 0, H265 = 1, AAC = 2, G711A = 3, G711U = 4
     */

    /**
     * 编码名称
     */
    private String codecName;

    /**
     * 编码类型 # Video = 0, Audio = 1
     */
    private Integer codecType;
    /**
     * 轨道是否准备就绪
     */
    private Boolean ready;
    /**
     * 音频采样位数
     */
    private Integer sampleBit;
    /**
     * 音频采样率
     */
    private Integer sampleRate;
}
