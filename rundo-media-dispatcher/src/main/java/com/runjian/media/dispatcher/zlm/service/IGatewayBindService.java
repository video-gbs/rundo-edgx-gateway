package com.runjian.media.dispatcher.zlm.service;

import com.runjian.common.commonDto.Gb28181Media.req.GatewayBindReq;
import com.runjian.media.dispatcher.zlm.dto.dao.GatewayBind;

public interface IGatewayBindService {

    /**
     * 编辑网关绑定
     * @param gatewayBindReq
     * @return
     */
    int edit(GatewayBindReq gatewayBindReq);



    /**
     * 查找网关信息
     * @param gatewayId
     * @return
     */
    GatewayBind findOne(String gatewayId);

    /**
     * 查找网关信息
     * @param mediaServerId
     * @return
     */
    GatewayBind findOneByMediaId(String mediaServerId);
}
