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

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 业务场景返回值
 * @author chenjialing
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GatewayBusinessSceneResp<T> implements Serializable {

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

    private String businessSceneKey;
    /**
     * 数据
     */
    private T data;

    /**
     * 当前线程的id,第一次加锁成功记录，后续释放锁的关键依据
     */
    private long threadId;



    /**
     * 创建初始数据
     * @param gatewayMsgType
     * @param msgId
     * @param <T>
     * @return
     */
    public static<T> GatewayBusinessSceneResp<T> addSceneReady(GatewayBusinessMsgType gatewayMsgType, String msgId,String businessSceneKey,T data){
        long id = Thread.currentThread().getId();
        return create(BusinessErrorEnums.BUSINESS_SCENE_EXCEPTION.getErrCode(), BusinessErrorEnums.BUSINESS_SCENE_EXCEPTION.getErrMsg(), BusinessSceneStatusEnum.ready, gatewayMsgType,msgId,businessSceneKey,data,id);
    }

    /**
     * 创建初始数据
     * @param <T>
     * @return
     */
    public static<T> GatewayBusinessSceneResp<T> addSceneEnd(GatewayBusinessMsgType gatewayMsgType, BusinessErrorEnums businessErrorEnums, String businessSceneKey, T data,String msgId,long id){
        return create(businessErrorEnums.getErrCode(), businessErrorEnums.getErrMsg(), BusinessSceneStatusEnum.end,gatewayMsgType,msgId,businessSceneKey,data,id);
    }

    /**
     * 创建初始数据
     * @param <T>
     * @return
     */
    public static<T> GatewayBusinessSceneResp<T> addSceneTimeout(GatewayBusinessMsgType gatewayMsgType, BusinessErrorEnums businessErrorEnums, String businessSceneKey, T data,String msgId,long id){
        return create(businessErrorEnums.getErrCode(), businessErrorEnums.getErrMsg(), BusinessSceneStatusEnum.TimeOut,gatewayMsgType,msgId,businessSceneKey,data,id);
    }




    /**
     * 自创建异常
     * @param code 消息code
     * @param msg 消息s
     * @param data 数据
     * @param <T>
     * @return
     */
    public static<T> GatewayBusinessSceneResp<T> create(Integer code, String msg, BusinessSceneStatusEnum status, GatewayBusinessMsgType gatewayMsgType, String msgId, String businessSceneKey,T data,long id){
        return new GatewayBusinessSceneResp<T>(code, msg,msgId,status,gatewayMsgType,businessSceneKey,data,id);
    }


}
