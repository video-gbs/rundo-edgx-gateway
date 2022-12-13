package com.runjian.timer.handle;

import com.mysql.cj.jdbc.Driver;
import com.runjian.common.constant.LogTemplate;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.DataSourceConnectionFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * @author Miracle
 * @date 2022/4/24 14:23
 */
@Slf4j
@Data
public class DatabaseHandle extends QuartzJobBean {

    /**
     * 数据库账号
     */
    private String username;

    /**
     * 数据库库密码
     */
    private String password;

    /**
     * 连接地址
     */
    private String url;

    /**
     * SQL语句
     */
    private String sqlString;

    @Override
    @Transactional(rollbackFor = Exception.class)
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "定时任务", "执行DATABASE处理中" , this);
        JobKey jobKey = context.getJobDetail().getKey();
        DataSourceConnectionFactory connectionFactory;
        try (BasicDataSource dataSource = new BasicDataSource()){
            dataSource.setUsername(this.username);
            dataSource.setPassword(this.password);
            dataSource.setUrl("jdbc:mysql://" + this.url + "?useUnicode=yes&characterEncoding=UTF-8&useSSL=false&serverTimezone=GMT%2B8");
            dataSource.setDriver(new Driver());
            connectionFactory = new DataSourceConnectionFactory(dataSource);
        } catch (Exception e) {
            log.info(LogTemplate.ERROR_LOG_MSG_TEMPLATE, "定时任务", "执行DATABASE处理失败" , this, e.getMessage());
            JobExecutionException e2 = new JobExecutionException(e);
            // true 表示 Quartz 会自动取消所有与这个 job 有关的 trigger，从而避免再次运行 job
            // e2.setUnscheduleAllTriggers(true);
            e2.setUnscheduleFiringTrigger(true);
            throw e2;
        }
        try(Connection connection = connectionFactory.createConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sqlString);) {
            preparedStatement.execute();
        }catch (Exception e) {
            log.info(LogTemplate.ERROR_LOG_MSG_TEMPLATE, "定时任务", "执行DATABASE处理失败" , this, e.getMessage());
            JobExecutionException e2 = new JobExecutionException(e);
            // true 表示 Quartz 会自动取消所有与这个 job 有关的 trigger，从而避免再次运行 job
            // e2.setUnscheduleAllTriggers(true);
            throw e2;
        }
        log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "定时任务", "执行DATABASE处理成功" , this);
    }
}
