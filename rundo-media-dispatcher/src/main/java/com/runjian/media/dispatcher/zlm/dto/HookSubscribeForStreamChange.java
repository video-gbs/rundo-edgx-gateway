package com.runjian.media.dispatcher.zlm.dto;

import com.alibaba.fastjson.JSONObject;

import java.time.Instant;

/**
 * hook订阅-流变化
 * @author lin
 */
public class HookSubscribeForStreamChange implements IHookSubscribe{

    private HookType hookType = HookType.on_stream_changed;

    private JSONObject content;

    private Instant expires;

    @Override
    public HookType getHookType() {
        return hookType;
    }

    @Override
    public JSONObject getContent() {
        return content;
    }

    public void setContent(JSONObject content) {
        this.content = content;
    }

    @Override
    public Instant getExpires() {
        return expires;
    }

    @Override
    public void setExpires(Instant expires) {
        this.expires = expires;
    }
}
