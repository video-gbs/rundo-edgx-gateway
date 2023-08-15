package com.runjian.domain.dto.commder;

import com.runjian.domain.dto.PresetQueryReq;
import lombok.Data;

import java.util.List;

/**
 * @author chenjialing
 */
@Data
public class PresetQueryDto {

    private int errorCode = 0;

    private List<PresetQueryReq> presetQueryReqList;
}
