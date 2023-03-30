package com.runjian.common.config.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
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
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime time;
    /**
     * 数据
     */
    private T data;


    /**
     * 创建初始数据
     * @param gatewayMsgType
     * @param msgId
     * @param timeOut 过期时间
     * @param <T>
     * @return
     */
    public static<T> BusinessSceneResp<T> addSceneReady(GatewayMsgType gatewayMsgType,String msgId,int timeOut,T data){
        long id = Thread.currentThread().getId();
        //timeOut毫秒转化为秒
        LocalDateTime now = LocalDateTime.now().plusSeconds(timeOut/1000);
        return create(BusinessErrorEnums.BUSINESS_SCENE_EXCEPTION.getErrCode(), BusinessErrorEnums.BUSINESS_SCENE_EXCEPTION.getErrMsg(), BusinessSceneStatusEnum.ready, gatewayMsgType,msgId,id,now,data);
    }

    /**
     * 创建初始数据
     * @param <T>
     * @return
     */
    public static<T> BusinessSceneResp<T> addSceneEnd(GatewayMsgType gatewayMsgType,BusinessErrorEnums businessErrorEnums,String msgId,long threadId, LocalDateTime time,T data){
        return create(businessErrorEnums.getErrCode(), businessErrorEnums.getErrMsg(), BusinessSceneStatusEnum.end,gatewayMsgType,msgId,threadId,time ,data);
    }


    public <T> BusinessSceneResp<T> addThisSceneEnd(GatewayMsgType gatewayMsgType,BusinessErrorEnums businessErrorEnums,BusinessSceneResp businessSceneResp,T data){
        return create(businessErrorEnums.getErrCode(), businessErrorEnums.getErrMsg(), BusinessSceneStatusEnum.end,gatewayMsgType,businessSceneResp.getMsgId(),businessSceneResp.getThreadId(),businessSceneResp.getTime() ,data);
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
