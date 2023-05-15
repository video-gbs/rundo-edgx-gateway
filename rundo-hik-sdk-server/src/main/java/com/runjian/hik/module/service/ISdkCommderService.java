package com.runjian.hik.module.service;

import com.runjian.domain.dto.commder.ChannelInfoDto;
import com.runjian.domain.dto.commder.DeviceConfigDto;
import com.runjian.domain.dto.commder.DeviceLoginDto;
import com.runjian.domain.dto.commder.PlayInfoDto;
import com.runjian.hik.sdklib.HCNetSDK;

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
    PlayInfoDto play(int lUserId, int channelNum, int dwStreamType, int dwLinkMode);

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


}
