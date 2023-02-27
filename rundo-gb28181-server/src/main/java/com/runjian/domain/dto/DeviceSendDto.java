package com.runjian.domain.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 国标设备/平台 发送信令的设备实体
 * @author lin
 */
@Schema(description = "国标设备/平台")
@Data
public class DeviceSendDto {
	/**
	 * 数据id
	 */
	private long id;
	/**
	 * 设备国标编号
	 */
	@Schema(description = "设备国标编号")
	private String deviceId;

	/**
	 * 设备名
	 */
	@Schema(description = "名称")
	private String name;

	/**
	 * 生产厂商
	 */
	@Schema(description = "生产厂商")
	private String manufacturer;

	/**
	 * 型号
	 */
	@Schema(description = "型号")
	private String model;

	/**
	 * 固件版本
	 */
	@Schema(description = "固件版本")
	private String firmware;



	/**
	 * wan地址_ip
	 */
	@Schema(description = "IP")
	private String  ip;

	/**
	 * wan地址_port
	 */
	@Schema(description = "端口")
	private int port;


	/**
	 * 在线
	 */
	@Schema(description = "是否在线，1为在线，0为离线")
	private int online;


}
