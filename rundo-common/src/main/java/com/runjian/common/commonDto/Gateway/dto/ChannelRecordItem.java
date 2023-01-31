package com.runjian.common.commonDto.Gateway.dto;



import com.runjian.common.utils.DateUtils;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.time.temporal.TemporalAccessor;

/**
 * 单个录像列表
 * @author chenjialing
 */
@Data
public class ChannelRecordItem implements Comparable<ChannelRecordItem>{


	private String name;

	private String fileSize;

	private String startTime;
	
	private String endTime;



	@Override
	public int compareTo(@NotNull ChannelRecordItem recordItem) {
		TemporalAccessor startTimeNow = DateUtils.formatter.parse(startTime);
		TemporalAccessor startTimeParam = DateUtils.formatter.parse(recordItem.getStartTime());
		Instant startTimeParamInstant = Instant.from(startTimeParam);
		Instant startTimeNowInstant = Instant.from(startTimeNow);
		if (startTimeNowInstant.equals(startTimeParamInstant)) {
			return 0;
		}else if (Instant.from(startTimeParam).isAfter(Instant.from(startTimeNow)) ) {
			return -1;
		}else {
			return 1;
		}

	}
}
