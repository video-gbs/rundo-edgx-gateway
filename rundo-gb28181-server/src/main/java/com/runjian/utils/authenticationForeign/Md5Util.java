package com.runjian.utils.authenticationForeign;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5工具类
 *
 * @author
 */
public class Md5Util {

    /**
     * 字符串加密为MD5
     *
     * @param plainText
     * @return
     */
    public static String toMD5(String plainText) {

        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 error:", e);
        }
        messageDigest.update(plainText.getBytes());
        byte[] by = messageDigest.digest();

        StringBuffer buf = new StringBuffer();
        int val;
        for (int i = 0; i < by.length; i++) {
            val = by[i];
            if (val < 0) {
                val += 256;
            } else if (val < 16) {
                buf.append("0");
            }
            buf.append(Integer.toHexString(val));
        }
        return buf.toString();
    }

    /**
     * 取MD5加密串16位码
     *
     * @param plainText
     * @return
     */
    public static String Md5Lenth16(String plainText) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 error:", e);
        }
        md.update(plainText.getBytes());
        byte[] b = md.digest();
        int i;
        StringBuffer buf = new StringBuffer("");
        for (int offset = 0; offset < b.length; offset++) {
            i = b[offset];
            if (i < 0) {
                i += 256;
            }
            if (i < 16) {
                buf.append("0");
            }
            buf.append(Integer.toHexString(i));
        }
        return buf.toString().substring(8, 24);
    }
}
