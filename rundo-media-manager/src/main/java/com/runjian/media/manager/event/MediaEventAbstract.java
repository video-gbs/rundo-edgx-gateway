package com.runjian.media.manager.event;

import com.runjian.media.manager.dto.entity.MediaServerEntity;
import lombok.Data;
import org.springframework.context.ApplicationEvent;

@Data
public abstract class MediaEventAbstract extends ApplicationEvent {


    private static final long serialVersionUID = 1L;

    private MediaServerEntity mediaServerEntity;


    public MediaEventAbstract(Object source) {
        super(source);
    }

}
