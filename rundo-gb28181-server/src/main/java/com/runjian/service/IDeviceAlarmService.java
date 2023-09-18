package com.runjian.service;


import com.runjian.gb28181.bean.DeviceAlarm;

import java.util.List;

/**
 * 报警相关业务处理
 */
public interface IDeviceAlarmService {



    /**
     * 添加一个报警
     * @param deviceAlarm 添加报警
     */
    void add(DeviceAlarm deviceAlarm);


}
