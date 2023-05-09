package com.runjian.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.runjian.domain.dto.commder.DeviceOnlineDto;
import com.runjian.entity.DeviceChannelEntity;
import com.runjian.entity.DeviceEntity;

import java.util.List;

/**
 * @author chenjialing
 */
public interface IDeviceService extends IService<DeviceEntity> {
    /**
     * 设备上线
     * String ip, short port, String user, String psw
     */
    DeviceOnlineDto online(String ip, short port, String user, String psw);

    /**
     * 设备下线
     * @param lUserId 登录句柄
     */
    void offline(int lUserId);


    /**
     * 设备信息获取
     * @param id 数据库id
     * @param lUserId 登录句柄
     */
    void deviceInfo(DeviceEntity deviceEntity, int lUserId);






}
