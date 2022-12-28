package com.runjian.common.config.response;

import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.constant.BusinessSceneStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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



    private BusinessSceneStatusEnum status;

    /**
     * 数据
     */
    private T data;


    /**
     * 创建初始数据
     * @param <T>
     * @return
     */
    public static<T> BusinessSceneResp<T> addSceneReady(){
        return create(BusinessErrorEnums.SUCCESS.getErrCode(), BusinessErrorEnums.SUCCESS.toString(), BusinessSceneStatusEnum.ready,null);
    }

    /**
     * 创建初始数据
     * @param <T>
     * @return
     */
    public static<T> BusinessSceneResp<T> addSceneEnd(BusinessErrorEnums businessErrorEnums,T data){
        return create(businessErrorEnums.getErrCode(), businessErrorEnums.toString(), BusinessSceneStatusEnum.end,data);
    }

    /**
     * 自创建异常
     * @param code 消息code
     * @param msg 消息
     * @param data 数据
     * @param <T>
     * @return
     */
    public static<T> BusinessSceneResp<T> create(Integer code, String msg,BusinessSceneStatusEnum status, T data){
        return new BusinessSceneResp<T>(code, msg,status, data);
    }


}
