package com.runjian.media.manager.dto.dto.hook;

import lombok.Data;

/**
 * @author chenjialing
 */
@Data
public class StreamChangeDto {

    	private String app;
        private String streamId;
        private String mediaServerId;
        private Integer isTcp;
        private Integer key;
}
