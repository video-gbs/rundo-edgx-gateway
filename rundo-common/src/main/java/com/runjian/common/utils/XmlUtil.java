package com.runjian.common.utils;


import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.StringReader;
import java.util.Iterator;


/**
 * @author
 */
public class XmlUtil {
    // 转换不带CDDATA的XML
    private static SAXReader sax;
    static {
        // 实例化XStream基本对象
        sax = new SAXReader();
        sax.setEncoding("UTF-8");
    }

    public static synchronized Element getRootElement(String xmlStr) throws DocumentException, SAXException {

        StringReader stringReader = new StringReader(xmlStr);

        InputSource inputSource = new InputSource(stringReader);

        sax.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);  // 禁止加载外部 DTD

        Document document = sax.read(inputSource);


//        Document document = sax.read(xmlStr);
        // 获取根元素

        return document.getRootElement();
    }

}