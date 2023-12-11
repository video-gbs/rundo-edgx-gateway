//package com.runjian.media.dispatcher.runner;
//
//import com.alibaba.fastjson.JSONObject;
//import com.runjian.common.config.exception.BusinessErrorEnums;
//import com.runjian.common.config.response.BusinessSceneResp;
//import com.runjian.common.config.response.GatewayBusinessSceneResp;
//import com.runjian.common.config.response.StreamBusinessSceneResp;
//import com.runjian.common.constant.*;
//import com.runjian.common.utils.redis.RedisCommonUtil;
//import com.runjian.media.dispatcher.conf.StreamInfoConf;
//import com.runjian.media.dispatcher.conf.mq.DispatcherSignInConf;
//import com.runjian.media.dispatcher.mq.dispatcherBusiness.asyncSender.BusinessAsyncSender;
//import com.runjian.media.dispatcher.service.IMediaPlayService;
//import com.runjian.media.dispatcher.service.IRedisCatchStorageService;
//import lombok.extern.slf4j.Slf4j;
//import org.redisson.api.RLock;
//import org.redisson.api.RedissonClient;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.core.annotation.Order;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Component;
//import org.springframework.util.CollectionUtils;
//import org.springframework.util.ObjectUtils;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.concurrent.ConcurrentLinkedQueue;
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
//    @Autowired
//    IRedisCatchStorageService redisCatchStorageService;
//
//    @Autowired
//    StreamInfoConf InfoConf;
//
//    @Autowired
//    DispatcherSignInConf dispatcherSignInConf;
//    @Async
//    @Override
//    public void run(String... args) throws Exception {
//        //常驻进程检测redis hash中所有的待处理的业务场景消息
//        //获取hashmap中的
//
//        while (true){
//            try{
//
//
//                ConcurrentLinkedQueue<StreamBusinessSceneResp> taskQueue = InfoConf.getTaskQueue();
//                if(!ObjectUtils.isEmpty(taskQueue)){
//
//                    if(ObjectUtils.isEmpty(dispatcherSignInConf.getMqExchange())){
//                        //业务队列暂时未创建成功，无法发送消息 todo 后续做补偿机制，顺序进行消息的推送
//                        log.error(LogTemplate.ERROR_LOG_TEMPLATE, "业务场景处理", "业务队列暂时未初始化", null);
//                        Thread.sleep(10);
//                    }
//
//                    StreamBusinessSceneResp  businessSceneRespEnd = taskQueue.poll();
//                    String businessSceneKey = businessSceneRespEnd.getBusinessSceneKey();
//                    ArrayList<String> keyStrings = new ArrayList<>();
//                    while (!ObjectUtils.isEmpty(RedisCommonUtil.rangListAll(redisTemplate,BusinessSceneConstants.STREAM_BUSINESS_LISTS + businessSceneKey))){
//
//                        StreamBusinessSceneResp oneResp = (StreamBusinessSceneResp)RedisCommonUtil.leftPop(redisTemplate, BusinessSceneConstants.STREAM_BUSINESS_LISTS + businessSceneKey);
//                        //消息汇聚聚合
//                        oneResp.setCode(businessSceneRespEnd.getCode());
//                        oneResp.setMsg(businessSceneRespEnd.getMsg());
//                        oneResp.setData(businessSceneRespEnd.getData());
//                        keyStrings.add(oneResp.getMsgId());
//                        businessAsyncSender.sendforAllScene(oneResp);
//                    };
//                    //消息日志记录 根据消息id进行消息修改
//                    redisCatchStorageService.businessSceneLogDb(businessSceneRespEnd,keyStrings);
//                    if(businessSceneRespEnd.getMsgType().equals(StreamBusinessMsgType.STREAM_LIVE_PLAY_START) || businessSceneRespEnd.getMsgType().equals(StreamBusinessMsgType.STREAM_RECORD_PLAY_START)){
//                        if(businessSceneRespEnd.getCode() != BusinessErrorEnums.SUCCESS.getErrCode()){
//                            //异常点播处理
//                            mediaPlayService.playBusinessErrorScene(businessSceneRespEnd);
//
//                        }
//                    }
//
//                    RLock lock = redissonClient.getLock( BusinessSceneConstants.BUSINESS_LOCK_KEY+businessSceneKey);
//                    if(!ObjectUtils.isEmpty(lock)){
//                        lock.unlockAsync(businessSceneRespEnd.getThreadId());
//                    }
//                }else {
//                    Thread.sleep(10);
//                }
//            }catch (Exception e){
//                log.error(LogTemplate.ERROR_LOG_TEMPLATE, "业务场景常驻线程处理","异常处理失败",e);
//            }
//        }
//
//    }
//
//
//}
