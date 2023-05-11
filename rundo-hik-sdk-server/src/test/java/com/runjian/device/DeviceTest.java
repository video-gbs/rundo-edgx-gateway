package com.runjian.device;

import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.CommonResponse;
import com.runjian.common.constant.LogTemplate;
import com.runjian.service.IDeviceService;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class DeviceTest {
    @Autowired
    IDeviceService deviceService;
    @Value("${mdeia-tool-uri-list.server-ip}")
    private String serverIp;

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

    @Test
    public void testSocket(){
        //进行socket 连接
        try{
            Socket socket = new Socket(serverIp, 52428);

        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"自研流媒体服务连接-socket-连接业务异常","aa",e);
        }
    }
}
