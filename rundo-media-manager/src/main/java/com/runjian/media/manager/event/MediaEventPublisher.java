package com.runjian.media.manager.event;

import com.runjian.media.manager.dto.entity.MediaServerEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**    
 * @description:Event事件通知推送器，支持推送在线事件、离线事件
 * @author: swwheihei
 * @date:   2020年5月6日 上午11:30:50     
 */
@Component
public class MediaEventPublisher {

	@Autowired
    private ApplicationEventPublisher applicationEventPublisher;
	


	public void meidiaOfflineEventPublish(MediaServerEntity mediaServerEntity){
		MediaOfflineEvent outEvent = new MediaOfflineEvent(this);
		outEvent.setMediaServerEntity(mediaServerEntity);
		applicationEventPublisher.publishEvent(outEvent);
	}

	public void meidiaOnlineEventPublish(MediaServerEntity mediaServerEntity) {
		MediaOnlineEvent outEvent = new MediaOnlineEvent(this);
		outEvent.setMediaServerEntity(mediaServerEntity);
		applicationEventPublisher.publishEvent(outEvent);
	}



}
