package com.runjian.conf;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 配置文件 user-settings 映射的配置信息
 */
@Component
@Data
@ConfigurationProperties(prefix = "user-settings", ignoreInvalidFields = true)
public class UserSetting {

    private Boolean savePositionHistory = Boolean.FALSE;

    private Boolean autoApplyPlay = Boolean.FALSE;

    private Boolean seniorSdp = Boolean.FALSE;

    private Integer playTimeout = 18000;
    /**
     * 业务场景流程超时时间 单位秒
     */
    private Integer businessSceneTimeout = 5;

    private int platformPlayTimeout = 60000;

    private Boolean interfaceAuthentication = Boolean.TRUE;

    private Boolean recordPushLive = Boolean.TRUE;

    private Boolean recordSip = Boolean.TRUE;

    private Boolean logInDatebase = Boolean.TRUE;

    private Boolean usePushingAsStatus = Boolean.TRUE;

    private Boolean streamOnDemand = Boolean.TRUE;

    private String serverId = "000000";

    private String thirdPartyGBIdReg = "[\\s\\S]*";

    private List<String> interfaceAuthenticationExcludes = new ArrayList<>();

    /**
     * 设备录像下载最大时间周期
     */
    private int deviceDownloadTimeCycle = 1800;

    private String civilCodeFile = "classpath:civilCode.csv";

}
