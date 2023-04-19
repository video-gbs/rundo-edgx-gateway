package com.runjian.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate 配置
 *
 * @author ouyangzhaoxing
 */
@Configuration
public class RestTemplateConfig {

    /**
     * 让RestTemplate 拥有 ribbon客户端负载均衡能力
     *  3s的连接
     * @return restTemplate对象
     */
    @Bean
    public RestTemplate restTemplate() {

        SimpleClientHttpRequestFactory clientHttpRequestFactory
                = new SimpleClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectTimeout(3 * 1000);
        clientHttpRequestFactory.setReadTimeout(3 * 1000);
        return new RestTemplate(clientHttpRequestFactory);
    }

}
