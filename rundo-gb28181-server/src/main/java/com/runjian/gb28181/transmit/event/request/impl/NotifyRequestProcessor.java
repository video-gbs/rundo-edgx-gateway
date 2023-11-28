package com.runjian.gb28181.transmit.event.request.impl;

import com.runjian.conf.SipConfig;
import com.runjian.gb28181.bean.CmdType;
import com.runjian.gb28181.bean.HandlerCatchData;
import com.runjian.gb28181.transmit.SIPProcessorObserver;
import com.runjian.gb28181.transmit.event.request.ISIPRequestProcessor;
import com.runjian.gb28181.transmit.event.request.SIPRequestProcessorParent;
import com.runjian.gb28181.utils.XmlUtil;
import com.runjian.runner.CivilCodeFileConfRunner;
import com.runjian.service.IDeviceChannelService;
import gov.nist.javax.sip.message.SIPRequest;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.SipException;
import javax.sip.message.Response;
import java.text.ParseException;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * SIP命令类型： NOTIFY请求,这是作为上级发送订阅请求后，设备才会响应的
 */
@Component
public class NotifyRequestProcessor extends SIPRequestProcessorParent implements InitializingBean, ISIPRequestProcessor {


	private final static Logger logger = LoggerFactory.getLogger(NotifyRequestProcessor.class);



	@Autowired
	private SipConfig sipConfig;



	private final String method = "NOTIFY";

	@Autowired
	private SIPProcessorObserver sipProcessorObserver;

	@Autowired
	private IDeviceChannelService deviceChannelService;

	@Autowired
	private NotifyRequestForCatalogProcessor notifyRequestForCatalogProcessor;

	@Autowired
	private CivilCodeFileConfRunner civilCodeFileConf;

	private ConcurrentLinkedQueue<HandlerCatchData> taskQueue = new ConcurrentLinkedQueue<>();

	@Qualifier("taskExecutor")
	@Autowired
	private ThreadPoolTaskExecutor taskExecutor;

	private int maxQueueCount = 30000;

	@Override
	public void afterPropertiesSet() throws Exception {
		// 添加消息处理的订阅
		sipProcessorObserver.addRequestProcessor(method, this);
	}

	@Override
	public void process(RequestEvent evt) {
		try {
			responseAck((SIPRequest) evt.getRequest(), Response.OK, null, null);
		} catch (SipException | InvalidArgumentException | ParseException e) {
			logger.error("未处理的异常 ", e);
		}
		boolean runed = !taskQueue.isEmpty();
		logger.info("[notify] 待处理消息数量： {}", taskQueue.size());
		taskQueue.offer(new HandlerCatchData(evt, null, null));
		if (!runed) {
			taskExecutor.execute(() -> {
				while (!taskQueue.isEmpty()) {
					try {
						HandlerCatchData take = taskQueue.poll();
						if (take == null) {
							continue;
						}
						Element rootElement = getRootElement(take.getEvt());
						if (rootElement == null) {
							logger.error("处理NOTIFY消息时未获取到消息体,{}", take.getEvt().getRequest());
							continue;
						}
						String cmd = XmlUtil.getText(rootElement, "CmdType");

						if (CmdType.CATALOG.equals(cmd)) {
							logger.info("接收到Catalog通知");
							notifyRequestForCatalogProcessor.process(take.getEvt());
						} else if (CmdType.ALARM.equals(cmd)) {
							logger.info("接收到Alarm通知,暂不处理");
						} else if (CmdType.MOBILE_POSITION.equals(cmd)) {
							logger.info("接收到MobilePosition通知，暂不处理");
						} else {
							logger.info("接收到消息：" + cmd);
						}
					} catch (DocumentException e) {
						logger.error("处理NOTIFY消息时错误", e);
					}
				}
			});
		}
	}



}
