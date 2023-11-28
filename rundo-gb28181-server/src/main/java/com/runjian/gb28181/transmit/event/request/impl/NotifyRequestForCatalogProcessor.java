package com.runjian.gb28181.transmit.event.request.impl;

import com.runjian.conf.DynamicTask;
import com.runjian.conf.SipConfig;
import com.runjian.conf.UserSetting;
import com.runjian.gb28181.bean.Device;
import com.runjian.gb28181.bean.DeviceChannel;
import com.runjian.gb28181.event.subscribe.catalog.CatalogEvent;
import com.runjian.gb28181.transmit.event.request.SIPRequestProcessorParent;
import com.runjian.gb28181.utils.SipUtils;
import com.runjian.gb28181.utils.XmlUtil;
import com.runjian.runner.CivilCodeFileConfRunner;
import com.runjian.service.IDeviceChannelService;
import com.runjian.service.IDeviceService;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sip.RequestEvent;
import javax.sip.header.FromHeader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SIP命令类型： NOTIFY请求中的目录请求处理
 */
@Component
public class NotifyRequestForCatalogProcessor extends SIPRequestProcessorParent {


    private final static Logger logger = LoggerFactory.getLogger(NotifyRequestForCatalogProcessor.class);


	@Autowired
	IDeviceService deviceService;

	@Autowired
	private UserSetting userSetting;



	@Autowired
	private IDeviceChannelService deviceChannelService;

	@Autowired
	private DynamicTask dynamicTask;

	@Autowired
	private CivilCodeFileConfRunner civilCodeFileConf;

	@Autowired
	private SipConfig sipConfig;

	private final static String talkKey = "notify-request-for-catalog-task";

	public void process(RequestEvent evt) {
		try {
			long start = System.currentTimeMillis();
			FromHeader fromHeader = (FromHeader) evt.getRequest().getHeader(FromHeader.NAME);
			String deviceId = SipUtils.getUserIdFromFromHeader(fromHeader);

			Device device = deviceService.getDevice(deviceId);
			if (device == null) {
				logger.warn("[收到目录订阅]：{}, 但是设备不存在", deviceId);
				return;
			}
			Element rootElement = getRootElement(evt, device.getCharset());
			if (rootElement == null) {
				logger.warn("[ 收到目录订阅 ] content cannot be null, {}", evt.getRequest());
				return;
			}
			Element deviceListElement = rootElement.element("DeviceList");
			if (deviceListElement == null) {
				return;
			}
			Iterator<Element> deviceListIterator = deviceListElement.elementIterator();
			if (deviceListIterator != null) {

				// 遍历DeviceList
				while (deviceListIterator.hasNext()) {
					Element itemDevice = deviceListIterator.next();
					Element channelDeviceElement = itemDevice.element("DeviceID");
					if (channelDeviceElement == null) {
						continue;
					}
					Element eventElement = itemDevice.element("Event");
					String event;
					if (eventElement == null) {
						logger.warn("[收到目录订阅]：{}, 但是Event为空, 设为默认值 ADD", (device != null ? device.getDeviceId():"" ));
						event = CatalogEvent.ADD;
					}else {
						event = eventElement.getText().toUpperCase();
					}
					DeviceChannel channel = XmlUtil.channelContentHandler(itemDevice, device, event, civilCodeFileConf);
					if (channel == null) {
						logger.info("[收到目录订阅]：但是解析失败 {}", new String(evt.getRequest().getRawContent()));
						continue;
					}
					if (channel.getParentId() != null && channel.getParentId().equals(sipConfig.getId())) {
						channel.setParentId(null);
					}
					channel.setDeviceId(device.getDeviceId());
					logger.info("[收到目录订阅]：{}/{}", device.getDeviceId(), channel.getChannelId());
					switch (event) {
						case CatalogEvent.ON:
							// 上线


							break;
						case CatalogEvent.OFF :
							// 离线

							break;
						case CatalogEvent.VLOST:
							// 视频丢失

							break;
						case CatalogEvent.DEFECT:
							// 故障

							break;
						case CatalogEvent.ADD:
							// 增加


							break;
						case CatalogEvent.DEL:
							// 删除

							break;
						case CatalogEvent.UPDATE:
							// 更新
							logger.info("[收到更新通道通知] 来自设备: {}, 通道 {}", device.getDeviceId(), channel.getChannelId());

							break;
						default:
							logger.warn("[ NotifyCatalog ] event not found ： {}", event );

					}
					// 转发变化信息
				}
			}
		} catch (DocumentException e) {
			logger.error("未处理的异常 ", e);
		}
	}

}
