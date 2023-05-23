package com.runjian.common.config.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.constant.BusinessSceneStatusEnum;
import com.runjian.common.constant.StreamBusinessMsgType;
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

    private StreamBusinessMsgType msgType;

    private String businessSceneKey;
    /**
     * 数据
     */
    private T data;



    /**
     * 创建初始数据
     * @param msgType
     * @param msgId
     * @param <T>
     * @return
     */
    public static<T> StreamBusinessSceneResp<T> addSceneReady(StreamBusinessMsgType msgType, String msgId,String businessSceneKey,T data){
        return create(BusinessErrorEnums.BUSINESS_SCENE_EXCEPTION.getErrCode(), BusinessErrorEnums.BUSINESS_SCENE_EXCEPTION.getErrMsg(), BusinessSceneStatusEnum.ready, msgType,msgId,businessSceneKey,data);
    }

    /**
     * 创建初始数据
     * @param <T>
     * @return
     */
    public static<T> StreamBusinessSceneResp<T> addSceneEnd(StreamBusinessMsgType msgType, BusinessErrorEnums businessErrorEnums, String businessSceneKey, T data){
        return create(businessErrorEnums.getErrCode(), businessErrorEnums.getErrMsg(), BusinessSceneStatusEnum.end,msgType,null,businessSceneKey,data);
    }

    /**
     * 创建初始数据
     * @param <T>
     * @return
     */
    public static<T> StreamBusinessSceneResp<T> addSceneTimeout(StreamBusinessMsgType msgType, BusinessErrorEnums businessErrorEnums, String businessSceneKey, T data){
        return create(businessErrorEnums.getErrCode(), businessErrorEnums.getErrMsg(), BusinessSceneStatusEnum.TimeOut,msgType,null,businessSceneKey,data);
    }




    /**
     * 自创建异常
     * @param code 消息code
     * @param msg 消息s
     * @param data 数据
     * @param <T>
     * @return
     */
    public static<T> StreamBusinessSceneResp<T> create(Integer code, String msg, BusinessSceneStatusEnum status, StreamBusinessMsgType msgType, String msgId, String businessSceneKey,T data){
        return new StreamBusinessSceneResp<T>(code, msg,msgId,status,msgType,businessSceneKey,data);
    }


}
