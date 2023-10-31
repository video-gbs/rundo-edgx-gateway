package com.runjian.service;

import com.runjian.domain.req.DeviceReq;
import com.runjian.gb28181.bean.Device;
import com.runjian.gb28181.event.SipSubscribe;

import javax.sip.InvalidArgumentException;
import javax.sip.SipException;
import java.text.ParseException;
import java.util.List;

public interface IDeviceService {
    /**
     * 设备上线
     * @param device 设备信息
     */
    void online(Device device);

    /**
     * 设备下线
     * @param device 设备信息
     */
    void offline(Device device);

    /**
     * 查询设备信息
     * @param deviceId 设备编号
     * @return 设备信息
     */
    Device getDevice(String deviceId);

    /**
     * 同步设备信息
     * @param device
     */
    void sync(Device device,String msgId);

    /**
     * 更新设备
     * @param device 设备信息
     */
    void updateDevice(Device device);

    /**
     * 判断是否注册已经失效
     * @param device 设备信息
     * @return 布尔
     */
    boolean expire(Device device);

    /**
     * 查询设备信息
     * @param device
     */
    void deviceInfoQuery(Device device,String msgId);

    /**
     *
     * @param deviceId
     * @return
     */
    void deviceDelete(String deviceId,String msgId);


    /**
     * 设备软删除
     * @param deviceId
     * @return
     */
    void deviceSoftDelete(String deviceId,String msgId);

    /**
     * 设备删除恢复
     * @param deviceId
     * @param msgId
     */
    void deviceDeleteRecover(String deviceId,String msgId);
    /**
     * 设备全量数据同步
     * @param msgId
     * @return
     */
    void deviceList(String msgId);


    /**
     * 获取所有在线设备
     * @return 设备列表
     */
    List<Device> getAllOnlineDevice();

    /**
     * 布防
     * @param deviceId
     */
    void guardAlarm(String deviceId,String channelId, String guardCmdStr, SipSubscribe.Event errorEvent) throws InvalidArgumentException, ParseException, SipException;
}
