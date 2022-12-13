package com.runjian.gb28181.transmit.event.request.impl.message.notify.cmd;

import com.runjian.common.constant.DeviceCompatibleEnum;
import com.runjian.common.constant.LogTemplate;
import com.runjian.common.utils.BeanUtil;
import com.runjian.dao.DeviceCompatibleMapper;
import com.runjian.domain.dto.DeviceDto;
import com.runjian.gb28181.bean.Device;
import com.runjian.gb28181.bean.ParentPlatform;
import com.runjian.gb28181.transmit.event.request.SIPRequestProcessorParent;
import com.runjian.gb28181.transmit.event.request.impl.message.IMessageHandler;
import com.runjian.gb28181.transmit.event.request.impl.message.notify.NotifyMessageHandler;
import com.runjian.service.IDeviceService;
import com.runjian.utils.DateUtil;
import gov.nist.javax.sip.message.SIPRequest;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.SipException;
import javax.sip.header.ViaHeader;
import javax.sip.message.Response;
import java.text.ParseException;

/**
 * 状态信息(心跳)报送
 */
@Component
public class KeepaliveNotifyMessageHandler extends SIPRequestProcessorParent implements InitializingBean, IMessageHandler {

    private Logger logger = LoggerFactory.getLogger(KeepaliveNotifyMessageHandler.class);
    private final static String cmdType = "Keepalive";

    @Autowired
    private NotifyMessageHandler notifyMessageHandler;

    @Autowired
    private IDeviceService deviceService;

    @Autowired
    private DeviceCompatibleMapper deviceCompatibleMapper;

    @Override
    public void afterPropertiesSet() throws Exception {
        notifyMessageHandler.addHandler(cmdType, this);
    }

    @Override
    public void handForDevice(RequestEvent evt, Device device, Element element) {
        if (device == null) {
            // 未注册的设备不做处理
            return;
        }
        // 回复200 OK
        try {
            responseAck((SIPRequest) evt.getRequest(), Response.OK);
        } catch (SipException | InvalidArgumentException | ParseException e) {
            logger.error(LogTemplate.ERROR_LOG_TEMPLATE, "状态信息(心跳)报送", "国标级联-心跳回复,命令发送失败", e);
        }
        // 判断RPort是否改变，改变则说明路由nat信息变化，修改设备信息
        // 获取到通信地址等信息
        ViaHeader viaHeader = (ViaHeader) evt.getRequest().getHeader(ViaHeader.NAME);
        String received = viaHeader.getReceived();
        int rPort = viaHeader.getRPort();
        // 解析本地地址替代
        if (ObjectUtils.isEmpty(received) || rPort == -1) {
            received = viaHeader.getHost();
            rPort = viaHeader.getPort();
        }
        if (device.getPort() != rPort) {
            device.setPort(rPort);
            device.setHostAddress(received.concat(":").concat(String.valueOf(rPort)));
        }
        device.setKeepaliveTime(DateUtil.getNow());

        if (device.getOnline() == 1) {
            deviceService.updateDevice(device);
        }else {
            DeviceDto deviceDto = new DeviceDto();
            BeanUtil.copyProperties(device,deviceDto);
            //判断是否华为nvr800 --有心跳即恢复上线
            if(deviceCompatibleMapper.getByDeviceId(device.getDeviceId(), DeviceCompatibleEnum.HUAWEI_NVR_800.getType())!=null){

                deviceService.online(deviceDto);
            }else {
                // 对于已经离线的设备判断他的注册是否已经过期
                if (!deviceService.expire(device)){
                    deviceService.online(deviceDto);
                }
            }
        }
    }

    @Override
    public void handForPlatform(RequestEvent evt, ParentPlatform parentPlatform, Element element) {
        // 不会收到上级平台的心跳信息

    }
}
