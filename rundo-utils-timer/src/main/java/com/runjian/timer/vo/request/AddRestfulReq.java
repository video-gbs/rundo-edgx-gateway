package com.runjian.timer.vo.request;

import com.runjian.timer.vo.JobData;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 添加请求任务请求体
 * @author Miracle
 * @date 2022/4/24 9:16
 */
@Data
public class AddRestfulReq extends JobData  {



    /**
     * 方法
     */
    @NotBlank(message = "方法不能为空")
    @Size(min = 1, max = 20, message = "非法请求方法")
    private String method;

    /**
     * URL连接
     */
    @NotBlank(message = "URL链接不能为空")
    @Size(min = 1, max = 500, message = "非法URL链接")
    private String url;

    /**
     * 请求参数
     */
    @Size(min = 0, max = 50, message = "参数过多")
    private Map<String, Object> parameter;



}
