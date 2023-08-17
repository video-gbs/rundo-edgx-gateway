package com.runjian.sdk.module.event;

import com.runjian.sdk.module.jnaDto.AlarmIntellectDto;
import org.springframework.context.ApplicationEvent;

/**
 * 网关注册消息通知
 * @author chenjialing
 */
public class AlarmIntellectEvent extends ApplicationEvent {
    public AlarmIntellectEvent(Object source) {
        super(source);
    }

    private AlarmIntellectDto alarmIntellectDto;

    public AlarmIntellectDto getAllarmIntellectDto() {
        return alarmIntellectDto;
    }

    public void setAllarmIntellectDto(AlarmIntellectDto alarmIntellectDto) {
        this.alarmIntellectDto = alarmIntellectDto;
    }
}
