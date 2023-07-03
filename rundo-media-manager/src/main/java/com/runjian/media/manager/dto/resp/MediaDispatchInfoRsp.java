package com.runjian.media.manager.dto.resp;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * 网关信息注册返回
 * @author chenjialing
 */
@Data
public class MediaDispatchInfoRsp {

    private Integer key;;
    private String app;;

    private String streamId;



    private String sourceUrl;

    private String sourceType;

    private Integer dispatchCount;

    private String videoCodec;

    private String audioCodec;

    private Integer audioChannels;

    private Integer audioSampleRate;

    private Integer width;

    private Integer height;

    private Integer networkType;

    private String dstUrl;

    private Integer dstPort;







}
