package com.runjian.common.utils.authenticationForeign;

import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 中间号的鉴权方式
 *
 * @author huangtongkui
 */
public class MidAuthTool {

    /**
     * http 签名密钥
     *
     * @param timestamp
     * @param secret
     * @param httpBody
     * @return
     */
    public static String httpSignature(String timestamp, String secret, String httpBody) {
        String signature = Md5Util.toMD5(timestamp + secret + httpBody).toUpperCase();
        return signature;
    }

    /**
     * https 签名密钥
     *
     * @param timestamp
     * @param secret
     * @return
     */
    public static String httpsSignature(String timestamp, String secret) {
        return httpSignature(timestamp, secret, "");
    }


    /**
     * 获取客户端请求头参数变量
     * 支持：
     * EOPAUTH platformid=ems0000000000001,timestamp=1592312902,signature=D3022ECBD3F877F856C875D90FF91429
     * EOPAUTH platformid="ems0000000000001",timestamp="1592312902",signature="D3022ECBD3F877F856C875D90FF91429"
     *
     * @param content 请求头参数值
     * @return
     */
    public static Map<String, String> getClientAuthorizationParam(String content) {
        content = getCommonAuth(content);
        Map<String, String> params = new HashMap<>(8);
        String eopauth = content.substring(0, 7);
        if (!"EOPAUTH".equals(eopauth)) {
            return params;
        }
        String lastContent = content.substring(8);
        String[] lastContentArr = StringUtils.delimitedListToStringArray(lastContent, ",");
        if (lastContentArr.length != 3) {
            return params;
        }
        String mapPlatformidKey = lastContentArr[0].substring(0, 10);
        String mapPlatformidVal = lastContentArr[0].substring(11);
        String mapTimestampKey = lastContentArr[1].substring(0, 9);
        String mapTimestampVal = lastContentArr[1].substring(10);
        String mapSignatureKey = lastContentArr[2].substring(0, 9);
        String mapSignatureVal = lastContentArr[2].substring(10);
        if (!"platformid".equals(mapPlatformidKey) || !"timestamp".equals(mapTimestampKey) || !"signature".equals(mapSignatureKey)) {
            return params;
        }
        params.put(mapPlatformidKey, mapPlatformidVal);
        params.put(mapTimestampKey, mapTimestampVal);
        params.put(mapSignatureKey, mapSignatureVal);
        return params;
    }


    /**
     * 获取到通用的鉴权信息
     *
     * @param content 模板内容 EOPAUTH platformid="ems0000000000001",timestamp="1592312902",signature="D3022ECBD3F877F856C875D90FF91429"
     * @return 返回结果 EOPAUTH platformid=ems0000000000001,timestamp=1592312902,signature=D3022ECBD3F877F856C875D90FF91429
     */
    public static String getCommonAuth(String content) {
        if (content == null) {
            return null;
        }
        return content.replaceAll("\"", "");
    }

}
