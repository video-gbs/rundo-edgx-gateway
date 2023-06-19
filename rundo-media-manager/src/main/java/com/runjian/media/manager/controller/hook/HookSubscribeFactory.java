package com.runjian.media.manager.controller.hook;


import com.alibaba.fastjson.JSONObject;

/**
 * hook 订阅工厂
 * @author lin
 */
public class HookSubscribeFactory {

    public static HookSubscribeForStreamChange onStreamArrive(String app, String stream,  String mediaServerId) {
        HookSubscribeForStreamChange hookSubscribe = new HookSubscribeForStreamChange();
        JSONObject subscribeKey = new JSONObject();
        subscribeKey.put("app", app);
        subscribeKey.put("streamId", stream);
        subscribeKey.put("mediaServerId", mediaServerId);
        hookSubscribe.setContent(subscribeKey);

        return hookSubscribe;
    }

    public static HookSubscribeForStreamDisconnect onStreamDisconnect(String app, String stream,String mediaServerId) {
        HookSubscribeForStreamDisconnect hookSubscribe = new HookSubscribeForStreamDisconnect();
        JSONObject subscribeKey = new JSONObject();
        subscribeKey.put("app", app);
        subscribeKey.put("streamId", stream);
        subscribeKey.put("mediaServerId", mediaServerId);
        hookSubscribe.setContent(subscribeKey);

        return hookSubscribe;
    }

}
