package com.runjian.common.config.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @author Miracle
 * @date 2020/3/3 22:49
 */
@Getter
@AllArgsConstructor
@ToString
public enum BusinessErrorEnums {

    /**************************** 通用消息0与-1 ****************************/
    SUCCESS(200,0,"SUCCESS"),

    UNKNOWN_ERROR(500,-1,"SYSTEM_ERROR"),

    /**************************** 数据校验类异常,PREFIX:VALID CODE:10xxx ****************************/

    VALID_NO_OBJECT_FOUND(400,10001, "请求对象不存在"),

    VALID_NO_HANDLER_FOUND(400,10002,"找不到执行的路径操作"),

    VALID_BIND_EXCEPTION_ERROR(400,10003, "请求参数错误"),

    VALID_METHOD_NOT_SUPPORTED(400, 10004, "不支持的请求方式"),

    VALID_PARAMETER_ERROR(400,10005,"请求参数校验失败"),

    VALID_OBJECT_IS_EXIST(400, 10006, "对象已存在"),

    VALID_JSON_PROCESSING_ERROR(500, 10007, "JSON转化异常"),

    VALID_NOT_FOUNT_FIELD(500, 10008, "找不到对应的字段"),

    VALID_ANNOTATION_PARAMETER_ERROR(500, 10009, "注解参数错误"),

    VALID_ILLEGAL_OPERATION(400, 10010, "非法的操作"),

    VALID_REPETITIVE_OPERATION_ERROR(400, 10011, "短时间重复的操作"),

    /**************************** WVP源码相关,PREFIX:VALID CODE:15xxx ****************************/

    WVP_DEVICE_PREVIEW_ERROR(500, 15001, "设备预览API调用失败！"),

    WVP_PLAYBACK_TIMEOUT(500, 15002, "回放超时"),

    WVP_PLAYBACK_ERROR(500, 15002, "回放异常"),




    /**************************** 用户模块相关异常,PREFIX:USER CODE:20xxx ****************************/

    USER_LOGIN_ERROR(401, 20001, "用户登录失败"),

    USER_NO_AUTH(401, 20002, "用户无权限"),

    /**************************** 定时器模块相关错误,PREFIX:TIMER CODE:21xxx ****************************/

    TIMER_START_ERROR(500, 21001, "定时任务启动异常"),

    TIMER_STOP_ERROR(500, 21002, "定时任务关闭异常"),

    TIMER_RESUME_ERROR(500, 21003, "定时任务恢复异常"),

    TIMER_RUN_ERROR(500,21004, "定时任务执行失败"),

    TIMER_ADD_ERROR(400, 21005, "添加定时任务失败"),

    TIMER_SAVE_TO_JSON_ERROR(500, 21006,  "定时器任务持久化失败"),

    TIMER_SELECT_ERROR(500, 21007, "定时器任务查询失败"),

    TIMER_DELETE_ERROR(500, 21008, "定时器删除异常"),

    /**************************** MQ模块相关异常,PREFIX:MQ CODE:22xxx ****************************/

    MQ_UNKNOWN_EXCHANGE_TYPE(500, 22001, "未知的消息队列类型"),

    MQ_QUEUE_IS_NOT_FOUND(500, 22002, "找不到该queue"),

    MQ_EXCHANGE_IS_NOT_FOUND(500, 22003, "找不到该exchange"),

    /**************************** 网关业务相关 CODE:231xx ****************************/
    REDIS_EXCEPTION(500,23100,"redis缓存异常"),
    BUSINESS_SCENE_EXCEPTION(500,23101,"业务场景处理异常"),

    /************************** 网关自定义s事件异常 CODE:232xx ****************************/
    SIP_TIMEOUT_EVENT(500,232001,"消息超时未回复"),
    SIP_TRASACTION_TERMINATED_EVENT(500,232002,"事务已结束"),
    SIP_DIALOG_TERMINATED_EVENT(500,232003,"会话已结束"),
    SIP_DEVICE_NOTFOUND_EVENT(500,232004,"设备未找到"),

    SIP_SEND_EXCEPTION(500,232005,"sip信令发送失败"),

    //通道同步失败
    SIP_CATALOG_EXCEPTION(500,232006,"通道同步失败异常"),

    DB_NOT_FOUND(500,232007,"数据查找失败"),

    CHANNEL_OFFLINE(500,232008,"通道处于离线状态"),

    DB_DEVICE_NOT_FOUND(500,232009,"设备数据查找失败"),

    SIP_SEND_SUCESS(500,232010,"信令交互成功，推流失败"),

    SIP_DELETE_SUCESS(500,232011,"设备删除失败"),

    BIND_GATEWAY_ERROR(500,232012,"网关绑定流媒体服务失败"),

    /************************** media异常 CODE:233xx ****************************/
    MEDIA_ZLM_EXSITED_ERROR(500,23300,"流媒体服务已存在"),
    MEDIA_ZLM_COLLECT_ERROR(500,23301,"流媒体服务连接失败"),
    MEDIA_SERVER_COLLECT_ERROR(500,23302,"流媒体调用服务连接失败"),
    MEDIA_ZLM_RTPSERVER_CREATE_ERROR(500,23303,"流媒体ZLM推流端口创建失败"),
    MEDIA_SERVER_BIND_ERROR(500,23304,"流媒体服务暂未绑定网关"),


    /**************************** 对外api类型异常,PREFIX:模块名 CODE:3xxxx ****************************/

    ACCOUNT_NOT_ENABLED(400,30001, "该用户已被禁用"),

    REQUEST_EXPIRED(400,30002, "该请求鉴权超时，请重新生成鉴权参数"),

    AUTHORIZED_FAILED(400,30003, "鉴权失败"),








    ;


    /**
     * 状态
     */
    private final Integer state;

    /**
     * 错误码
     */
    private final Integer errCode;

    /**
     * 错误信息
     */
    private final String errMsg;
}
