package com.runjian.media.dispatcher.zlm.service.impl;

import com.runjian.common.commonDto.Gb28181Media.req.GatewayBindReq;
import com.runjian.common.utils.BeanUtil;
import com.runjian.media.dispatcher.zlm.dto.dao.GatewayBind;
import com.runjian.media.dispatcher.zlm.mapper.GatewayBindMapper;
import com.runjian.media.dispatcher.zlm.service.IGatewayBindService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;

public class GatewayBindServiceImpl implements IGatewayBindService {
    @Autowired
    GatewayBindMapper gatewayBindMapper;

    @Override
    public int edit(GatewayBindReq gatewayBindReq) {
        GatewayBind gatewayBind = gatewayBindMapper.queryOneByGatewayId(gatewayBindReq.getGatewayId());
        BeanUtil.copyProperties(gatewayBindReq,gatewayBind);
        int i;
        if(ObjectUtils.isEmpty(gatewayBind)){
            i = gatewayBindMapper.update(gatewayBind);
        }else {
            i = gatewayBindMapper.add(gatewayBind);
        }

        return i;
    }

    @Override
    public GatewayBind findOne(String gatewayId) {
        return gatewayBindMapper.queryOneByGatewayId(gatewayId);
    }
}
