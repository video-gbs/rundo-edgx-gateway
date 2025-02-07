package com.runjian.device;

import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.CommonResponse;
import com.runjian.common.constant.LogTemplate;
import com.runjian.entity.DeviceChannelEntity;
import com.runjian.hik.sdklib.SocketPointer;
import com.runjian.service.IDeviceChannelService;
import com.runjian.service.IDeviceService;
import com.sun.jna.Pointer;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class DeviceTest {
    @Autowired
    IDeviceService deviceService;
//    @Value("${mdeia-tool-uri-list.server-ip}")
    private String serverIp;

    @Autowired
    private IDeviceChannelService deviceChannelService;
    @Test
    public void testLogin(){

//        deviceService.online("192.168.0.203",(short)8000,"admin","rj123456");
//        deviceService.online("192.168.0.241",(short)8000,"admin","rj123456");
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

    static void work(Pointer p,String socket)
    {
        SocketPointer socketPointer = new SocketPointer();
        Pointer pointer = socketPointer.getPointer();
        socketPointer.socketHandle=socket;
        socketPointer.write();
        p.write(0, pointer.getByteArray(0, socketPointer.size()), 0, socketPointer.size());
    }

    @Test
    public void testSocketnew(){
        //进行socket 连接
        try{
//            Socket socket = new Socket("192.168.0.132", 10000);
            SocketPointer socketDto = new SocketPointer();
            socketDto.socketHandle = "123";
            Pointer p=socketDto.getPointer();
            socketDto.write();
            work(p,"234");
            socketDto.read();
            System.out.println("修改后的dwsize = "+ socketDto.socketHandle);

        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"自研流媒体服务连接-socket-连接业务异常","aa",e);
        }
    }
    @Test
    public void testInsertMysql(){
        long stime = System.currentTimeMillis(); // 统计开始时间
        ArrayList<DeviceChannelEntity> deviceChannelEntities = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            DeviceChannelEntity deviceChannelEntity = new DeviceChannelEntity();
            deviceChannelEntity.setDeviceId("24010200492000000001");
            deviceChannelEntity.setChannelId("2401020049132000000"+i);
            deviceChannelEntity.setChannelName("test_"+i);
            deviceChannelEntity.setOnline(1);
            deviceChannelEntity.setManufacturer("rundo");
            deviceChannelEntity.setPtzType(1);


        }
        deviceChannelService.saveBatch(deviceChannelEntities);


        long etime = System.currentTimeMillis(); // 统计结束时间
        System.out.println("执行时间：" + (etime - stime));

    }
}
