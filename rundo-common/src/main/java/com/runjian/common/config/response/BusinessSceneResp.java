package com.runjian.common.config.response;

import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.constant.BusinessSceneStatusEnum;
import com.runjian.common.constant.GatewayMsgType;
import com.runjian.common.utils.redis.RedisCommonUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 业务场景返回值
 * @author chenjialing
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BusinessSceneResp<T> {

    /**
     * 消息码
     */
    private int code;

    /**
     * 消息
     */
    private String msg;

    /**
     * 消息ID
     */
    private String msgId;


    private BusinessSceneStatusEnum status;

    private GatewayMsgType gatewayMsgType;

    /**
     * 当前线程的id,第一次加锁成功记录，后续释放锁的关键依据
     */
    private long threadId;

    /**
     * 预计过期时间
     */
    private LocalDateTime time;
    /**
     * 数据
     */
    private T data;


    /**
     * 创建初始数据
     * @param <T>
     * @return
     */
    public static<T> BusinessSceneResp<T> addSceneReady(GatewayMsgType gatewayMsgType,String msgId){
        long id = Thread.currentThread().getId();
        LocalDateTime now = LocalDateTime.now();
        return create(BusinessErrorEnums.SUCCESS.getErrCode(), BusinessErrorEnums.SUCCESS.toString(), BusinessSceneStatusEnum.ready, gatewayMsgType,msgId,id,now,null);
    }

    /**
     * 创建初始数据
     * @param <T>
     * @return
     */
    public static<T> BusinessSceneResp<T> addSceneEnd(GatewayMsgType gatewayMsgType,BusinessErrorEnums businessErrorEnums,String msgId,long threadId, LocalDateTime time,T data){
        return create(businessErrorEnums.getErrCode(), businessErrorEnums.toString(), BusinessSceneStatusEnum.end,gatewayMsgType,msgId,threadId,time ,data);
    }

    /**
     * 自创建异常
     * @param code 消息code
     * @param msg 消息s
     * @param data 数据
     * @param <T>
     * @return
     */
    public static<T> BusinessSceneResp<T> create(Integer code, String msg, BusinessSceneStatusEnum status, GatewayMsgType gatewayMsgType, String msgId, long threadId, LocalDateTime time,T data){
        return new BusinessSceneResp<T>(code, msg,msgId,status,gatewayMsgType,threadId,time ,data);
    }


}
