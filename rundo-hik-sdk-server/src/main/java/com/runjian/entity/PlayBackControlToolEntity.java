package com.runjian.entity;

import lombok.Data;

/**
 * @author chenjialing
 */
@Data
public class PlayBackControlToolEntity {

    /**
     * 通道号
     */
    private long channel;
    /**
     * 1：开始播放 2：停止播放 3：暂停播放 4：恢复播放
     */
    private long cmd;
    /**
     * 回放句柄
     */
    private long replayHandle;
    /**
     * 选填，拖放操作为必选，传入的是拖放时间：20230811011000
     */
    private String val1;
    /**
     * 选填，预留字段
     */
    private long val2;

}
