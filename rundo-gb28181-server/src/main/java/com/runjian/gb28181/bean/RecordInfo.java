package com.runjian.gb28181.bean;

import lombok.Data;

import java.time.Instant;
import java.util.List;

/**
 *	录像列表
 * @author chenjialing
 */
@Data
public class RecordInfo {

	private String deviceId;

	private String channelId;

	private String sn;

	private String name;

	private int sumNum;

	private Instant lastTime;

	private List<RecordItem> recordList;


}
