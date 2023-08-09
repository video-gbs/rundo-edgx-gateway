package com.runjian.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.runjian.common.config.response.CommonResponse;
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
     * 获取单个设备
     * @param id
     * @return
     */
    DeviceEntity getOne(Long id);

    /**
     * 设备添加
     * @param ip
     * @param port
     * @param user
     * @param psw
     * @return
     */
    CommonResponse<Long> add(String ip, short port, String user, String psw);

    /**
     * 设备状态检测
     * @param deviceEntity
     */
    void checkDeviceStatus(DeviceEntity deviceEntity);

    /**
     * 设备下线
     * @param lUserId 登录句柄
     */
    Boolean offline(long encodeId);


    /**
     * 设备信息获取
     * @param id 数据库id
     * @param lUserId 登录句柄
     */
    void deviceInfo(DeviceEntity deviceEntity, int lUserId);


    /**
     * 全量设备信息
     * @return
     */
    List<DeviceEntity> deviceList();

    /**
     *
     * @param deviceId
     * @return
     */
    void deviceDelete(long encodeId);


    /**
     * 设备软删除
     * @return
     */
    void deviceSoftDelete(long encodeId);


}
