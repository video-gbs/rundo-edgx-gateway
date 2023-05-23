package com.runjian.common.config.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.constant.BusinessSceneStatusEnum;
import com.runjian.common.constant.StreamBusinessMsgType;
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
public class StreamBusinessSceneResp<T> {

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

    private StreamBusinessMsgType streamGatewayMsgType;

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
     * @param streamGatewayMsgType
     * @param msgId
     * @param timeOut 过期时间
     * @param <T>
     * @return
     */
    public static<T> StreamBusinessSceneResp<T> addSceneReady(StreamBusinessMsgType streamGatewayMsgType, String msgId, int timeOut, T data){
        long id = Thread.currentThread().getId();
        //timeOut毫秒转化为秒
        LocalDateTime now = LocalDateTime.now().plusSeconds(timeOut/1000);
        return create(BusinessErrorEnums.BUSINESS_SCENE_EXCEPTION.getErrCode(), BusinessErrorEnums.BUSINESS_SCENE_EXCEPTION.getErrMsg(), BusinessSceneStatusEnum.ready, streamGatewayMsgType,msgId,id,now,data);
    }

    /**
     * 创建初始数据
     * @param <T>
     * @return
     */
    public static<T> StreamBusinessSceneResp<T> addSceneEnd(StreamBusinessMsgType streamGatewayMsgType, BusinessErrorEnums businessErrorEnums, String msgId, long threadId, LocalDateTime time, T data){
        return create(businessErrorEnums.getErrCode(), businessErrorEnums.getErrMsg(), BusinessSceneStatusEnum.end,streamGatewayMsgType,msgId,threadId,time ,data);
    }

    /**
     * 创建初始数据
     * @param <T>
     * @return
     */
    public static<T> StreamBusinessSceneResp<T> addSceneEnd(StreamBusinessMsgType streamGatewayMsgType, BusinessErrorEnums businessErrorEnums, StreamBusinessSceneResp businessSceneResp, T data){
        return create(businessErrorEnums.getErrCode(), businessErrorEnums.getErrMsg(), BusinessSceneStatusEnum.end,streamGatewayMsgType,businessSceneResp.getMsgId(),businessSceneResp.getThreadId(),businessSceneResp.getTime() ,data);
    }

    public <T> StreamBusinessSceneResp<T> addThisSceneEnd(StreamBusinessMsgType streamGatewayMsgType, BusinessErrorEnums businessErrorEnums, StreamBusinessSceneResp businessSceneResp, T data){
        return create(businessErrorEnums.getErrCode(), businessErrorEnums.getErrMsg(), BusinessSceneStatusEnum.end,streamGatewayMsgType,businessSceneResp.getMsgId(),businessSceneResp.getThreadId(),businessSceneResp.getTime() ,data);
    }

    /**
     * 业务运行状态修改
     * @param streamGatewayMsgType
     * @param businessErrorEnums
     * @param businessSceneResp
     * @param data
     * @param <T>
     * @return
     */
    public static<T> StreamBusinessSceneResp<T> addSceneRunning(StreamBusinessMsgType streamGatewayMsgType, BusinessErrorEnums businessErrorEnums, StreamBusinessSceneResp businessSceneResp, T data){
        return create(businessErrorEnums.getErrCode(), businessErrorEnums.getErrMsg(), BusinessSceneStatusEnum.running,streamGatewayMsgType,businessSceneResp.getMsgId(),businessSceneResp.getThreadId(),businessSceneResp.getTime() ,data);
    }
    /**
     * 自创建异常
     * @param code 消息code
     * @param msg 消息s
     * @param data 数据
     * @param <T>
     * @return
     */
    public static<T> StreamBusinessSceneResp<T> create(Integer code, String msg, BusinessSceneStatusEnum status, StreamBusinessMsgType streamGatewayMsgType, String msgId, long threadId, LocalDateTime time, T data){
        return new StreamBusinessSceneResp<T>(code, msg,msgId,status,streamGatewayMsgType,threadId,time ,data);
    }


}
