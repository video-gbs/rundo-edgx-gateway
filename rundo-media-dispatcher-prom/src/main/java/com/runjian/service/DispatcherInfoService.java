package com.runjian.service;

/**
 * 调度服务的业务处理
 * @author chenjialing
 */
public interface DispatcherInfoService {
     /**
      * 动态创建业务队列
      * @param queueName
      */
     void addMqListener(String queueName);
     /**
      * 发送注册请求
      */
     void sendRegisterInfo();
}
