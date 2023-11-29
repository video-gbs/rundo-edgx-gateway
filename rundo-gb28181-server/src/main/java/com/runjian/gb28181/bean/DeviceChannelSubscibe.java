package com.runjian.gb28181.bean;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author chenjialing
 */
@Schema(description = "通道信息")
@Data
public class DeviceChannelSubscibe {


	DeviceChannel deviceChannel;
	/**
	 * 订阅事件
	 */
	String event;



}
