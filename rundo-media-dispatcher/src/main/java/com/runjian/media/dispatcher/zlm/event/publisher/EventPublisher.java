package com.runjian.media.dispatcher.zlm.event.publisher;

import com.runjian.media.dispatcher.zlm.event.ZLMOfflineEvent;
import com.runjian.media.dispatcher.zlm.event.ZLMOnlineEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**    
 * @description:Event事件通知推送器，支持推送在线事件、离线事件
 * @author: swwheihei
 * @date:   2020年5月6日 上午11:30:50     
 */
@Component
public class EventPublisher {

	@Autowired
    private ApplicationEventPublisher applicationEventPublisher;
	


	public void zlmOfflineEventPublish(String mediaServerId){
		ZLMOfflineEvent outEvent = new ZLMOfflineEvent(this);
		outEvent.setMediaServerId(mediaServerId);
		applicationEventPublisher.publishEvent(outEvent);
	}

	public void zlmOnlineEventPublish(String mediaServerId) {
		ZLMOnlineEvent outEvent = new ZLMOnlineEvent(this);
		outEvent.setMediaServerId(mediaServerId);
		applicationEventPublisher.publishEvent(outEvent);
	}



}
