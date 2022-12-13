package com.runjian.common.config;

import io.github.yedaxia.apidocs.Docs;
import io.github.yedaxia.apidocs.DocsConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/**
 * @author Miracle
 * @date 2022/7/11 16:10
 */
//@Configuration
//@ConditionalOnProperty(name = "JApiDocs.enable", havingValue = "true")
public class JApiDocsConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${JApiDocs.docs.path}")
    private String docsPath;

    @Value("${JApiDocs.docs.version}")
    private String docsVersion;

    @Value("${JApiDocs.project.path}")
    private String projectPath;

    @Bean
    public void createRestfulApi() {
        DocsConfig config = new DocsConfig();
        // 项目根目录
        config.setProjectPath(projectPath);
        // 项目名称
        config.setProjectName(applicationName);
        // 声明该API的版本
        config.setApiVersion(docsVersion);
        // 生成API 文档所在目录
        config.setDocsPath(docsPath +"\\" + applicationName + "\\" );
        // 配置自动生成
        config.setAutoGenerate(Boolean.TRUE);
        // 执行生成文档
        Docs.buildHtmlDocs(config);
    }


}