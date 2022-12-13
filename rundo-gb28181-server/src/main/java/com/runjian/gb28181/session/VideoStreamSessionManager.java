package com.runjian.gb28181.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**    
 * @description:视频流session管理器，管理视频预览、预览回放的通信句柄 
 * @author: swwheihei
 * @date:   2020年5月13日 下午4:03:02     
 */
@Component
public class VideoStreamSessionManager {



	public enum SessionType {
		play,
		playback,
		download
	}

	private final Logger logger = LoggerFactory.getLogger(VideoStreamSessionManager.class);
	/**
	 * 添加一个点播/回放的事务信息
	 * 后续可以通过流Id/callID
	 * @param deviceId 设备ID
	 * @param channelId 通道ID
	 * @param callId 一次请求的CallID
	 * @param stream 流名称
	 * @param mediaServerId 所使用的流媒体ID
	 * @param response 回复
	 */

}
