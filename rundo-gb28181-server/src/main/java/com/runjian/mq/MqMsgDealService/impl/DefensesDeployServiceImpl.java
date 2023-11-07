package com.runjian.mq.MqMsgDealService.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.constant.BusinessSceneConstants;
import com.runjian.common.constant.GatewayBusinessMsgType;
import com.runjian.common.constant.LogTemplate;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.gb28181.bean.Device;
import com.runjian.mq.MqMsgDealService.IMqMsgDealServer;
import com.runjian.mq.MqMsgDealService.IMsgProcessorService;
import com.runjian.service.IDeviceService;
import com.runjian.service.IRedisCatchStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.sip.InvalidArgumentException;
import javax.sip.SipException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @author chenjialing
 */
@Component
@Slf4j
public class DefensesDeployServiceImpl implements InitializingBean, IMsgProcessorService {

    @Autowired
    IMqMsgDealServer iMqMsgDealServer;

    @Autowired
    IDeviceService deviceService;


    @Autowired
    IRedisCatchStorageService redisCatchStorageService;

    String control = "SetGuard";
    @Override
    public void afterPropertiesSet() throws Exception {
        iMqMsgDealServer.addRequestProcessor(GatewayBusinessMsgType.CHANNEL_DEFENSES_DEPLOY.getTypeName(),this);
    }

    @Override
    public void process(CommonMqDto commonMqDto) {
        JSONObject dataJson = (JSONObject) commonMqDto.getData();
        log.info("布防={}",dataJson);

        JSONObject dataMapJson = dataJson.getJSONObject("dataMap");
        String msgId = commonMqDto.getMsgId();

        String businessSceneKey = GatewayBusinessMsgType.CHANNEL_DEFENSES_DEPLOY.getTypeName()+BusinessSceneConstants.SCENE_SEM_KEY+msgId;
        redisCatchStorageService.addBusinessSceneKey(businessSceneKey,GatewayBusinessMsgType.CHANNEL_DEFENSES_DEPLOY,msgId,0,null);
        if(!ObjectUtils.isEmpty(dataMapJson)){
            HashMap<String, List<String>> stringArraysHashMap = new HashMap<>();
            for(String key: dataMapJson.keySet()){
                JSONArray channelArr = dataMapJson.getJSONArray(key);

                    if (!ObjectUtils.isEmpty(channelArr)){
                        ArrayList<String> channelList = new ArrayList<>();
                        channelArr.forEach(channelObj->{
                            String channelId = (String)channelObj;


                            try {
                                deviceService.guardAlarm(key,channelId,control,null);
                            } catch (Exception e) {
                                log.error(LogTemplate.ERROR_LOG_TEMPLATE,"布防失败",e);
                                channelList.add(channelId);
                            }
                        });
                        if(!ObjectUtils.isEmpty(channelList)){
                            stringArraysHashMap.put(key,channelList);

                        }
                    }


            }
            if(!ObjectUtils.isEmpty(stringArraysHashMap)){
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,BusinessErrorEnums.UNKNOWN_ERROR,stringArraysHashMap);
            }else {
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,BusinessErrorEnums.SUCCESS,null);
            }


        }else {
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,BusinessErrorEnums.SUCCESS,null);

        }

    }


}
