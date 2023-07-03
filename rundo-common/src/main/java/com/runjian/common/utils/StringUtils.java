package com.runjian.common.utils;

import com.runjian.common.constant.LogTemplate;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Miracle
 * @date 2022/7/21 17:47
 */
@Slf4j
public class StringUtils {

    /**
     * 获取指定字符串出现的次数
     * @param srcText 源字符串
     * @param findText 要查找的字符串
     * @return
     */
    public static int appearNumber(String srcText, String findText) {
        int count = 0;
        Pattern p = Pattern.compile(findText);
        Matcher m = p.matcher(srcText);
        while (m.find()) {
            count++;
        }
        return count;
    }

    public static String getUTF8StringFromGBKString(String gbkStr) {
         try {
                 return new String(getUTF8BytesFromGBKString(gbkStr), "UTF-8");
             } catch (UnsupportedEncodingException e) {
                 throw new InternalError();
         }
     }
    public static String getUTF8StringFromByte(byte[] strByte) {
        return new String(strByte, StandardCharsets.UTF_8);
    }

    public static String getGbkStringFromByte(byte[] strByte) {

        try {
            return new String(strByte, "GB2312").trim();
        } catch (UnsupportedEncodingException e) {
            throw new InternalError();
        }
    }


    public static String getUtf8StringFromByte(byte[] strByte) {

        try {
            return new String(strByte, "UTF-8").trim();
        } catch (UnsupportedEncodingException e) {
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "编码转换失败", "编码转换utf8失败", e.getMessage());
        }
        return "";
    }

      public static byte[] getUTF8BytesFromGBKString(String gbkStr) {
         int n = gbkStr.length();
         byte[] utfBytes = new byte[3 * n];
         int k = 0;
         for (int i = 0; i < n; i++) {
                 int m = gbkStr.charAt(i);
                 if (m < 128 && m >= 0) {
                         utfBytes[k++] = (byte) m;
                         continue;
                     }
                 utfBytes[k++] = (byte) (0xe0 | (m >> 12));
                 utfBytes[k++] = (byte) (0x80 | ((m >> 6) & 0x3f));
                 utfBytes[k++] = (byte) (0x80 | (m & 0x3f));
             }
         if (k < utfBytes.length) {
                 byte[] tmp = new byte[k];
                 System.arraycopy(utfBytes, 0, tmp, 0, k);
                 return tmp;
             }
         return utfBytes;
     }

    /** 7位ASCII字符，也叫作ISO646-US、Unicode字符集的基本拉丁块 */

    public static final String US_ASCII = "US-ASCII";

    /** ISO 拉丁字母表 No.1，也叫作 ISO-LATIN-1 */

    public static final String ISO_8859_1 = "ISO-8859-1";

    /** 8 位 UCS 转换格式 */

    public static final String UTF_8 = "UTF-8";

    /** 16 位 UCS 转换格式，Big Endian（最低地址存放高位字节）字节顺序 */

    public static final String UTF_16BE = "UTF-16BE";

    /** 16 位 UCS 转换格式，Little-endian（最高地址存放低位字节）字节顺序 */

    public static final String UTF_16LE = "UTF-16LE";

    /** 16 位 UCS 转换格式，字节顺序由可选的字节顺序标记来标识 */

    public static final String UTF_16 = "UTF-16";

    /** 中文超大字符集 */

    public static final String GBK = "GBK";

    /**

     * 将字符编码转换成US-ASCII码

     */

    public static String toASCII(String str) throws UnsupportedEncodingException{

        return changeCharset(str, US_ASCII);

    }

    /**

     * 将字符编码转换成ISO-8859-1码

     */

    public static String toISO_8859_1(String str) throws UnsupportedEncodingException{

        return changeCharset(str, ISO_8859_1);

    }

    /**

     * 将字符编码转换成UTF-8码

     */

    public static String toUTF_8(String str) throws UnsupportedEncodingException{

        return changeCharset(str, UTF_8);

    }

    /**

     * 将字符编码转换成UTF-16BE码

     */

    public static String toUTF_16BE(String str) throws UnsupportedEncodingException{

        return changeCharset(str, UTF_16BE);

    }

    /**

     * 将字符编码转换成UTF-16LE码

     */

    public static String toUTF_16LE(String str) throws UnsupportedEncodingException{

        return changeCharset(str, UTF_16LE);

    }

    /**

     * 将字符编码转换成UTF-16码

     */

    public static String toUTF_16(String str) throws UnsupportedEncodingException{

        return changeCharset(str, UTF_16);

    }

    /**

     * 将字符编码转换成GBK码

     */

    public static String toGBK(String str) throws UnsupportedEncodingException{

        return changeCharset(str, GBK);

    }

    /**

     * 字符串编码转换的实现方法

     * @param str  待转换编码的字符串

     * @param newCharset 目标编码

     * @return

     * @throws UnsupportedEncodingException

     */

    public static String changeCharset(String str, String newCharset)

            throws UnsupportedEncodingException {

        if (str != null) {

            //用默认字符编码解码字符串。

            byte[] bs = str.getBytes();

            //用新的字符编码生成字符串

            return new String(bs, newCharset);

        }

        return null;

    }

    /**

     * 字符串编码转换的实现方法

     * @param str  待转换编码的字符串

     * @param oldCharset 原编码

     * @param newCharset 目标编码

     * @return

     * @throws UnsupportedEncodingException

     */

    public String changeCharset(String str, String oldCharset, String newCharset)

            throws UnsupportedEncodingException {

        if (str != null) {

            //用旧的字符编码解码字符串。解码可能会出现异常。

            byte[] bs = str.getBytes(oldCharset);

            //用新的字符编码生成字符串

            return new String(bs, newCharset);

        }

        return null;

    }



}
