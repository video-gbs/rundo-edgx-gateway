package com.runjian.media.dispatcher.zlm.event;

import com.runjian.common.constant.LogTemplate;
import com.runjian.media.dispatcher.zlm.service.ImediaServerService;
import com.runjian.media.dispatcher.zlm.service.ImediaService;
import com.runjian.media.dispatcher.zlm.service.IplayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class ZLMStatusEventListener {
	
	private final static Logger logger = LoggerFactory.getLogger(ZLMStatusEventListener.class);


	@Autowired
	private ImediaServerService mediaServerService;

	@Autowired
	private IplayService playService;

	/**
	 * 处理zlm上线
	 * @param event
	 */
	@Async("taskExecutor")
	@EventListener
	public void onApplicationEvent(ZLMOnlineEvent event) {
		logger.info(LogTemplate.PROCESS_LOG_TEMPLATE, "ZLM在线事件监听器", "[ZLM] 上线 ID："+ event.getMediaServerId());
		playService.zlmServerOnline(event.getMediaServerId());
	}

	/**
	 * 处理zlm离线
	 * @param event
	 */
	@Async("taskExecutor")
	@EventListener
	public void onApplicationEvent(ZLMOfflineEvent event) {
		logger.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "ZLM在线事件监听器", "ZLM离线", event.getMediaServerId());
		// 处理ZLM离线
		mediaServerService.zlmServerOffline(event.getMediaServerId());
		playService.zlmServerOffline(event.getMediaServerId());
	}
}
