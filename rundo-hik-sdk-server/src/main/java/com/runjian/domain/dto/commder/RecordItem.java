package com.runjian.domain.dto.commder;


import lombok.Data;

/**
 * 单个录像列表
 * @author chenjialing
 */
@Data
public class RecordItem{

	private String deviceId;
	
	private String name;
	
	private String filePath;

	private String fileSize;

	private String address;
	
	private String startTime;
	
	private String endTime;
	
	private int secrecy;
	
	private String type;
	
	private String recorderId;

}
