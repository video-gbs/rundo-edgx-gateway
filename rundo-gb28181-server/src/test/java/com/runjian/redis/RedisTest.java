package com.runjian.redis;

import com.alibaba.fastjson.JSONObject;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.common.config.response.GatewayBusinessSceneResp;
import com.runjian.common.constant.BusinessSceneConstants;
import com.runjian.common.constant.GatewayBusinessMsgType;
import com.runjian.common.constant.GatewayMsgType;
import com.runjian.common.constant.LogTemplate;
import com.runjian.common.utils.redis.RedisCommonUtil;
import com.runjian.gb28181.bean.Device;
import com.runjian.service.IDeviceService;
import com.runjian.service.IRedisCatchStorageService;
import com.runjian.utils.redis.RedisDelayQueuesUtil;
import com.runjian.utils.redis.RedissonLockUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class RedisTest {

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    IDeviceService deviceService;
    @Test
    public void testZadd(){
        BusinessSceneResp<Object> objectBusinessSceneResp = BusinessSceneResp.addSceneReady(GatewayMsgType.GATEWAY_SIGN_IN,null,5,null);

    }

    @Test
    public void testRedisson(){
        log.info("测试");
        for (int i=0;i<=5;i++){
            int finalI = i;
            new Thread(()->{
                long name = Thread.currentThread().getId();
                log.info("当前线程,={}",name);
                RLock lock = redissonClient.getLock("test---:0");
                lock.lock();
                
            }).start();

        }

        while (true){

            RLock lock = redissonClient.getLock("test---:0");
            boolean locked = lock.isLocked();
            if(locked){
                lock.unlockAsync();
                lock.lock();
                break;
            }



        }

    }

    @Test
    public void testDeviceSender(){
        Device device = new Device();
        device.setDeviceId("123123123");
        device.setName("test");
        device.setStreamMode("UDP");
        device.setStreamMode("UDP");
        device.setCharset("GB2312");
        device.setOnline(0);
        device.setTransport("UDP");
        device.setIp("127.0.0.1");
        device.setPort(5060);
        device.setHostAddress("127.0.0.1:5060");
        deviceService.deviceInfoQuery(device,null);
    }

    @Test
    public void testRedisHash(){
//        String businessSceneKey = "test";
//        ArrayList<BusinessSceneResp> businessSceneRespArrayList = new ArrayList<>();
//
//        for (int i = 0; i < 5; i++) {
//            BusinessSceneResp<Object> objectBusinessSceneResp = BusinessSceneResp.addSceneReady(GatewayMsgType.DEVICE_TOTAL_SYNC,String.valueOf(i),5,null);
//            businessSceneRespArrayList.add(objectBusinessSceneResp);
//
//        }
//        RedisCommonUtil.hset(redisTemplate, "test_all", businessSceneKey, businessSceneRespArrayList);
//
//
//        String businessSceneString = (String) RedisCommonUtil.hget(redisTemplate, "test_all", businessSceneKey);
//        log.info(LogTemplate.PROCESS_LOG_TEMPLATE,"业务消息修改修改",businessSceneKey);
//        if(ObjectUtils.isEmpty(businessSceneString)){
//            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"处理网关业务状态","处理失败,对应的业务缓存不存在",businessSceneKey);
//            return;
//        }
//        //其中data的数据格式为arraylist
//        List<BusinessSceneResp> businessSceneResps = JSONObject.parseArray(businessSceneString, BusinessSceneResp.class);
//
//        ArrayList<BusinessSceneResp> businessSceneRespArrayListNew = new ArrayList<>();
//        for (BusinessSceneResp businessSceneResp : businessSceneResps) {
//            BusinessSceneResp<Object> objectBusinessSceneResp = businessSceneResp.addThisSceneEnd(GatewayMsgType.DEVICE_TOTAL_SYNC,BusinessErrorEnums.SIP_SEND_EXCEPTION, businessSceneResp,"成功啊");
//            businessSceneRespArrayListNew.add(objectBusinessSceneResp);
//        }

        //把其中全部的请求状态修改成一致

//        RedisCommonUtil.hset(redisTemplate,BusinessSceneConstants.ALL_SCENE_HASH_KEY,businessSceneKey,businessSceneRespArrayListNew);

    }

    @Autowired
    IRedisCatchStorageService redisCatchStorageService;

    @Autowired
    RedisDelayQueuesUtil redisDelayQueuesUtil;


    @Test
    public void testGetRedisHash() throws InterruptedException {
        String key = GatewayBusinessMsgType.CHANNEL_DELETE_HARD.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+"test";
        redisCatchStorageService.addBusinessSceneKey(key, GatewayBusinessMsgType.CHANNEL_DELETE_HARD,null,0);
        new Thread(()->{
            while (true) {
                try {
                    Set<String> keys = RedisCommonUtil.keys(redisTemplate, BusinessSceneConstants.GATEWAY_BUSINESS_LISTS + "*");
                    if (!ObjectUtils.isEmpty(keys)) {
                        for (String bKey : keys) {
                            String businessKey = bKey.substring(bKey.indexOf(BusinessSceneConstants.SCENE_SEM_KEY) + 1);
                            if(businessKey.equals("test")){
                                Object delayQueue = redisDelayQueuesUtil.getDelayQueue(businessKey);

                            }

                        }

                    }
                } catch (Exception e) {
                    log.error("(Redis延迟队列异常中断) {}", e.getMessage());
                }
            }
        }).start();
        Thread.sleep(8000);
        redisCatchStorageService.editBusinessSceneKey(key,BusinessErrorEnums.SUCCESS,"test");



        //把其中全部的请求状态修改成一致


    }
}
