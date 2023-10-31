package com.runjian.common.commonDto.Gateway.dto;


import lombok.Data;

import java.time.LocalDateTime;

/**
 * 国标设备/平台
 * @author lin
 */
@Data
public class AlarmSendDto {
	/**
	 * 设备国标编号
	 */
	private String deviceId;
	/**
	 * 通道Id
	 */
	private long channelId;
	/**
	 * 事件编码
	 *     //移动侦测
	 *     MOVE_ALARM("1"),
	 *     //遮挡告警
	 *     COVER_ALARM("2"),
	 *     //区域入侵
	 *     REGIONAL_ALARM("3"),
	 *     //绊线入侵
	 *     TRIPPING_WIRE_ALARM("4")
	 */
	private String eventCode;
	/**
	 * 事件描述
	 */
	private String eventDesc;
	/**
	 * 事件类型
	 */
	private long eventMsgType;
	/**
	 * 事件时间
	 */
	private String eventTime;



}
