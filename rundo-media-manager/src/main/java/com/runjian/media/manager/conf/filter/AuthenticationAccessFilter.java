package com.runjian.media.manager.conf.filter;//package com.runjian.media.manager.conf.filter;
//
//import com.runjian.common.config.exception.BusinessErrorEnums;
//import com.runjian.common.config.exception.BusinessException;
//import com.runjian.common.constant.MarkConstant;
//import com.runjian.common.utils.authenticationForeign.MidAuthTool;
//import com.runjian.media.manager.zlm.dto.PlatformAccountRsp;
//import com.runjian.media.manager.zlm.service.ProjectManagementService;
//import io.micrometer.core.instrument.util.StringUtils;
//import lombok.SneakyThrows;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.web.servlet.HandlerExceptionResolver;
//
//import javax.servlet.*;
//import javax.servlet.annotation.WebFilter;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.util.Enumeration;
//import java.util.HashMap;
//import java.util.Locale;
//import java.util.Map;
//
//
//@Slf4j
//@WebFilter(urlPatterns = { "/api/*" }, filterName = "authorFilter",asyncSupported=true)
//public class AuthenticationAccessFilter implements Filter {
//
//    @Autowired
//    ProjectManagementService projectManagementService;
//
//    @Autowired
//    @Qualifier("handlerExceptionResolver")
//    private HandlerExceptionResolver resolver;
//
//
//    @SneakyThrows
//    @Override
//    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
//                                   FilterChain filterChain) throws IOException, ServletException {
//        HttpServletRequest req = (HttpServletRequest) servletRequest;
//        HttpServletResponse resp = (HttpServletResponse)servletResponse;
//        HeaderMapRequestWrapper requestWrapper = new HeaderMapRequestWrapper(req);
//        String platformid = "";
//        try{
////获取鉴权的参数
//            String authorization = req.getHeader(MarkConstant.FOREIGN_AUTHORIZATION);
//            if(authorization == null){
//                log.warn("|MSG={}|INFO={}", "鉴权-签名参数缺失", authorization);
////            throw resolver.resolveException(req,resp,null,new BuilderException());
//                throw  new BusinessException(BusinessErrorEnums.VALID_BIND_EXCEPTION_ERROR);
//
//            }
//            Map<String, String> httpHeaderParms = MidAuthTool.getClientAuthorizationParam(authorization);
//
//            // 判断鉴权头参数变量数
//            if (httpHeaderParms.size() != 3) {
//                log.warn("|MSG={}|INFO={}", "鉴权-签名参数有误", httpHeaderParms);
//                throw  new BusinessException(BusinessErrorEnums.VALID_BIND_EXCEPTION_ERROR);
//
//            }
//            // 获取 platformid 先判断是否存在，存在才执行下一步
//            platformid = httpHeaderParms.get("platformid");
//            if (StringUtils.isBlank(platformid)) {
//                log.warn("|MSG={}|INFO={}", "鉴权-签名参数[platformid]有误", httpHeaderParms);
//                throw  new BusinessException(BusinessErrorEnums.VALID_BIND_EXCEPTION_ERROR);
//            }
//            //进行校验 获取redis/或则数据库中的数据获取并写入到缓存中24小时
//            PlatformAccountRsp onePlatformAccount = projectManagementService.getOnePlatformAccount(platformid);
//            if(onePlatformAccount == null){
//                log.warn("|MSG={}|INFO={}", "鉴权-用户信息不存在", httpHeaderParms);
//                throw  new BusinessException(BusinessErrorEnums.VALID_BIND_EXCEPTION_ERROR);
//
//            }
//            if(onePlatformAccount.getEnable() == 0){
//                log.warn("|MSG={}|INFO={}", "鉴权-该用户已被禁用", httpHeaderParms);
//                throw  new BusinessException(BusinessErrorEnums.ACCOUNT_NOT_ENABLED);
//            }
//            String secret = onePlatformAccount.getSecretKey();
//            // 时间戳 , 格式：从 1970 1 1 到当前时间的秒数 精确到秒，与标准时间偏差 5 分钟之内
//            String timestamp = httpHeaderParms.get("timestamp");
//            if (org.apache.commons.lang3.StringUtils.isBlank(timestamp)) {
//                log.warn("|MSG={}|INFO={}", "鉴权-签名参数[timestamp]有误", httpHeaderParms);
//                throw  new BusinessException(BusinessErrorEnums.VALID_BIND_EXCEPTION_ERROR);
//
//            }
//            int minutes = (int) ((System.currentTimeMillis() / 1000) - Integer.parseInt(timestamp)) / 60;
//            if (Math.abs(minutes) > 5) {
//                log.warn("|MSG={}|INFO={}", "鉴权-签名参数[timestamp] 超过5分钟", httpHeaderParms);
//                throw  new BusinessException(BusinessErrorEnums.REQUEST_EXPIRED);
//            }
//            // 签名串
//            String clientSignature = httpHeaderParms.get("signature");
//            if (org.apache.commons.lang3.StringUtils.isBlank(clientSignature)) {
//                log.warn("|MSG={}|INFO={}", "鉴权-签名参数[signature]有误", httpHeaderParms);
//                throw  new BusinessException(BusinessErrorEnums.VALID_BIND_EXCEPTION_ERROR);
//            }
//            // 识别http请求还是https请求,如果是https请求，就不需要校验http-body的参数签名
//            String serverSignature = MidAuthTool.httpsSignature(timestamp, secret);
//            Locale.setDefault(Locale.ENGLISH);
//            if (!clientSignature.equals(serverSignature)) {
//                log.warn("|MSG={}|INFO={}|CINFO={}|SINFO={}", "签名参数[signature]参数不一致", httpHeaderParms, clientSignature, serverSignature);
//                throw  new BusinessException(BusinessErrorEnums.AUTHORIZED_FAILED);
//            }
//        }catch (Exception e){
//            resolver.resolveException(req, resp, null, e);
//            return;
//        }
//
//
//        requestWrapper.addHeader("platformid",platformid);
//        log.info("header-->{}",getHeadKeyAndValue(req));
//        filterChain.doFilter(requestWrapper, servletResponse);
//
//    }
//
//    private Map<String, String> getHeadKeyAndValue(HttpServletRequest httpRequest) {
//        Map<String, String> header = new HashMap<>();
//        Enumeration<String> headerNames = httpRequest.getHeaderNames();
//        while (headerNames.hasMoreElements()) {
//            String nextElement = headerNames.nextElement();
//            header.put(nextElement, httpRequest.getHeader(nextElement));
//        }
//        return header;
//    }
//}
