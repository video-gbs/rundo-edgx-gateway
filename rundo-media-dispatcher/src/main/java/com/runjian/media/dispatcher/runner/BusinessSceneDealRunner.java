package com.runjian.media.dispatcher.runner;

import com.alibaba.fastjson.JSONObject;
import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.common.constant.BusinessSceneConstants;
import com.runjian.common.constant.BusinessSceneStatusEnum;
import com.runjian.common.constant.GatewayMsgType;
import com.runjian.common.constant.LogTemplate;
import com.runjian.common.utils.redis.RedisCommonUtil;
import com.runjian.media.dispatcher.mq.dispatcherBusiness.asyncSender.BusinessAsyncSender;
import com.runjian.media.dispatcher.service.IMediaPlayService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
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
@Order(value = 2)
public class BusinessSceneDealRunner implements CommandLineRunner {

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    BusinessAsyncSender businessAsyncSender;


    @Autowired
    IMediaPlayService mediaPlayService;



    @Async
    @Override
    public void run(String... args) throws Exception {
        //常驻进程检测redis hash中所有的待处理的业务场景消息
        //获取hashmap中的

        while (true){
            try{
                Map<String, Object> allBusinessMap = RedisCommonUtil.hmget(redisTemplate, BusinessSceneConstants.DISPATCHER_ALL_SCENE_HASH_KEY);
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
                        if(time.isBefore(now)){
                            //消息跟踪完毕 删除指定的键值 异步处理对应的mq消息发送,并释放相应的redisson锁

                            commonBusinessDeal(businessSceneResp,entrykey,deleteKeyFlag);
                            if(statusEnum.equals(BusinessSceneStatusEnum.running)){
                                //推流是失败的信令 进行bye指令发送给网关
                                mediaPlayService.playBusinessErrorScene(entrykey,businessSceneResp);
                            }
                            deleteKeyFlag = true;

                        }else if(statusEnum.equals(BusinessSceneStatusEnum.end)){
                            commonBusinessDeal(businessSceneResp,entrykey,deleteKeyFlag);
                            deleteKeyFlag = true;

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

    private void commonBusinessDeal(BusinessSceneResp businessSceneResp,String entrykey,Boolean deleteKeyFlag){
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
        //区分mq与restful接口
        //异步处理消息的mq发送
        businessAsyncSender.sendforAllScene(businessSceneResp);
        //同类key消息请求就删除一次
        if(!deleteKeyFlag){
            RedisCommonUtil.hdel(redisTemplate,BusinessSceneConstants.DISPATCHER_ALL_SCENE_HASH_KEY,entrykey);

        }
    }
}
