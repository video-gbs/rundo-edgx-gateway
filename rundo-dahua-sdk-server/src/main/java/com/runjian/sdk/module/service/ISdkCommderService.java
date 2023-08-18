package com.runjian.sdk.module.service;

import com.runjian.domain.dto.commder.*;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public interface ISdkCommderService {

    /**
     * 登陆
     * @param ip
     * @param port
     * @param user
     * @param psw
     * @return
     */
    DeviceLoginDto login(String ip, int port, String user, String psw);

    /**
     *
     * @param lUserId
     * @return
     */
    DeviceLoginOutDto logout(long lUserId);



    /**
     * 获取设备配置信息
     * @param lUserId
     * @return
     */
    DeviceConfigDto deviceConfig(long lUserId);


    /**
     * 通道同步
     * @param lUserId
     * @param channelNum
     * @return
     */
    ChannelInfoDto channelSync(long lUserId,int channelNum);
    /**
     * 获取录像信息
     * @param lUserId
     * @param lChannel
     * @param startTime
     * @param endTime
     * @return
     */
    RecordInfoDto recordList(long lUserId,int lChannel,String startTime,String endTime);



    /**
     * ptz操作
     * @param lUserId
     * @param lChannel
     * @param dwPTZCommand
     * @param dwStop
     * @param dwSpeed
     * @return
     */
    Integer ptzControl(long lUserId,int lChannel,int dwPTZCommand, int dwStop,int dwSpeed);


    /**
     * ptz操作
     * @param lUserId
     * @param lChannel
     * @return
     */
    PresetQueryDto presetList(long lUserId,int lChannel);


    /**
     * 预置位设置
     * @param lUserId
     * @param lChannel
     * @param commond
     * @param presetNum
     * @return
     */
    Integer presetControl(long lUserId,int lChannel,int commond, int presetNum);

    /**
     * 3d放大功能
     * @param lUserId
     * @param lChannel
     * @param xTop
     * @param yTop
     * @param xBottom
     * @param yBottom
     * @param dragType
     * @return
     */
    Integer Zoom3DControl(long lUserId, int lChannel, int xTop, int yTop,int xBottom,int yBottom,int dragType);

    /**
     * 回放控制
     * @param lPlayHandle
     * @param dwControlCode
     * @param value
     * @return
     */
    Integer playBackControl(int lPlayHandle, int dwControlCode, int value);

    /**
     * 设备远程控制
     * @param lUserId
     * @param dwCommand
     * @return
     */
    Integer remoteControl(long lUserId, int dwCommand,String loginHandle);

    Integer intellectAlarm(long lUserId, int channelNm,int dwUser);
}
