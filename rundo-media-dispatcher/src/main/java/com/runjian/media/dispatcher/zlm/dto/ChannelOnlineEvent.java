package com.runjian.media.dispatcher.zlm.dto;

import java.text.ParseException;

/**
 * @author lin
 */
public interface ChannelOnlineEvent {

    void run(String app, String stream, String serverId) throws ParseException;
}
