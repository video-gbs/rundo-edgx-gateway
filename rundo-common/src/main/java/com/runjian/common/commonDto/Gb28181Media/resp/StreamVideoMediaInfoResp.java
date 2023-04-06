package com.runjian.common.commonDto.Gb28181Media.resp;

import lombok.Data;

import java.util.List;

/**
 * 视频编码信息
 * @author chenjialing
 */
@Data
public class StreamVideoMediaInfoResp {

    /**
     *  # H264 = 0, H265 = 1, AAC = 2, G711A = 3, G711U = 4
     */

    /**
     * 编码名称
     */
    private String codecName;
    /**
     * 编码类型名称
     */

    /**
     * 编码类型 # Video = 0, Audio = 1
     */
    private Integer codecType;
    /**
     * 轨道是否准备就绪
     */
    private Boolean ready;
    /**
     * 轨道是否准备就绪
     */
    private Integer fps;
    /**
     * 视频高
     */
    private Integer height;
    /**
     * 视频宽
     */
    private Integer width;
}
