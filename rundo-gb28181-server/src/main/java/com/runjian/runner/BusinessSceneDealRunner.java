package com.runjian.runner;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.common.constant.BusinessSceneConstants;
import com.runjian.common.constant.BusinessSceneStatusEnum;
import com.runjian.common.constant.GatewayMsgType;
import com.runjian.common.constant.LogTemplate;
import com.runjian.common.utils.redis.RedisCommonUtil;
import com.runjian.mq.gatewayBusiness.asyncSender.GatewayBusinessAsyncSender;
import com.runjian.service.IplayService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author chenjialing
 */
@Component
@Slf4j
@Order(value = 0)
public class BusinessSceneDealRunner implements CommandLineRunner {

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    GatewayBusinessAsyncSender gatewayBusinessAsyncSender;

    @Autowired
    IplayService iplayService;

    @Async
    @Override
    public void run(String... args) throws Exception {
        //常驻进程检测redis hash中所有的待处理的业务场景消息
        //获取hashmap中的

        while (true){
            try{
                Map<String, Object> allBusinessMap = RedisCommonUtil.hmget(redisTemplate, BusinessSceneConstants.ALL_SCENE_HASH_KEY);
                if(CollectionUtils.isEmpty(allBusinessMap)){

                    Thread.sleep(50);
                    continue;

                }
                Set<Map.Entry<String, Object>> entries = allBusinessMap.entrySet();
                for(Map.Entry entry:  entries){
                    //获取key与value  value为BusinessSceneResp
                    List<BusinessSceneResp> businessSceneRespList = JSONObject.parseArray((String) entry.getValue(), BusinessSceneResp.class);
                    String entrykey = (String)entry.getKey();
                    Boolean deleteKeyFlag = false;
                    for (BusinessSceneResp businessSceneResp : businessSceneRespList) {
                        //同一组的消息状态均为一致，一个成功，最后一个发送完毕进行缓存删除

                        //判断状态是否结束，以及是否信令跟踪超时
                        LocalDateTime time = businessSceneResp.getTime();
                        BusinessSceneStatusEnum statusEnum =businessSceneResp.getStatus();

                        LocalDateTime now = LocalDateTime.now();

                        if(time.isBefore(now) || statusEnum.equals(BusinessSceneStatusEnum.end)){
                            //消息跟踪完毕 删除指定的键值 异步处理对应的mq消息发送,并释放相应的redisson锁
                            //释放全局redisson锁
                            //针对点播失败的异常场景，需要：1.自行释放ssrc和2.删除相关的缓存，3.判断是否需要进行设备指令的bye和4.流媒体推流端口的关闭
                            if(businessSceneResp.getGatewayMsgType().equals(GatewayMsgType.PLAY) || businessSceneResp.getGatewayMsgType().equals(GatewayMsgType.PLAY_BACK)){
                                //businessSceneResp.getCode() == BusinessErrorEnums.SIP_SEND_SUCESS.getErrCode()
                                if(businessSceneResp.getCode() == BusinessErrorEnums.SIP_SEND_SUCESS.getErrCode()){
                                    //BusinessErrorEnums.SIP_SEND_SUCESS.getErrCode() 只处理超时的请求
                                    if(time.isBefore(now)){
                                        commonBusinessDeal(businessSceneResp,entrykey);
                                        iplayService.playBusinessErrorScene(entrykey,businessSceneResp);
                                    }
                                }else {
                                    commonBusinessDeal(businessSceneResp,entrykey);
                                }
                            }else {
                                commonBusinessDeal(businessSceneResp,entrykey);
                            }

                        }
                    }
                    //处理完毕，进行缓存删除
                }
                Thread.yield();
            }catch (Exception e){
                log.error(LogTemplate.ERROR_LOG_TEMPLATE, "业务场景常驻线程处理","异常处理失败",e);
            }
        }

    }

    private void commonBusinessDeal(BusinessSceneResp businessSceneResp,String entrykey){
        log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "业务场景常驻线程处理", "预处理可进行消费的场景信令", businessSceneResp);
        long threadId = businessSceneResp.getThreadId();
        RLock lock = redissonClient.getLock(entrykey);
        if(!ObjectUtils.isEmpty(lock)){
            try {
                lock.unlockAsync(threadId);

            }catch (Exception e){
                log.error(LogTemplate.ERROR_LOG_TEMPLATE, "业务场景常驻线程处理","redis解锁异常处理失败",e);
            }
        }
        //异步处理消息的mq发送
        gatewayBusinessAsyncSender.sendforAllScene(businessSceneResp);
            //同类key消息请求就删除一次
        if(!ObjectUtils.isEmpty(RedisCommonUtil.hget(redisTemplate,BusinessSceneConstants.ALL_SCENE_HASH_KEY,entrykey))){
            RedisCommonUtil.hdel(redisTemplate,BusinessSceneConstants.ALL_SCENE_HASH_KEY,entrykey);

        }
    }
}
