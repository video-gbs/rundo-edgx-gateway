package com.runjian.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 国标设备/平台
 * @author lin
 */
@Data
@TableName("rundo_device")
public class DeviceEntity {
	/**
	 * 数据id
	 */
	@TableId(value = "id",type = IdType.AUTO)
	private long id;
	/**
	 * 登陆句柄
	 */
	private Integer lUserId;

	/**
	 * 账户
	 */
	private String username;

	/**
	 * 序列号
	 */
	private String serialNumber;

	/**
	 * 设备名称
	 */
	private String name;

	/**
	 * 编码
	 */
	private String charset;

	/**
	 * wan地址_ip
	 */
	private String  ip;

	/**
	 * wan地址_port
	 */

	private Short port;


	/**
	 * 生产厂商
	 */
	private String manufacturer;

	/**
	 * 在线与否
	 */
	private int online;



	private int deviceType;



	private String password;

	private LocalDateTime createdAt;


	private LocalDateTime updatedAt;

}
