package com.runjian.media.manager.event;

import com.runjian.media.manager.service.IMediaServerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;


/**
 * @description: 在线事件监听器，监听到离线后，修改设备离在线状态。 设备在线有两个来源：
 *               1、设备主动注销，发送注销指令
 *               2、设备未知原因离线，心跳超时
 * @author: swwheihei
 * @date: 2020年5月6日 下午1:51:23
 */
@Component
@Slf4j
public class MediaStatusEventListener {
	

	@Autowired
	private IMediaServerService mediaServerService;


	/**
	 * 处理zlm上线
	 * @param event
	 */
	@Async("taskExecutor")
	@EventListener
	public void onApplicationEvent(MediaOnlineEvent event) {
		// 上线  处理遗留的流同步
//		mediaServerService.connectMediaServer(event.getMediaServerEntity());
	}

	/**
	 * 处理zlm离线
	 * @param event
	 */
	@Async("taskExecutor")
	@EventListener
	public void onApplicationEvent(MediaOfflineEvent event) {
		// 离线
		mediaServerService.mediaServerOffline(event.getMediaServerEntity());

	}
}
