package com.runjian.media.manager.dto.req;

import lombok.Data;

/**
 * @author chenjialing
 */
@Data
public class CreateServerReq {

    private String app;;

    private String streamId;

    private Integer port;

    private Integer enableTcp;

    private Integer enableMp4;



}
