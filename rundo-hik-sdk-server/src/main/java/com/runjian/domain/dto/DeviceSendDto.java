package com.runjian.domain.dto;


import lombok.Data;

/**
 * 国标设备/平台 发送信令的设备实体
 * @author lin
 */

@Data
public class DeviceSendDto {

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
	 * wan地址_ip
	 */

	private String  ip;

	/**
	 * wan地址_port
	 */

	private int port;


	/**
	 * 在线
	 */

	private int online;


}
