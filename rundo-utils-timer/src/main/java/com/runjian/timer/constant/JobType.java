package com.runjian.timer.constant;

import com.runjian.timer.vo.JobData;
import com.runjian.timer.vo.request.AddDatabaseReq;
import com.runjian.timer.vo.request.AddRestfulReq;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Miracle
 * @date 2019/7/9 15:13
 */

@Getter
@AllArgsConstructor
public enum JobType {
    DATABASE(1,"DATABASE", "TIMER_DB_GROUP_KEY_"),
    RESTFUL(2, "RESTFUL", "TIMER_RF_GROUP_KEY_"),
    ;

    private final Integer code;

    private final String msg;

    private final String redisKey;

    public static JobType getJobTypeByCode(int code){
        for (JobType jobType : JobType.values()){
            if (jobType.getCode().equals(code)){
                return jobType;
            }
        }
        return null;
    }

}
