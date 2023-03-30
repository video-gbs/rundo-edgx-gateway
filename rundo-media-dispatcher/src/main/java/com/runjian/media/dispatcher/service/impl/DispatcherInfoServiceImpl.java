package com.runjian.media.dispatcher.service.impl;

import com.runjian.common.commonDto.Gateway.dto.EdgeGatewayInfoDto;
import com.runjian.common.constant.*;
import com.runjian.common.mq.RabbitMqSender;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.common.utils.DateUtils;
import com.runjian.common.utils.UuidUtil;
import com.runjian.media.dispatcher.mq.dispatcherBusiness.DispatcherBusinessMqListener;
import com.runjian.media.dispatcher.service.DispatcherInfoService;
import com.runjian.media.dispatcher.service.IRedisCatchStorageService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * @author chenjialing
 */
@Service
@Slf4j
public class DispatcherInfoServiceImpl implements DispatcherInfoService {



    @Value("${server.port}")
    private String serverPort;

    @Autowired
    private RabbitMqSender rabbitMqSender;

    @Autowired
    private IRedisCatchStorageService iRedisCatchStorageService;
    @Value("${dispatcher-info.serialNum}")
    private String serialNum;

    @Value("${dispatcher-info.expire}")
    private int expire;

    @Autowired
    RedissonClient redissonClient;


    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    IRedisCatchStorageService redisCatchStorageService;

    @Autowired
    @Qualifier("dispatcherBusinessMqListenerContainer")
    SimpleMessageListenerContainer container;

    @Autowired
    DispatcherBusinessMqListener dispatcherBusinessMqListener;

    //发送队列
    @Value("${mq-defualt.public.queue-id-set:PUBLIC-SG}")
    private String queueId;
    @Override
    public void addMqListener(String queueName) {
        String[] strings = container.getQueueNames();
        List<String> list= Arrays.asList(strings);
        try{
            if (!list.contains(queueName)) {
                container.addQueueNames(queueName);
                container.setMessageListener(dispatcherBusinessMqListener);
                container.setConcurrentConsumers(8);
                container.setMaxConcurrentConsumers(32);
                container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
            }else {
                for (String queueString : list) {
                    if(!queueString.equals(queueName)){
                        container.removeQueueNames(queueString);
                    }
                }

            }
        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"调度服务动态业务队列创建","创建失败",e);
        }

    }

    @Override
    public void sendRegisterInfo() {
        String ip = "127.0.0.1";
        try{
            ip = InetAddress.getLocalHost().getHostAddress();
        }catch (UnknownHostException e){
            log.info("获取ip失败,={}",e);
        }

        int port = Integer.parseInt(serverPort);

        //进行mq消息发送
        String sn = iRedisCatchStorageService.getSn(GatewayCacheConstants.DISPATCHER_INFO_SN_INCR);
        EdgeGatewayInfoDto config = new EdgeGatewayInfoDto();
        config.setPort(port);
        config.setIp(ip);
        config.setOutTime(DateUtils.getExpireTimestamp(expire));
        CommonMqDto dataRes = new CommonMqDto();

        dataRes.setMsgId(GatewayCacheConstants.GATEWAY_INFO_SN_prefix+sn);
        dataRes.setSerialNum(serialNum);
        dataRes.setMsgType(GatewayMsgType.DISPATCH_SIGN_IN.getTypeName());
        dataRes.setData(config);
        dataRes.setTime(LocalDateTime.now());
        //消息组装
        log.info("注册信息发送:={}",dataRes);
        rabbitMqSender.sendMsg(queueId, UuidUtil.toUuid(), dataRes, true);
    }


}
