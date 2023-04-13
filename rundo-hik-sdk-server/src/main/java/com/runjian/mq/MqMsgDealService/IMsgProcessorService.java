package com.runjian.mq.MqMsgDealService;


import com.runjian.common.mq.domain.CommonMqDto;

/**
 *
 * @author chenjialing
 */
public interface IMsgProcessorService {

	/**
	 * 消息处理
	 * @param commonMqDto
	 */
	void process(CommonMqDto commonMqDto);

}
