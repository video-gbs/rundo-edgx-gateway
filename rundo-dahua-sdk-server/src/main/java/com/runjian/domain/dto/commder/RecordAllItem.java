package com.runjian.domain.dto.commder;

import lombok.Data;

import java.util.List;

/**
 *	录像列表
 * @author chenjialing
 */
@Data
public class RecordAllItem {

	private String name;

	private int sumNum;

	private List<RecordItem> recordList;


}
