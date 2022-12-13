package com.runjian.service.impl;

import com.runjian.common.constant.DeviceCompatibleEnum;
import com.runjian.common.constant.LogTemplate;
import com.runjian.common.utils.BeanUtil;
import com.runjian.conf.DynamicTask;
import com.runjian.dao.DeviceChannelMapper;
import com.runjian.dao.DeviceCompatibleMapper;
import com.runjian.dao.DeviceMapper;
import com.runjian.domain.dto.DeviceDto;
import com.runjian.gb28181.bean.Device;
import com.runjian.gb28181.transmit.cmd.ISIPCommander;
import com.runjian.gb28181.transmit.event.request.impl.message.response.cmd.CatalogResponseMessageHandler;
import com.runjian.service.IDeviceService;
import com.runjian.utils.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sip.InvalidArgumentException;
import javax.sip.SipException;
import java.text.ParseException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * 设备业务
 */
@Service
@Slf4j
public class DeviceServiceImpl implements IDeviceService {



    @Autowired
    private DeviceMapper deviceMapper;

    @Autowired
    private DeviceChannelMapper deviceChannelMapper;

    @Autowired
    private DeviceCompatibleMapper deviceCompatibleMapper;


    @Autowired
    private ISIPCommander sipCommander;

    private final String  registerExpireTaskKeyPrefix = "device-register-expire-";

    @Autowired
    private CatalogResponseMessageHandler catalogResponseMessageHandler;

    @Autowired
    private DynamicTask dynamicTask;

    @Override
    public void online(DeviceDto device) {

        log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "设备服务", "设备上线", device);
        //转换为gb28181专用的bean
        Device deviceBean = new Device();
        BeanUtil.copyProperties(device, deviceBean);

        // 第一次上线 或则设备之前是离线状态--进行通道同步和设备信息查询
        if (device.getCreatedAt() == null) {
            device.setOnline(1);
            log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "设备服务", "设备上线-首次注册,查询设备信息以及通道信息", device.getDeviceId());
            deviceMapper.add(device);
            try {
                sipCommander.deviceInfoQuery(deviceBean);
            } catch (InvalidArgumentException | SipException | ParseException e) {
                log.error(LogTemplate.ERROR_LOG_TEMPLATE, "设备服务", "[命令发送失败] 查询设备信息", e);
            }
            sync(deviceBean);
        }else {

            if(device.getOnline() == 0){
                device.setOnline(1);
                log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "设备服务", "设备上线,离线状态下重新注册", device.getDeviceId());
                deviceMapper.update(device);
                try {
                    sipCommander.deviceInfoQuery(deviceBean);
                } catch (InvalidArgumentException | SipException | ParseException e) {
                    log.error(LogTemplate.ERROR_LOG_TEMPLATE, "设备服务", "[命令发送失败] 查询设备信息", e);
                }
                sync(deviceBean);
            }


            deviceMapper.update(device);

        }

        // 刷新过期任务
        if(deviceCompatibleMapper.getByDeviceId(device.getDeviceId(), DeviceCompatibleEnum.HUAWEI_NVR_800.getType()) == null){
            //华为nvr800 不做定时过期限制
            String registerExpireTaskKey = registerExpireTaskKeyPrefix + device.getDeviceId();
            dynamicTask.startDelay(registerExpireTaskKey, ()-> offline(device), device.getExpires() * 1000);
        }

    }

    @Override
    public void offline(DeviceDto device) {
        log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "设备服务", "设备离线", device);
        //判断数据库中是否存在
        if(device.getId() == 0){
            log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "设备服务", "设备离线--设备信息不存在", device);
            return;
        }
        String deviceId = device.getDeviceId();
        String registerExpireTaskKey = registerExpireTaskKeyPrefix + deviceId;
        dynamicTask.stop(registerExpireTaskKey);
        device.setOnline(0);
        deviceMapper.update(device);
        //进行通道离线
        deviceChannelMapper.offlineByDeviceId(deviceId);
        //  TODO离线释放所有ssrc
//        List<SsrcTransaction> ssrcTransactions = streamSession.getSsrcTransactionForAll(deviceId, null, null, null);
//        if (ssrcTransactions != null && ssrcTransactions.size() > 0) {
//            for (SsrcTransaction ssrcTransaction : ssrcTransactions) {
//                mediaServerService.releaseSsrc(ssrcTransaction.getMediaServerId(), ssrcTransaction.getSsrc());
//                mediaServerService.closeRTPServer(ssrcTransaction.getMediaServerId(), ssrcTransaction.getStream());
//                streamSession.remove(deviceId, ssrcTransaction.getChannelId(), ssrcTransaction.getStream());
//            }
//        }
    }

    @Override
    public DeviceDto getDevice(String deviceId) {
        return deviceMapper.getDeviceByDeviceId(deviceId);

    }

    @Override
    public void sync(Device device) {

        int sn = (int)((Math.random()*9+1)*100000);
        try {
            sipCommander.catalogQuery(device, sn, event -> {
                String errorMsg = String.format("同步通道失败，错误码： %s, %s", event.statusCode, event.msg);
                catalogResponseMessageHandler.setChannelSyncEnd(device.getDeviceId(), errorMsg);
            });
        } catch (SipException | InvalidArgumentException | ParseException e) {
            log.error(LogTemplate.ERROR_LOG_MSG_TEMPLATE, "设备服务", "同步通道-信令发送失败", device.getDeviceId(), e);
            String errorMsg = String.format("同步通道失败，信令发送失败： %s", e.getMessage());
            catalogResponseMessageHandler.setChannelSyncEnd(device.getDeviceId(), errorMsg);
        }
    }

    @Override
    public void updateDevice(Device device) {
        device.setCharset(device.getCharset().toUpperCase());

        DeviceDto deviceDto = new DeviceDto();
        BeanUtil.copyProperties(device,deviceDto);
        deviceMapper.update(deviceDto);

    }

    @Override
    public boolean expire(Device device) {
        Instant registerTimeDate = Instant.from(DateUtil.formatter.parse(device.getRegisterTime()));
        Instant expireInstant = registerTimeDate.plusMillis(TimeUnit.SECONDS.toMillis(device.getExpires()));
        return expireInstant.isBefore(Instant.now());
    }
}
