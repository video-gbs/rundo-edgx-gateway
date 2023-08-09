package com.runjian.common.constant;

/**
 * @author Miracle
 * @date 2022/4/7 16:53
 */
public class MarkConstant {

    /*********************************助手服务相关*******************************************/

    /**
     * 助手服务Redis在线状态key
     */
    public static final String REDIS_ASSIST_STATE = "ASSIST_STATE";

    /*********************************标志位分割符号*******************************************/

    public static final String MARK_SPLIT_SYMBOL = "_";

    public static final String MARK_SPLIT_SEMICOLON = ":";

    public static final String MARK_SPLIT_RAIL = "-";

    /*********************************未完成任务定时重试标志*******************************************/

    /**
     * 自管理无人观看流
     */
    public static final String REDIS_NO_READER_STREAM = "NO_READER_STREAM";


    /*********************************视频类型标志*******************************************/

    /**
     * 正在录制的视频流 key-ZLM的流 value-自定义标识流
     */
    public static final String REDIS_VIDEO_PLAY_MAP = "VIDEO_PLAY_MAP";

    /**
     * 录像标志位
     */
    public static final String REDIS_RECORD_VIDEO_MARK = "RECORD_TYPE_";

    /**
     * 录像计划批量播放锁
     */
    public static final String REDIS_RECORD_PLAN_LOCK_MARK = "RECORD_PLAN_PLAY_LOCK:";

    /**
     * 设备云端录像操作锁
     */
    public static final String REDIS_RECORD_PLAN_DEVICE_LOCK_MARK = "RECORD_PLAN_DEVICE_OPERATION_LOCK:";

    /**
     * 告警标志位
     */
    public static final String REDIS_ALARM_VIDEO_MARK = "ALARM_TYPE_";

    /**
     * 设备录像下载标志
     */
    public static final String REDIS_DOWNLOAD_VIDEO_MARK = "DOWNLOAD_TYPE_";




    /*******************************录像计划定时器标志****************************************/

    /**
     * 录像计划定时器分组名前缀
     */
    public static final  String TIMER_RECORD_GROUP_PREFIX = "RECORD-GROUP-";

    /**
     * 录像计划定时器名开始前缀
     */
    public static final String TIMER_RECORD_NAME_START_PREFIX = "START-";

    /**
     * 录像计划定时器名结束前缀
     */
    public static final String TIMER_RECORD_NAME_CLOSE_PREFIX = "CLOSE-";

    /*******************************用户相关****************************************/

    public static final String REDIS_MAP_CONFIG = "MAP_CONFIG";
    public static final String REDIS_USER_DATA = "USER_DATA:";

    public static final String REDIS_USER_DEVICE_CHANNEL = "DEVICE_CHANNEL";

    public static final String REDIS_USER_DEVICE_CHANNEL_ID = "DEVICE_CHANNEL_ID";

    public static final String MAX_AUTH_USERNAME = "admin";
    public static final String REDIS_USER_ROLE_IDS = "ROLE_IDS";

    /******************************mq队列相关***********************************************/


    /******************************对外api接口参数相关***********************************************/
    public static final String FOREIGN_AUTHORIZATION = "authorization";

    public static final String FOREIGN_PLATFORM_ID = "platformid";

    /******************************国标ptz-- 10进制指令大全 **********************************************/
    /**预置位
     * start
     */
    public static final int PRESET_SET = 129;
    public static final int PRESET_INVOKE = 130;
    public static final int PRESET_DEL = 131;
    /**预置位
     * end
     */
    public static final int PTZ_LEFT = 2;
    public static final int PTZ_RIGHT = 1;
    public static final int PTZ_UP = 8;
    public static final int PTZ_DOWN = 4;
    public static final int PTZ_UPLEFT = 10;
    public static final int PTZ_UPRIGHT = 9;
    public static final int PTZ_DOWNLEFT = 6;
    public static final int PTZ_DOWNRIGHT = 5;
    public static final int PTZ_STOP = 0;


    //倍率放大缩小
    public static final int ZOOM_IN = 20;
    public static final int ZOOM_OUT = 10;

    //F1指令----start
    public static final int IRIS_REDUCE = 48;
    public static final int IRIS_GROW = 44;
    public static final int FOCUS_NEAR = 42;
    public static final int FOCUS_FAR = 41;
    public static final int IRISE_AND_FOCUS_STOP = 40;
    //F1指令----stop


    /*********************manufacturer厂商****************************/
    public static final String HIK_MANUFACTURER = "hikvision";

}
