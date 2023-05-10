package com.runjian.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @author chenjialing
 */
@Data
@TableName("rundo_device_channel")
public class DeviceChannelEntity {


	/**
	 * 数据库自增ID
	 */

	private long id;

	/**
	 * 通道
	 */

	private String channelId;


	/**
	 * 通道号
	 */
	private Integer channelNum;

	/**
	 * 原国标id--置空
	 */

	private String deviceId;

	/**
	 * 编码器id
	 */

	private Long encodeId;
	/**
	 * 通道名
	 */
	private String channelName;

	/**
	 * 生产厂商
	 */
	private String manufacturer;



	/**
	 * IP地址
	 */

	private String ip;

	/**
	 * 端口号
	 */

	private int port;

	/**
	 * 密码
	 */

	private String password;

	/**
	 * 云台类型
	 */

	private int ptzType;



	/**
	 * ip/模拟通道
	 */

	private int isIpChannel;
	/**
	 * 在线/离线
	 * 1在线,0离线
	 * 默认在线
	 * 信令:
	 * <Status>ON</Status>
	 * <Status>OFF</Status>
	 * 遇到过NVR下的IPC下发信令可以推流， 但是 Status 响应 OFF
	 */

	private int online;



	private Date createdAt;

	private Date updatedAt;

}
