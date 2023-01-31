package com.runjian.common.commonDto.Gateway.dto;

import lombok.Data;

import java.util.List;

/**
 *	录像列表
 * @author chenjialing
 */
@Data
public class ChannelRecordInfo {

	private int sumNum;

	private List<ChannelRecordItem> channelRecordList;

}
