package com.runjian.media.manager.dto.dto.hook;

import lombok.Data;

/**
 * @author chenjialing
 */
@Data
public class OnPublishDto {

    	private String app;

        private String streamId;

        private String mediaServerId;
        /**
         * 协议区分
         */
        private Integer networkType;

        private Integer key;

        private String ip;

        private Integer port;

        private String params;


}
