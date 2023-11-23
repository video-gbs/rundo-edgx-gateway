package com.runjian.common.commonDto.Gb28181Media.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 点播传参
 * @author chenjialing
 */
@Data
public class MediaPictureDownloadReq extends MediaPlayReq {

    /**
     * 截图时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    String time;

    /**
     * 上传id
     */
    String uploadId;
    /**
     * 上传ul
     */
    String uploadUrl;
}
