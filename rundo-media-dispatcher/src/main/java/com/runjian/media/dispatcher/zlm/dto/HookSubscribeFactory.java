package com.runjian.media.dispatcher.zlm.dto;


import com.alibaba.fastjson.JSONObject;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * hook 订阅工厂
 * @author lin
 */
public class HookSubscribeFactory {

    public static HookSubscribeForStreamChange on_stream_changed(String app, String stream, boolean regist, String scheam, String mediaServerId) {
        HookSubscribeForStreamChange hookSubscribe = new HookSubscribeForStreamChange();
        JSONObject subscribeKey = new JSONObject();
        subscribeKey.put("app", app);
        subscribeKey.put("stream", stream);
        subscribeKey.put("regist", regist);
        if (scheam != null) {
            subscribeKey.put("schema", scheam);
        }
        subscribeKey.put("mediaServerId", mediaServerId);
        hookSubscribe.setContent(subscribeKey);

        return hookSubscribe;
    }

    public static HookSubscribeForStreamChange onRecordMp4(String app, String stream, String mediaServerId) {
        HookSubscribeForStreamChange hookSubscribe = new HookSubscribeForStreamChange();
        JSONObject subscribeKey = new JSONObject();
        subscribeKey.put("app", app);
        subscribeKey.put("stream", stream);
        subscribeKey.put("mediaServerId", mediaServerId);
        hookSubscribe.setContent(subscribeKey);
        //最大1小时的过期时间
        Instant expiresInstant = Instant.now().plusSeconds(TimeUnit.HOURS.toSeconds(1));
        hookSubscribe.setExpires(expiresInstant);
        return hookSubscribe;
    }

    public static HookSubscribeForServerStarted on_server_started() {
        HookSubscribeForServerStarted hookSubscribe = new HookSubscribeForServerStarted();
        hookSubscribe.setContent(new JSONObject());

        return hookSubscribe;
    }
}
