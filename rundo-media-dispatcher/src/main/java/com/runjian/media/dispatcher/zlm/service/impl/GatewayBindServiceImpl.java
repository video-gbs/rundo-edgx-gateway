package com.runjian.media.dispatcher.zlm.service.impl;

import com.runjian.common.commonDto.Gb28181Media.req.GatewayBindReq;
import com.runjian.common.utils.BeanUtil;
import com.runjian.media.dispatcher.zlm.dto.dao.GatewayBind;
import com.runjian.media.dispatcher.zlm.mapper.GatewayBindMapper;
import com.runjian.media.dispatcher.zlm.service.IGatewayBindService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
public class GatewayBindServiceImpl implements IGatewayBindService {
    @Autowired
    GatewayBindMapper gatewayBindMapper;

    @Value("${media.id}")
    String mediaServerId;
    @Override
    public int edit(GatewayBindReq gatewayBindReq) {
        GatewayBind gatewayBind = new GatewayBind();
        GatewayBind gatewayBindDb = gatewayBindMapper.queryOneByGatewayId(gatewayBindReq.getGatewayId());
        //获取zlm的id
        int i;
        if(ObjectUtils.isEmpty(gatewayBindDb)){
            BeanUtil.copyProperties(gatewayBindReq,gatewayBind);

            i = gatewayBindMapper.add(gatewayBind);

        }else {
            BeanUtil.copyProperties(gatewayBindReq,gatewayBindDb);
            gatewayBindDb.setMediaServerId(mediaServerId);

            i = gatewayBindMapper.update(gatewayBindDb);
        }

        return i;
    }

    @Override
    public GatewayBind findOne(String gatewayId) {
        return gatewayBindMapper.queryOneByGatewayId(gatewayId);
    }

    @Override
    public GatewayBind findOneByMediaId(String mediaServerId) {
        return gatewayBindMapper.queryOneByMediaServerId(mediaServerId);
    }
}
