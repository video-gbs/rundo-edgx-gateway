package com.runjian.gb28181.transmit.event.request.impl.message.response.cmd;

import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.constant.BusinessSceneConstants;
import com.runjian.common.constant.GatewayBusinessMsgType;
import com.runjian.common.constant.LogTemplate;
import com.runjian.common.constant.PtzOperationTypeEnum;
import com.runjian.gb28181.bean.Device;
import com.runjian.gb28181.bean.ParentPlatform;
import com.runjian.gb28181.bean.PresetQuerySipReq;
import com.runjian.gb28181.transmit.event.request.SIPRequestProcessorParent;
import com.runjian.gb28181.transmit.event.request.impl.message.IMessageHandler;
import com.runjian.gb28181.transmit.event.request.impl.message.response.ResponseMessageHandler;
import com.runjian.service.IRedisCatchStorageService;
import gov.nist.javax.sip.message.SIPRequest;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.SipException;
import javax.sip.message.Response;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.runjian.gb28181.utils.XmlUtil.getText;
/**
 * 设备预置位查询应答
 */
@Component
public class PresetQueryResponseMessageHandler extends SIPRequestProcessorParent implements InitializingBean, IMessageHandler {

    private Logger logger = LoggerFactory.getLogger(PresetQueryResponseMessageHandler.class);
    private final String cmdType = "PresetQuery";

    @Autowired
    private ResponseMessageHandler responseMessageHandler;


    @Autowired
    IRedisCatchStorageService redisCatchStorageService;

    @Override
    public void afterPropertiesSet() throws Exception {
        responseMessageHandler.addHandler(cmdType, this);
    }

    @Override
    public void handForDevice(RequestEvent evt, Device device, Element element) {

        SIPRequest request = (SIPRequest) evt.getRequest();

        try {
             Element rootElement = getRootElement(evt, device.getCharset());

            if (rootElement == null) {
                logger.warn(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "设备预置位查询应答", "content cannot be null", evt.getRequest());
                try {
                    responseAck(request, Response.BAD_REQUEST);
                } catch (InvalidArgumentException | ParseException | SipException e) {
                    logger.warn(LogTemplate.ERROR_LOG_TEMPLATE, "设备预置位查询应答", "命令发送失败", e);
                }
                return;
            }
            Element presetListNumElement = rootElement.element("PresetList");
            //该字段可能为通道或则设备的id
            String channelId = getText(rootElement, "DeviceID");
            //预置位查询的key
            String businessSceneKey = GatewayBusinessMsgType.CHANNEL_PTZ_PRESET.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+device.getDeviceId()+BusinessSceneConstants.SCENE_STREAM_KEY+channelId;

            int sumNum = Integer.parseInt(presetListNumElement.attributeValue("Num"));
            List<PresetQuerySipReq> presetQuerySipReqList = new ArrayList<>();
            if (sumNum > 0) {
                for (Iterator<Element> presetIterator = presetListNumElement.elementIterator(); presetIterator.hasNext(); ) {
                    Element itemListElement = presetIterator.next();
                    PresetQuerySipReq presetQuerySipReq = new PresetQuerySipReq();
                    for (Iterator<Element> itemListIterator = itemListElement.elementIterator(); itemListIterator.hasNext(); ) {
                        // 遍历item
                        Element itemOne = itemListIterator.next();
                        String name = itemOne.getName();
                        String textTrim = itemOne.getTextTrim();
                        if ("PresetID".equalsIgnoreCase(name)) {
                            presetQuerySipReq.setPresetId(Integer.parseInt(textTrim));
                        } else {
                            presetQuerySipReq.setPresetName(textTrim);
                        }
                    }
                    presetQuerySipReqList.add(presetQuerySipReq);
                }
            }
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey, BusinessErrorEnums.SUCCESS,presetQuerySipReqList);
            try {
                responseAck(request, Response.OK);
            } catch (InvalidArgumentException | ParseException | SipException e) {
                logger.error(LogTemplate.ERROR_LOG_TEMPLATE, "设备预置位查询应答", "命令发送失败", e);
            }
        } catch (DocumentException e) {
            logger.error(LogTemplate.ERROR_LOG_TEMPLATE, "设备预置位查询应答", "[解析xml]失败", e);
        }
    }

    @Override
    public void handForPlatform(RequestEvent evt, ParentPlatform parentPlatform, Element rootElement) {

    }

}
