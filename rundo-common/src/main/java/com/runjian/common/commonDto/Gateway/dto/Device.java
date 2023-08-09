package com.runjian.common.commonDto.Gateway.dto;


import lombok.Data;

import java.time.LocalDateTime;

/**
 * 国标设备/平台
 * @author lin
 */
@Data
public class Device {
	/**
	 * 数据id
	 */
	private long id;
	/**
	 * 设备国标编号
	 */
	private String deviceId;

	/**
	 * 设备名
	 */
	private String name;

	/**
	 * 生产厂商
	 */
	private String manufacturer;

	/**
	 * 型号
	 */
	private String model;

	/**
	 * 固件版本
	 */
	private String firmware;

	/**
	 * 传输协议
	 * UDP/TCP
	 */
	private String transport;

	/**
	 * 数据流传输模式
	 * UDP:udp传输
	 * TCP-ACTIVE：tcp主动模式
	 * TCP-PASSIVE：tcp被动模式
	 */
	private String streamMode;

	/**
	 * wan地址_ip
	 */
	private String  ip;

	/**
	 * wan地址_port
	 */
	private int port;

	/**
	 * wan地址
	 */
	private String  hostAddress;

	/**
	 * 在线
	 */
	private int online;

	/**
	 * 注册时间
	 */
	private String registerTime;


	/**
	 * 心跳时间
	 */
	private String keepaliveTime;

	/**
	 * 注册有效期
	 */
	private Integer keepaliveIntervalTime;

	/**
	 * 注册有效期
	 */
	private int expires;

	/**
	 * 字符集, 支持 UTF-8 与 GB2312
	 */
	private String charset ;
	/**
	 * 是否开启ssrc校验，默认关闭，开启可以防止串流
	 */
	private boolean ssrcCheck = true;

	private String password;

	private LocalDateTime createdAt;


	private LocalDateTime updatedAt;

}
