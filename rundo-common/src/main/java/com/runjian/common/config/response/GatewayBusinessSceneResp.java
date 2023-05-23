package com.runjian.common.config.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.constant.BusinessSceneStatusEnum;
import com.runjian.common.constant.GatewayBusinessMsgType;
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
public class GatewayBusinessSceneResp<T> {

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

    private GatewayBusinessMsgType gatewayMsgType;

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
    public static<T> GatewayBusinessSceneResp<T> addSceneReady(GatewayBusinessMsgType gatewayMsgType, String msgId, int timeOut, T data){
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
    public static<T> GatewayBusinessSceneResp<T> addSceneEnd(GatewayBusinessMsgType gatewayMsgType, BusinessErrorEnums businessErrorEnums, String msgId, long threadId, LocalDateTime time, T data){
        return create(businessErrorEnums.getErrCode(), businessErrorEnums.getErrMsg(), BusinessSceneStatusEnum.end,gatewayMsgType,msgId,threadId,time ,data);
    }

    /**
     * 创建初始数据
     * @param <T>
     * @return
     */
    public static<T> GatewayBusinessSceneResp<T> addSceneEnd(GatewayBusinessMsgType gatewayMsgType, BusinessErrorEnums businessErrorEnums, GatewayBusinessSceneResp businessSceneResp, T data){
        return create(businessErrorEnums.getErrCode(), businessErrorEnums.getErrMsg(), BusinessSceneStatusEnum.end,gatewayMsgType,businessSceneResp.getMsgId(),businessSceneResp.getThreadId(),businessSceneResp.getTime() ,data);
    }

    public <T> GatewayBusinessSceneResp<T> addThisSceneEnd(GatewayBusinessMsgType gatewayMsgType, BusinessErrorEnums businessErrorEnums, GatewayBusinessSceneResp businessSceneResp, T data){
        return create(businessErrorEnums.getErrCode(), businessErrorEnums.getErrMsg(), BusinessSceneStatusEnum.end,gatewayMsgType,businessSceneResp.getMsgId(),businessSceneResp.getThreadId(),businessSceneResp.getTime() ,data);
    }

    /**
     * 业务运行状态修改
     * @param gatewayMsgType
     * @param businessErrorEnums
     * @param businessSceneResp
     * @param data
     * @param <T>
     * @return
     */
    public static<T> GatewayBusinessSceneResp<T> addSceneRunning(GatewayBusinessMsgType gatewayMsgType, BusinessErrorEnums businessErrorEnums, GatewayBusinessSceneResp businessSceneResp, T data){
        return create(businessErrorEnums.getErrCode(), businessErrorEnums.getErrMsg(), BusinessSceneStatusEnum.running,gatewayMsgType,businessSceneResp.getMsgId(),businessSceneResp.getThreadId(),businessSceneResp.getTime() ,data);
    }
    /**
     * 自创建异常
     * @param code 消息code
     * @param msg 消息s
     * @param data 数据
     * @param <T>
     * @return
     */
    public static<T> GatewayBusinessSceneResp<T> create(Integer code, String msg, BusinessSceneStatusEnum status, GatewayBusinessMsgType gatewayMsgType, String msgId, long threadId, LocalDateTime time, T data){
        return new GatewayBusinessSceneResp<T>(code, msg,msgId,status,gatewayMsgType,threadId,time ,data);
    }


}
