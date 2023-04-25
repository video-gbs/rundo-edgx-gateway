package com.runjian.service.impl;

import com.runjian.common.commonDto.Gateway.req.ChannelPtzControlReq;
import com.runjian.common.commonDto.Gateway.req.DragZoomControlReq;
import com.runjian.service.IPtzService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PtzServiceImpl implements IPtzService {
    @Override
    public void ptzControl(ChannelPtzControlReq channelPtzControlReq) {

    }

    @Override
    public void ptzPresetControl(String deviceId, String channelId, String msgId) {

    }

    @Override
    public void dragZoomControl(DragZoomControlReq dragZoomControlReq) {

    }
}
