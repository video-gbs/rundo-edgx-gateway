package com.runjian.media.manager.dto.resp;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * 网关信息注册返回
 * @author chenjialing
 */
@Data
public class MediaPlayInfoRsp {

    /**
     * 网关序列号
     */
    private String app;;

    private String streamId;


    private Boolean recordMp4;

    private String sourceUrl;

    private String sourceType;

    private Integer readerCount;

    private String videoCodec;

    private Integer width;

    private Integer height;

    private Integer networkType;


    private String audioCodec;


    private Integer audioChannels;

    private Integer audioSampleRate;


    private JSONObject url;




}
