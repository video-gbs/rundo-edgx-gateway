package com.runjian.common.commonDto.Gb28181Media.resp;

import lombok.Data;

import java.util.List;

/**
 * 视频编码信息
 * @author chenjialing
 */
@Data
public class StreamMediaInfoResp {



    private Integer readerCount;

    private Integer totalReaderCount;

    private Object tracks;
}
