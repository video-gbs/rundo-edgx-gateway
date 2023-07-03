package com.runjian.domain.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author chenjialing
 */
@Data
public class RecordInfoSdkReq implements Serializable {
    @NotBlank(message = "lUserId不得为空")
    @JsonProperty("lUserId")
    private Integer lUserId;

    @NotBlank(message = "lChannel不得为空")
    @JsonProperty("lChannel")
    private Integer lChannel;

    @NotBlank(message = "不得为空")
    private String startTime;

    @NotBlank(message = "不得为空")
    private String endTime;
}
