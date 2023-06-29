package com.runjian.hik.module.service;

import com.runjian.domain.dto.commder.*;
import com.runjian.hik.sdklib.HCNetSDK;
import com.runjian.hik.sdklib.SocketPointer;

public interface ISdkCommderService {

    /**
     * 登陆
     * @param ip
     * @param port
     * @param user
     * @param psw
     * @return
     */
    DeviceLoginDto login(String ip, short port, String user, String psw);

    /**
     *
     * @param lUserId
     * @return
     */
    DeviceLoginOutDto logout(int lUserId);



    /**
     * 获取设备配置信息
     * @param lUserId
     * @return
     */
    DeviceConfigDto deviceConfig(int lUserId);


    /**
     * ipc通道
     * @param lUserId
     * @param devicecfgV40
     * @return
     */
    ChannelInfoDto getIpcChannelList(int lUserId, HCNetSDK.NET_DVR_DEVICECFG_V40 devicecfgV40);

    /**
     * dvr通道
     * @param lUserId
     * @param devicecfgV40
     * @return
     */
    ChannelInfoDto getDvrChannelList(int lUserId, HCNetSDK.NET_DVR_DEVICECFG_V40 devicecfgV40);

    /**
     * nvr通道
     * @param lUserId
     * @param devicecfgV40
     * @return
     */
    ChannelInfoDto getNvrChannelList(int lUserId, HCNetSDK.NET_DVR_DEVICECFG_V40 devicecfgV40);

    /**
     * 直播
     * @return
     */
    PlayInfoDto play(int lUserId, int channelNum, int dwStreamType, int dwLinkMode, SocketPointer socketPointer);

    /**
     * 预览回调码流信息
     * @param lPreviewHandle
     * @return
     */
    int playStandardCallBack(int lPreviewHandle);
    /**
     * 点播停止
     * @return
     */
    PlayInfoDto stopPlay(int lPreviewHandle);

    /**
     * 获取录像信息
     * @param lUserId
     * @param lChannel
     * @param startTime
     * @param endTime
     * @return
     */
    RecordInfoDto recordList(int lUserId,int lChannel,String startTime,String endTime);


    /**
     * 回放
     * @param lUserId
     * @param channelNum
     * @param startTime
     * @param endTime
     * @return
     */
    PlayInfoDto playBack(int lUserId, int channelNum, String startTime,String endTime, SocketPointer socketPointer);

    /**
     * ptz操作
     * @param lUserId
     * @param lChannel
     * @param dwPTZCommand
     * @param dwStop
     * @param dwSpeed
     * @return
     */
    Integer ptzControl(int lUserId,int lChannel,int dwPTZCommand, int dwStop,int dwSpeed);


    /**
     * ptz操作
     * @param lUserId
     * @param lChannel
     * @return
     */
    PresetQueryDto presetList(int lUserId,int lChannel);


    /**
     * 预置位设置
     * @param lUserId
     * @param lChannel
     * @param commond
     * @param presetNum
     * @return
     */
    Integer presetControl(int lUserId,int lChannel,int commond, int presetNum);


}
