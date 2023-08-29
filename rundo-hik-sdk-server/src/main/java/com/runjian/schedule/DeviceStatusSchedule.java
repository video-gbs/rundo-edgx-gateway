package com.runjian.schedule;

import com.runjian.entity.DeviceEntity;
import com.runjian.service.IDeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * 定时发送心跳
 * @author chenjialing
 */
@Component
@Slf4j
public class DeviceStatusSchedule {

    @Autowired
    IDeviceService deviceService;

    //每3分钟执行一次
    @Scheduled(fixedRate=300000)
    public void deviceStatusCheck(){
        List<DeviceEntity> deviceEntities = deviceService.deviceList();
        for (DeviceEntity one : deviceEntities){
            deviceService.checkDeviceStatus(one);
        }

    }
}
