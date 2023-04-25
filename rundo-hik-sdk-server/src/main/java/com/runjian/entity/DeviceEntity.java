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
	 * 数据流传输模式
	 * UDP:udp传输
	 * TCP-ACTIVE：tcp主动模式
	 * TCP-PASSIVE：tcp被动模式
	 */
	private int lUserId;

	private String userName;

	/**
	 * wan地址_ip
	 */
	private String  ip;

	/**
	 * wan地址_port
	 */

	private int port;
	


	private int online;



	private int deviceType;



	private String password;

	private LocalDateTime createdAt;


	private LocalDateTime updatedAt;

}
