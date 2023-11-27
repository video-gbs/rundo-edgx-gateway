package com.runjian.gb28181.bean;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author chenjialing
 */
@Schema(description = "通道信息")
@Data
public class DeviceChannel {


	/**
	 * 数据库自增ID
	 */
	@Schema(description = "数据库自增ID")
	private long id;

	/**
	 * 通道国标编号
	 */
	@Schema(description = "通道国标编号")
	private String channelId;

	/**
	 * 设备国标编号
	 */
	@Schema(description = "设备国标编号")
	private String deviceId;
	
	/**
	 * 通道名
	 */
	@Schema(description = "名称")
	private String channelName;
	
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
	 * 设备归属
	 */
	@Schema(description = "设备归属")
	private String owner;
	
	/**
	 * 行政区域
	 */
	@Schema(description = "行政区域")
	private String civilCode;
	
	/**
	 * 警区
	 */
	@Schema(description = "警区")
	private String block;

	/**
	 * 安装地址
	 */
	@Schema(description = "安装地址")
	private String address;
	
	/**
	 * 是否有子设备 1有, 0没有
	 */
	@Schema(description = "是否有子设备 1有, 0没有")
	private int parental;
	
	/**
	 * 父级id
	 */
	@Schema(description = "父级id")
	private String parentId;
	
	/**
	 * 信令安全模式  缺省为0; 0:不采用; 2: S/MIME签名方式; 3: S/ MIME加密签名同时采用方式; 4:数字摘要方式
	 */
	@Schema(description = "信令安全模式  缺省为0; 0:不采用; 2: S/MIME签名方式; 3: S/ MIME加密签名同时采用方式; 4:数字摘要方式")
	private int safetyWay;
	
	/**
	 * 注册方式 缺省为1;1:符合IETFRFC3261标准的认证注册模 式; 2:基于口令的双向认证注册模式; 3:基于数字证书的双向认证注册模式
	 */
	@Schema(description = "注册方式 缺省为1;1:符合IETFRFC3261标准的认证注册模 式; 2:基于口令的双向认证注册模式; 3:基于数字证书的双向认证注册模式")
	private int registerWay;
	
	/**
	 * 证书序列号
	 */
	@Schema(description = "证书序列号")
	private String certNum;
	
	/**
	 * 证书有效标识 缺省为0;证书有效标识:0:无效1: 有效
	 */
	@Schema(description = "证书有效标识 缺省为0;证书有效标识:0:无效1: 有效")
	private int certifiable;
	
	/**
	 * 证书无效原因码
	 */
	@Schema(description = "证书无效原因码")
	private int errCode;
	
	/**
	 * 证书终止有效期
	 */
	@Schema(description = "证书终止有效期")
	private String endTime;
	
	/**
	 * 保密属性 缺省为0; 0:不涉密, 1:涉密
	 */
	@Schema(description = "保密属性 缺省为0; 0:不涉密, 1:涉密")
	private String secrecy;
	
	/**
	 * IP地址
	 */
	@Schema(description = "IP地址")
	private String ipAddress;
	
	/**
	 * 端口号
	 */
	@Schema(description = "端口号")
	private int port;
	
	/**
	 * 密码
	 */
	@Schema(description = "密码")
	private String password;

	/**
	 * 云台类型
	 */
	@Schema(description = "云台类型")
	private int ptzType;


	
	/**
	 * 在线/离线
	 * 1在线,0离线
	 * 默认在线
	 * 信令:
	 * <Status>ON</Status>
	 * <Status>OFF</Status>
	 * 遇到过NVR下的IPC下发信令可以推流， 但是 Status 响应 OFF
	 */
	@Schema(description = "在线/离线， 1在线,0离线")
	private int status;

	/**
	 * 经度
	 */
	@Schema(description = "经度")
	private double longitude;
	
	/**
	 * 纬度
	 */
	@Schema(description = "纬度")
	private double latitude;

	/**
	 * 业务分组
	 */
	@Schema(description = "业务分组")
	private String businessGroupId;


	/**
	 * gb28181类型编码
	 */
	private int gbCode;



}
