package com.runjian.common.commonDto.Gb28181Media.resp;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author chenjialing
 */
@Data
public class StreamCheckListResp {

    private List<String> streamIdList;

    private LocalDateTime checkTime;

}
