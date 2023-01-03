package com.runjian.redis;

import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.common.constant.GatewayMsgType;
import com.runjian.common.utils.redis.RedisCommonUtil;
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

@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class RedisTest {

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;
    @Test
    public void testZadd(){
        BusinessSceneResp<Object> objectBusinessSceneResp = BusinessSceneResp.addSceneReady(GatewayMsgType.GATEWAY_SIGN_IN,null);
        Boolean test = RedisCommonUtil.zAdd(redisTemplate, "test", objectBusinessSceneResp, 125);

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

}
