package com.runjian.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @author chenjialing
 */
@Data
@TableName("rundo_play_list_log")
public class PlayListLogEntity {


    /**
     * 数据库自增ID
     */
    @TableId(value = "id",type = IdType.AUTO)
    private long id;
    /**
     * 流id
     */

    private String streamId;


    /**
     * 点播句柄
     */
    private Integer playHandle;

    /**
     * 点播状态：-1点播失败，0点播成功，1停止失败,2停止成功
     */
    private int playStatus;

    /**
     * sdk错误状态码
     */
    private Integer playErrorCode;

    private Integer deleted;



    private Date createdAt;

    private Date updatedAt;

}
