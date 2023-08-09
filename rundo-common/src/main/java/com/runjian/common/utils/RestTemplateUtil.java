package com.runjian.common.utils;

import com.runjian.common.commonDto.SsrcInfo;
import com.runjian.common.commonDto.StreamInfo;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.CommonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * @author
 */
@Slf4j
public class RestTemplateUtil {


    public static CommonResponse<SsrcInfo> postReturnCommonrespons(String url, Object obj, RestTemplate restTemplate) {
        if (StringUtils.isEmpty(url) || ObjectUtils.isEmpty(obj)) {
            return CommonResponse.failure(BusinessErrorEnums.VALID_BIND_EXCEPTION_ERROR);
        }
        long startTime = System.currentTimeMillis();

        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);

            ParameterizedTypeReference<CommonResponse<SsrcInfo>> typeRef = new ParameterizedTypeReference<CommonResponse<SsrcInfo>>() {};

            HttpEntity httpEntity = new HttpEntity(obj, httpHeaders);
            ResponseEntity<CommonResponse<SsrcInfo>> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, typeRef);
            long endTime = System.currentTimeMillis();
            log.info("post-json, 请求地址={}, 耗时={} ms, 参数={}, 响应信息={}", url,
                    (endTime - startTime), obj.toString(), responseEntity.getBody());
            if (responseEntity.getStatusCodeValue() == HttpStatus.OK.value()) {
                return  responseEntity.getBody();
            }
        } catch (Exception e) {
            log.error("post-json error, 请求地址={}, 耗时={} ms, 参数={}, 失败信息={}", url,
                    (System.currentTimeMillis() - startTime), obj.toString(), e.getMessage());
        }
        return CommonResponse.failure(BusinessErrorEnums.UNKNOWN_ERROR);
    }

    public static CommonResponse<StreamInfo> postRtpinforespons(String url, Object obj, RestTemplate restTemplate) {
        if (StringUtils.isEmpty(url) || ObjectUtils.isEmpty(obj)) {
            return CommonResponse.failure(BusinessErrorEnums.VALID_BIND_EXCEPTION_ERROR);
        }
        long startTime = System.currentTimeMillis();

        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);

            ParameterizedTypeReference<CommonResponse<StreamInfo>> typeRef = new ParameterizedTypeReference<CommonResponse<StreamInfo>>() {};

            HttpEntity httpEntity = new HttpEntity(obj, httpHeaders);
            ResponseEntity<CommonResponse<StreamInfo>> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, typeRef);
            long endTime = System.currentTimeMillis();
            log.info("post-json, 请求地址={}, 耗时={} ms, 参数={}, 响应信息={}", url,
                    (endTime - startTime), obj.toString(), responseEntity.getBody());
            if (responseEntity.getStatusCodeValue() == HttpStatus.OK.value()) {
                return  responseEntity.getBody();
            }
        } catch (Exception e) {
            log.error("post-json error, 请求地址={}, 耗时={} ms, 参数={}, 失败信息={}", url,
                    (System.currentTimeMillis() - startTime), obj.toString(), e.getMessage());
        }
        return CommonResponse.failure(BusinessErrorEnums.UNKNOWN_ERROR);
    }

    public static CommonResponse<Boolean> postCloseRtpserverRespons(String url, Object obj, RestTemplate restTemplate) {
        if (StringUtils.isEmpty(url) || ObjectUtils.isEmpty(obj)) {
            return CommonResponse.failure(BusinessErrorEnums.VALID_BIND_EXCEPTION_ERROR);
        }
        long startTime = System.currentTimeMillis();

        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);

            ParameterizedTypeReference<CommonResponse<Boolean>> typeRef = new ParameterizedTypeReference<CommonResponse<Boolean>>() {};

            HttpEntity httpEntity = new HttpEntity(obj, httpHeaders);
            ResponseEntity<CommonResponse<Boolean>> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, typeRef);
            long endTime = System.currentTimeMillis();
            log.info("post-json, 请求地址={}, 耗时={} ms, 参数={}, 响应信息={}", url,
                    (endTime - startTime), obj.toString(), responseEntity.getBody());
            if (responseEntity.getStatusCodeValue() == HttpStatus.OK.value()) {
                return  responseEntity.getBody();
            }
        } catch (Exception e) {
            log.error("post-json error, 请求地址={}, 耗时={} ms, 参数={}, 失败信息={}", url,
                    (System.currentTimeMillis() - startTime), obj.toString(), e.getMessage());
        }
        return CommonResponse.failure(BusinessErrorEnums.UNKNOWN_ERROR);
    }

    public static CommonResponse<Boolean> postStreamNotifyRespons(String url, Object obj, RestTemplate restTemplate) {
        if (StringUtils.isEmpty(url) || ObjectUtils.isEmpty(obj)) {
            return CommonResponse.failure(BusinessErrorEnums.VALID_BIND_EXCEPTION_ERROR);
        }
        long startTime = System.currentTimeMillis();

        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);

            ParameterizedTypeReference<CommonResponse<Boolean>> typeRef = new ParameterizedTypeReference<CommonResponse<Boolean>>() {};

            HttpEntity httpEntity = new HttpEntity(obj, httpHeaders);
            ResponseEntity<CommonResponse<Boolean>> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, typeRef);
            long endTime = System.currentTimeMillis();
            log.info("post-json, 请求地址={}, 耗时={} ms, 参数={}, 响应信息={}", url,
                    (endTime - startTime), obj.toString(), responseEntity.getBody());
            if (responseEntity.getStatusCodeValue() == HttpStatus.OK.value()) {
                return  responseEntity.getBody();
            }
        } catch (Exception e) {
            log.error("post-json error, 请求地址={}, 耗时={} ms, 参数={}, 失败信息={}", url,
                    (System.currentTimeMillis() - startTime), obj.toString(), e.getMessage());
        }
        return CommonResponse.failure(BusinessErrorEnums.UNKNOWN_ERROR);
    }

    /**
     * post请求(json格式)
     *
     * @param url          请求地址
     * @param map          请求参数
     * @param restTemplate
     * @return
     */
    public static String post(String url, Map<String, Object> map, RestTemplate restTemplate) {
        if (StringUtils.isEmpty(url) || CollectionUtils.isEmpty(map)) {
            return null;
        }
        long startTime = System.currentTimeMillis();

        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<HashMap<String, String>> httpEntity = new HttpEntity(map, httpHeaders);
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, httpEntity, String.class);
            long endTime = System.currentTimeMillis();
            log.info("post-json, 请求地址={}, 耗时={} ms, 参数={}, 响应信息={}", url,
                    (endTime - startTime), map.toString(), responseEntity.getBody());
            if (responseEntity.getStatusCodeValue() == HttpStatus.OK.value()) {
                return responseEntity.getBody();
            }
        } catch (Exception e) {
            log.error("post-json error, 请求地址={}, 耗时={} ms, 参数={}, 失败信息={}", url,
                    (System.currentTimeMillis() - startTime), map.toString(), e.getMessage());
        }
        return null;
    }

    /**
     * post请求(json格式)带请求头
     *
     * @param url          请求地址
     * @param map          参数
     * @param headers      请求头参数
     * @param restTemplate
     * @return
     */
    public static String post(String url, Map<String, Object> map, Map<String, String> headers, RestTemplate restTemplate) {
        if (StringUtils.isEmpty(url) || CollectionUtils.isEmpty(map)) {
            return null;
        }
        long startTime = System.currentTimeMillis();

        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            if (!CollectionUtils.isEmpty(headers)) {
                httpHeaders.setAll(headers);
            }
            HttpEntity<HashMap<String, String>> httpEntity = new HttpEntity(map, httpHeaders);
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, httpEntity, String.class);
            long endTime = System.currentTimeMillis();
            log.info("post-json-header, 请求地址={}, 耗时={} ms, 参数={}, 响应信息={}", url,
                    (endTime - startTime), map.toString(), responseEntity.getBody());
            if (responseEntity.getStatusCodeValue() == HttpStatus.OK.value()) {
                return responseEntity.getBody();
            }
        } catch (Exception e) {
            log.error("post-json-header error, 请求地址={}, 耗时={} ms, 参数={}, 失败信息={}", url,
                    (System.currentTimeMillis() - startTime), map.toString(), e.getMessage());
        }
        return null;
    }

    /**
     * post请求带请求头
     *
     * @param url          请求地址
     * @param body         参数
     * @param headers      请求头参数
     * @param restTemplate
     * @return
     */
    public static String postString(String url, String body, Map<String, String> headers, RestTemplate restTemplate) {
        if (StringUtils.isEmpty(url) || StringUtils.isEmpty(body)) {
            return null;
        }
        long startTime = System.currentTimeMillis();

        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            if (!CollectionUtils.isEmpty(headers)) {
                httpHeaders.setAll(headers);
            }
            HttpEntity<String> httpEntity = new HttpEntity<>(body, httpHeaders);
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, httpEntity, String.class);
            long endTime = System.currentTimeMillis();
            log.info("post-string, 请求地址={}, 耗时={} ms, 参数={}, 响应信息={}", url,
                    (endTime - startTime), body, responseEntity.getBody());
            if (responseEntity.getStatusCodeValue() == HttpStatus.OK.value()) {
                return responseEntity.getBody();
            }
        } catch (Exception e) {
            log.error("post-string error, 请求地址={}, 耗时={} ms, 参数={}, 失败信息={}", url,
                    (System.currentTimeMillis() - startTime), body, e.getMessage());
        }
        return null;
    }


    /**
     * post请求(application/x-www-form-urlencoded格式)
     *
     * @param url           请求地址
     * @param multiValueMap 请求参数
     * @param restTemplate
     * @return
     */
    public static String post(String url, MultiValueMap<String, Object> multiValueMap, RestTemplate restTemplate) {
        if (StringUtils.isEmpty(url) || CollectionUtils.isEmpty(multiValueMap)) {
            return null;
        }
        long startTime = System.currentTimeMillis();

        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(multiValueMap, httpHeaders);
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, httpEntity, String.class);
            long endTime = System.currentTimeMillis();
            log.info("post-urlencoded, 请求地址={}, 耗时={} ms, 参数={}, 响应信息={}", url,
                    (endTime - startTime), multiValueMap.toString(), responseEntity.getBody());
            if (responseEntity.getStatusCodeValue() == HttpStatus.OK.value()) {
                return responseEntity.getBody();
            }
        } catch (Exception e) {
            log.error("post-urlencoded error, 请求地址={}, 耗时={} ms, 参数={}, 失败信息={}", url,
                    (System.currentTimeMillis() - startTime), multiValueMap.toString(), e.getMessage());
        }
        return null;
    }

    /**
     * post请求(application/x-www-form-urlencoded格式)
     *
     * @param url           请求地址
     * @param multiValueMap 请求参数
     * @param restTemplate
     * @return
     */
    public static String post(String url, MultiValueMap<String, Object> multiValueMap,Map<String, String> headers, RestTemplate restTemplate) {
        if (StringUtils.isEmpty(url) || CollectionUtils.isEmpty(multiValueMap)) {
            return null;
        }
        long startTime = System.currentTimeMillis();

        try {

            HttpHeaders httpHeaders = new HttpHeaders();
            if (!CollectionUtils.isEmpty(headers)) {
                httpHeaders.setAll(headers);
            }
            httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(multiValueMap, httpHeaders);
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, httpEntity, String.class);
            long endTime = System.currentTimeMillis();
            log.info("post-urlencoded, 请求地址={}, 耗时={} ms, 参数={}, 响应信息={}", url,
                    (endTime - startTime), multiValueMap.toString(), responseEntity.getBody());
            if (responseEntity.getStatusCodeValue() == HttpStatus.OK.value()) {
                return responseEntity.getBody();
            }
        } catch (Exception e) {
            log.error("post-urlencoded error, 请求地址={}, 耗时={} ms, 参数={}, 失败信息={}", url,
                    (System.currentTimeMillis() - startTime), multiValueMap.toString(), e.getMessage());
        }
        return null;
    }

    /**
     * 请求xml数据  post请求
     *
     * @param url          请求url
     * @param xml          xml格式字符串
     * @param restTemplate RestTemplate实例
     * @return
     */
    public static String postXml(String url, String xml, RestTemplate restTemplate) {
        if (StringUtils.isEmpty(url) || StringUtils.isEmpty(xml)) {
            return null;
        }
        String xmlLog = xml.replaceAll("(\r\n|\n)", "");
        long startTime = System.currentTimeMillis();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);
            HttpEntity<String> request = new HttpEntity<>(xml, headers);
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, request, String.class);
            long endTime = System.currentTimeMillis();
            log.info("post-xml, 请求地址={}, 耗时={} ms, 参数={}, 响应信息={}", url,
                    (endTime - startTime), xmlLog, responseEntity.getBody());
            if (responseEntity.getStatusCodeValue() == HttpStatus.OK.value()) {
                return responseEntity.getBody();
            }
        } catch (Exception e) {
            log.error("post-xml error, 请求地址={}, 耗时={} ms, 参数={}, 失败信息={}", url,
                    (System.currentTimeMillis() - startTime), xmlLog, e.getMessage());
        }
        return null;
    }

    /**
     * 请求xml数据(带请求头)
     *
     * @param url          请求地址
     * @param xml          参数
     * @param headers      请求头信息
     * @param restTemplate
     * @return
     */
    public static String postXml(String url, String xml, Map<String, String> headers, RestTemplate restTemplate) {
        if (StringUtils.isEmpty(url) || StringUtils.isEmpty(xml)) {
            return null;
        }
        String xmlLog = xml.replaceAll("(\r\n|\n)", "");
        long startTime = System.currentTimeMillis();

        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_XML);
            if (!CollectionUtils.isEmpty(headers)) {
                httpHeaders.setAll(headers);
            }
            HttpEntity<String> request = new HttpEntity<>(xml, httpHeaders);
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, request, String.class);
            long endTime = System.currentTimeMillis();
            log.info("post-xml-header, 请求地址={}, 耗时={} ms, 参数={}, 响应信息={}", url,
                    (endTime - startTime), xmlLog, responseEntity.getBody());
            if (responseEntity.getStatusCodeValue() == HttpStatus.OK.value()) {
                return responseEntity.getBody();
            }
        } catch (Exception e) {
            log.error("post-xml-header error, 请求地址={}, 耗时={} ms, 参数={}, 失败信息={}", url,
                    (System.currentTimeMillis() - startTime), xmlLog, e.getMessage());
        }
        return null;
    }

    /**
     * post请求(multipart/form-data格式)
     *
     * @param url           请求地址
     * @param multiValueMap 请求参数
     * @param restTemplate
     * @return
     */
    public static String postFile(String url, MultiValueMap<String, Object> multiValueMap, Map<String, String> headers, RestTemplate restTemplate) {
        if (StringUtils.isEmpty(url) || CollectionUtils.isEmpty(multiValueMap)) {
            return null;
        }
        long startTime = System.currentTimeMillis();

        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
            if (!CollectionUtils.isEmpty(headers)) {
                httpHeaders.setAll(headers);
            }
            HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(multiValueMap, httpHeaders);
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, httpEntity, String.class);
            long endTime = System.currentTimeMillis();
            log.info("post-urlencoded, 请求地址={}, 耗时={} ms, 参数={}, 响应信息={}", url,
                    (endTime - startTime), multiValueMap.toString(), responseEntity.getBody());
            if (responseEntity.getStatusCodeValue() == HttpStatus.OK.value()) {
                return responseEntity.getBody();
            }
        } catch (Exception e) {
            log.error("post-urlencoded error, 请求地址={}, 耗时={} ms, 参数={}, 失败信息={}", url,
                    (System.currentTimeMillis() - startTime), multiValueMap.toString(), e.getMessage());
        }
        return null;
    }

    /**
     * get请求带请求头
     *
     * @param url          请求地址
     * @param headers      请求头参数
     * @param restTemplate
     * @return
     */
    public static String get(String url, Map<String, String> headers, RestTemplate restTemplate) {
        if (StringUtils.isEmpty(url)) {
            return null;
        }
        long startTime = System.currentTimeMillis();

        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            if (!CollectionUtils.isEmpty(headers)) {
                httpHeaders.setAll(headers);
            }
            HttpEntity<Object> httpEntity = new HttpEntity<>(httpHeaders);
            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
            long endTime = System.currentTimeMillis();
            log.info("post-string, 请求地址={}, 耗时={} ms, 响应信息={}", url,
                    (endTime - startTime), responseEntity.getBody());
            if (responseEntity.getStatusCodeValue() == HttpStatus.OK.value()) {
                return responseEntity.getBody();
            }
        } catch (Exception e) {
            log.error("post-string error, 请求地址={}, 耗时={} ms, 失败信息={}", url,
                    (System.currentTimeMillis() - startTime), e.getMessage());
        }
        return null;
    }

    /**
     * get请求带请求头
     *
     * @param url          请求地址
     * @param headers      请求头参数
     * @param restTemplate
     * @return
     */
    public static String getWithParams(String url, Map<String, String> headers,Map<String, Object> reqParam, RestTemplate restTemplate) {
        if (ObjectUtils.isEmpty(url)) {
            return null;
        }
        long startTime = System.currentTimeMillis();

        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            if (!CollectionUtils.isEmpty(headers)) {
                httpHeaders.setAll(headers);
            }
            HttpEntity<Object> httpEntity = new HttpEntity<>(httpHeaders);
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
            //如果存在參數
            if(!reqParam.isEmpty()){
                for (Map.Entry<String,Object> e: reqParam.entrySet()) {
                    //构建查询参数
                    builder.queryParam(e.getKey(),e.getValue());
                }
                //拼接好参数后的URl//test.com/url?param1={param1}&param2={param2};
                url=builder.build().toString();
            }

            ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, String.class);
            long endTime = System.currentTimeMillis();
            log.info("post-string, 请求地址={}, 耗时={} ms, 响应信息={}", url,
                    (endTime - startTime), responseEntity.getBody());
            if (responseEntity.getStatusCodeValue() == HttpStatus.OK.value()) {
                return responseEntity.getBody();
            }
        } catch (Exception e) {
            log.error("post-string error, 请求地址={}, 耗时={} ms, 失败信息={}", url,
                    (System.currentTimeMillis() - startTime), e.getMessage());
        }
        return null;
    }

}
