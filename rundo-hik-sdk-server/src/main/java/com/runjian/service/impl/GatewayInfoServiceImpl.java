package com.runjian.service.impl;

import com.runjian.common.commonDto.Gateway.dto.EdgeGatewayInfoDto;
import com.runjian.common.constant.GatewayCacheConstants;
import com.runjian.common.constant.GatewayMsgType;
import com.runjian.common.constant.GatewayProtocalEnum;
import com.runjian.common.constant.GatewayTypeEnum;
import com.runjian.common.mq.RabbitMqSender;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.common.utils.DateUtils;
import com.runjian.common.utils.UuidUtil;
import com.runjian.conf.GatewayInfoConf;
import com.runjian.conf.UserSetting;
import com.runjian.conf.mq.GatewaySignInConf;
import com.runjian.mq.gatewayBusiness.GatewayBusinessMqListener;
import com.runjian.service.IGatewayInfoService;
import com.runjian.service.IRedisCatchStorageService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class GatewayInfoServiceImpl implements IGatewayInfoService {
    @Autowired
    @Qualifier("gatewayBusinessMqListenerContainer")
    SimpleMessageListenerContainer container;

    @Autowired
    GatewayBusinessMqListener gatewayBusinessMqListener;


    @Value("${server.port}")
    private String serverPort;
    @Autowired
    private GatewayInfoConf gatewayInfoConf;
    @Autowired
    private RabbitMqSender rabbitMqSender;

    @Autowired
    private IRedisCatchStorageService iRedisCatchStorageService;
    @Value("${gateway-info.serialNum}")
    private String serialNum;

    @Value("${gateway-info.expire}")
    private int expire;

    @Autowired
    RedissonClient redissonClient;
    @Autowired
    UserSetting userSetting;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    IRedisCatchStorageService redisCatchStorageService;


    @Value("${mdeia-api-uri-list.gateway-bind}")
    private String gatewayBind;

    @Autowired
    GatewaySignInConf gatewaySignInConf;



    @Autowired
    private RestTemplate restTemplate;

    @Value("${mq-defualt.public.queue-id-set:PUBLIC-SG}")
    private String queueId;
    /**
     * 动态监听mq的队列
     * @param queueName
     */
    @Override
    public void addMqListener(String queueName) {
        String[] strings = container.getQueueNames();
        List<String> list= Arrays.asList(strings);

        if (!list.contains(queueName)) {
            container.addQueueNames(queueName);
            container.setMessageListener(gatewayBusinessMqListener);
            container.setConcurrentConsumers(1);
            container.setMaxConcurrentConsumers(1);
            container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        }else {
            for (String queueString : list) {
                if(!queueString.equals(queueName)){
                    container.removeQueueNames(queueString);
                }
            }

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

        EdgeGatewayInfoDto config = new EdgeGatewayInfoDto();
        config.setPort(port);
        config.setIp(ip);
        config.setGatewayType(GatewayTypeEnum.OTHER.getTypeName());
        config.setProtocol(GatewayProtocalEnum.GB28181.getTypeName());
        config.setOutTime(DateUtils.getExpireTimestamp(expire));
        gatewayInfoConf.setEdgeGatewayInfoDto(config);

        //进行mq消息发送
        String sn = iRedisCatchStorageService.getSn(GatewayCacheConstants.GATEWAY_INFO_SN_INCR);
        CommonMqDto dataRes = new CommonMqDto();

        dataRes.setMsgId(GatewayCacheConstants.GATEWAY_INFO_SN_prefix+sn);
        dataRes.setSerialNum(serialNum);
        dataRes.setMsgType(GatewayMsgType.GATEWAY_SIGN_IN.getTypeName());
        dataRes.setData(config);
        dataRes.setTime(LocalDateTime.now());
        //消息组装
        log.info("注册信息发送:={}",dataRes);
        rabbitMqSender.sendMsg(queueId, UuidUtil.toUuid(), dataRes, true);
    }

}
