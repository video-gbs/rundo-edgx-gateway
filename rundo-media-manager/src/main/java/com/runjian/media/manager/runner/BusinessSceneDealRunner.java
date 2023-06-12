//package com.runjian.media.manager.runner;
//
//import com.alibaba.fastjson.JSONObject;
//import com.runjian.common.config.response.BusinessSceneResp;
//import com.runjian.common.constant.*;
//import com.runjian.common.utils.redis.RedisCommonUtil;
//import com.runjian.media.manager.mq.dispatcherBusiness.asyncSender.BusinessAsyncSender;
//import com.runjian.media.manager.service.IMediaPlayService;
//import lombok.extern.slf4j.Slf4j;
//import org.redisson.api.RedissonClient;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.core.annotation.Order;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Component;
//import org.springframework.util.CollectionUtils;
//
//import java.time.LocalDateTime;
//import java.util.Map;
//import java.util.Set;
//
///**
// * @author chenjialing
// */
//@Component
//@Slf4j
//@Order(value = 2)
//public class BusinessSceneDealRunner implements CommandLineRunner {
//
//    @Autowired
//    RedissonClient redissonClient;
//
//    @Autowired
//    RedisTemplate redisTemplate;
//
//    @Autowired
//    BusinessAsyncSender businessAsyncSender;
//
//
//    @Autowired
//    IMediaPlayService mediaPlayService;
//
//    @Async
//    @Override
//    public void run(String... args) throws Exception {
//        //常驻进程检测redis hash中所有的待处理的业务场景消息
//        //获取hashmap中的
//
//        while (true){
//
//            try{
//                Map<String, Object> allBusinessMap = RedisCommonUtil.hmget(redisTemplate, StreamBusinessMsgType.DISPATCHER_ALL_SCENE_HASH_KEY);
//                if(CollectionUtils.isEmpty(allBusinessMap)){
//
//                    Thread.sleep(50);
//                    continue;
//                }
//                Set<Map.Entry<String, Object>> entries = allBusinessMap.entrySet();
//                for(Map.Entry entry:  entries){
//                    //获取key与value  value为BusinessSceneResp
//                    BusinessSceneResp businessSceneResp = JSONObject.parseObject((String) entry.getValue(), BusinessSceneResp.class);
//                    GatewayMsgType gatewayMsgType = businessSceneResp.getGatewayMsgType();
//                    //判断状态是否结束，以及是否信令跟踪超时
//                    LocalDateTime time = businessSceneResp.getTime();
//                    BusinessSceneStatusEnum statusEnum =businessSceneResp.getStatus();
//
//                    LocalDateTime now = LocalDateTime.now();
//                    String entrykey = (String)entry.getKey();
//                    if(time.isBefore(now)){
//                        //消息跟踪完毕 删除指定的键值 异步处理对应的mq消息发送,并释放相应的redisson锁
//
//                        commonBusinessDeal(businessSceneResp,entrykey);
//                        if(statusEnum.equals(BusinessSceneStatusEnum.running)){
//                            //推流是失败的信令 进行bye指令发送给网关
//                            mediaPlayService.playBusinessErrorScene(entrykey,businessSceneResp);
//                        }
//
//
//                    }else if(statusEnum.equals(BusinessSceneStatusEnum.end)){
//                        commonBusinessDeal(businessSceneResp,entrykey);
//                    }
//                }
//                Thread.yield();
//            }catch (Exception e){
//                log.error(LogTemplate.ERROR_LOG_TEMPLATE, "业务场景常驻线程处理","异常处理失败",e);
//            }
//
//
//
//        }
//
//    }
//
//    private void commonBusinessDeal(BusinessSceneResp businessSceneResp,String entrykey){
//        log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "业务场景常驻线程处理", "预处理可进行消费的场景信令", businessSceneResp);
//        RedisCommonUtil.hdel(redisTemplate,BusinessSceneConstants.DISPATCHER_ALL_SCENE_HASH_KEY,entrykey);
//        //异步处理消息的mq发送
//        businessAsyncSender.sendforAllScene(businessSceneResp);
//
//    }
//}
