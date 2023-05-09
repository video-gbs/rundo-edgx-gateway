package com.runjian.device;

import com.runjian.service.IDeviceService;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class DeviceTest {
    @Autowired
    IDeviceService deviceService;

    @Test
    public void testLogin(){

//        deviceService.online("192.168.0.203",(short)8000,"admin","rj123456");
        deviceService.online("192.168.0.241",(short)8000,"admin","rj123456");
    }

    @Test
    public void testOffline(){

        deviceService.offline(0);
    }

    @Test
    public void testDeviceInfo(){

        deviceService.deviceInfo(null,0);
    }
}
