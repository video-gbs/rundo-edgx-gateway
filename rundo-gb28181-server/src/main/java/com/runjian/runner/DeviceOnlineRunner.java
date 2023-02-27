package com.runjian.runner;

import com.runjian.gb28181.bean.Device;
import com.runjian.service.IDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * 系统启动时控制设备
 * @author lin
 */
@Component
@Order(value=4)
public class DeviceOnlineRunner implements CommandLineRunner {

    @Autowired
    private IDeviceService deviceService;

    @Override
    public void run(String... args) throws Exception {
        List<Device> deviceList = deviceService.getAllOnlineDevice();

        for (Device device : deviceList) {
            if (deviceService.expire(device)){
                deviceService.offline(device);
            }else {
                deviceService.online(device);
            }
        }
    }
}
