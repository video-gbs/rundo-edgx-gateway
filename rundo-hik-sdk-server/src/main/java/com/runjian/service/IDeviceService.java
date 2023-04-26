package com.runjian.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.runjian.domain.dto.Device;
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
    void online(String ip, short port, String user, String psw);

    /**
     * 设备下线
     * @param lUserId 登录句柄
     */
    void offline(int lUserId);


    /**
     * 设备信息获取
     * @param lUserId 登录句柄
     */
    void deviceInfo(int lUserId);



    /**
     * 设备下线
     * @param lUserId 登录句柄
     */
    void nvrDeviceInfo(int lUserId);
}
