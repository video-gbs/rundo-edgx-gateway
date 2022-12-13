package com.runjian.timer.vo.request;

import com.runjian.timer.vo.JobData;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 添加数据库任务请求体
 * @author Miracle
 * @date 2022/4/24 9:15
 */
@Data
public class AddDatabaseReq extends JobData  {

    /**
     * 数据库用户名
     */
    @NotBlank(message = "数据库用户名不能为空")
    @Size(max = 50, min = 1, message = "非法的数据库用户名")
    private String username;

    /**
     * 数据库密码
     */
    @NotBlank(message = "数据库密码不能为空")
    @Size(max = 100, min = 1, message = "非法的数据库密码")
    private String password;

    /**
     * 数据库连接URL ：127.0.0.1/{databaseName}
     */
    @NotBlank(message = "数据库连接URL不能为空")
    @Size(max = 600, min = 1, message = "非法的数据库连接URL")
    private String url;

    /**
     * SQL语句
     */
    @NotBlank(message = "数据库操作语句不能为空")
    @Size(max = 600, min = 1, message = "非法的数据库操作语句")
    private String sqlString;
}
