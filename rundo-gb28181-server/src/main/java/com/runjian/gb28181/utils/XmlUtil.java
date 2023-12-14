package com.runjian.gb28181.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.runjian.common.constant.LogTemplate;
import com.runjian.gb28181.bean.CivilCodePo;
import com.runjian.gb28181.bean.Device;
import com.runjian.gb28181.bean.DeviceChannel;
import com.runjian.gb28181.event.subscribe.catalog.CatalogEvent;
import com.runjian.runner.CivilCodeFileConfRunner;
import org.apache.commons.lang3.math.NumberUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

import javax.sip.RequestEvent;
import javax.sip.message.Request;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * 基于dom4j的工具包
 *
 *
 */
public class XmlUtil {
    /**
     * 日志服务
     */
    private static Logger logger = LoggerFactory.getLogger(XmlUtil.class);

    /**
     * 解析XML为Document对象
     *
     * @param xml 被解析的XMl
     *
     * @return Document
     */
    public static Element parseXml(String xml) {
        Document document = null;
        //
        StringReader sr = new StringReader(xml);
        SAXReader saxReader = new SAXReader();
        try {
            document = saxReader.read(sr);
        } catch (DocumentException e) {
            logger.error("解析失败", e);
        }
        return null == document ? null : document.getRootElement();
    }

    /**
     * 获取element对象的text的值
     *
     * @param em  节点的对象
     * @param tag 节点的tag
     * @return 节点
     */
    public static String getText(Element em, String tag) {
        if (null == em) {
            return null;
        }
        Element e = em.element(tag);
        //
        return null == e ? null : e.getText().trim();
    }

    /**
     * 递归解析xml节点，适用于 多节点数据
     *
     * @param node     node
     * @param nodeName nodeName
     * @return List<Map<String, Object>>
     */
    public static List<Map<String, Object>> listNodes(Element node, String nodeName) {
        if (null == node) {
            return null;
        }
        // 初始化返回
        List<Map<String, Object>> listMap = new ArrayList<Map<String, Object>>();
        // 首先获取当前节点的所有属性节点
        List<Attribute> list = node.attributes();

        Map<String, Object> map = null;
        // 遍历属性节点
        for (Attribute attribute : list) {
            if (nodeName.equals(node.getName())) {
                if (null == map) {
                    map = new HashMap<String, Object>();
                    listMap.add(map);
                }
                // 取到的节点属性放到map中
                map.put(attribute.getName(), attribute.getValue());
            }

        }
        // 遍历当前节点下的所有节点 ，nodeName 要解析的节点名称
        // 使用递归
        Iterator<Element> iterator = node.elementIterator();
        while (iterator.hasNext()) {
            Element e = iterator.next();
            listMap.addAll(listNodes(e, nodeName));
        }
        return listMap;
    }

    /**
     * xml转json
     *
     * @param element
     * @param json
     */
    public static void node2Json(Element element, JSONObject json) {
        // 如果是属性
        for (Object o : element.attributes()) {
            Attribute attr = (Attribute) o;
            if (!ObjectUtils.isEmpty(attr.getValue())) {
                json.put("@" + attr.getName(), attr.getValue());
            }
        }
        List<Element> chdEl = element.elements();
        if (chdEl.isEmpty() && !ObjectUtils.isEmpty(element.getText())) {// 如果没有子元素,只有一个值
            json.put(element.getName(), element.getText());
        }

        for (Element e : chdEl) {   // 有子元素
            if (!e.elements().isEmpty()) {  // 子元素也有子元素
                JSONObject chdjson = new JSONObject();
                node2Json(e, chdjson);
                Object o = json.get(e.getName());
                if (o != null) {
                    JSONArray jsona = null;
                    if (o instanceof JSONObject) {  // 如果此元素已存在,则转为jsonArray
                        JSONObject jsono = (JSONObject) o;
                        json.remove(e.getName());
                        jsona = new JSONArray();
                        jsona.add(jsono);
                        jsona.add(chdjson);
                    }
                    if (o instanceof JSONArray) {
                        jsona = (JSONArray) o;
                        jsona.add(chdjson);
                    }
                    json.put(e.getName(), jsona);
                } else {
                    if (!chdjson.isEmpty()) {
                        json.put(e.getName(), chdjson);
                    }
                }
            } else { // 子元素没有子元素
                for (Object o : element.attributes()) {
                    Attribute attr = (Attribute) o;
                    if (!ObjectUtils.isEmpty(attr.getValue())) {
                        json.put("@" + attr.getName(), attr.getValue());
                    }
                }
                if (!e.getText().isEmpty()) {
                    json.put(e.getName(), e.getText());
                }
            }
        }
    }
    public static  Element getRootElement(RequestEvent evt) throws DocumentException {

        return getRootElement(evt, "gb2312");
    }

    public static Element getRootElement(RequestEvent evt, String charset) throws DocumentException {
        Request request = evt.getRequest();
        return getRootElement(request.getRawContent(), charset);
    }

    public static Element getRootElement(byte[] content, String charset) throws DocumentException {
        if (charset == null) {
            charset = "gb2312";
        }
        SAXReader reader = new SAXReader();
        reader.setEncoding(charset);
        Document xml = reader.read(new ByteArrayInputStream(content));
        return xml.getRootElement();
    }

    private enum ChannelType{
        CivilCode, BusinessGroup,VirtualOrganization,Other
    }

    public static DeviceChannel channelContentHandler(Element itemDevice, Device device, String event, CivilCodeFileConfRunner civilCodeFileConf){
        DeviceChannel deviceChannel = new DeviceChannel();
        deviceChannel.setDeviceId(device.getDeviceId());
        Element channdelIdElement = itemDevice.element("DeviceID");
        if (channdelIdElement == null) {
            logger.warn("解析Catalog消息时发现缺少 DeviceID");
            return null;
        }
        String channelId = channdelIdElement.getTextTrim();
        if (ObjectUtils.isEmpty(channelId)) {
            logger.warn("解析Catalog消息时发现缺少 DeviceID");
            return null;
        }
        deviceChannel.setChannelId(channelId);
        if (event != null && !event.equals(CatalogEvent.ADD) && !event.equals(CatalogEvent.UPDATE)) {
            // 除了ADD和update情况下需要识别全部内容，
            return deviceChannel;
        }
        Element nameElement = itemDevice.element("Name");
        if (nameElement != null) {
            deviceChannel.setChannelName(nameElement.getText());
        }
        // 设备厂商
        String manufacturer = getText(itemDevice, "Manufacturer");
        if (!ObjectUtils.isEmpty(manufacturer)) {
            deviceChannel.setManufacturer(manufacturer);
        }
        // 设备型号
        String model = getText(itemDevice, "Model");
        if (!ObjectUtils.isEmpty(model)) {
            deviceChannel.setModel(model);
        }
        // 设备归属
        String owner = getText(itemDevice, "Owner");
        if (!ObjectUtils.isEmpty(owner)) {
            deviceChannel.setOwner(owner);
        }
        // 行政区域
        String civilCode = getText(itemDevice, "CivilCode");

        if (!ObjectUtils.isEmpty(civilCode)) {
            deviceChannel.setCivilCode(civilCode);
        }

        // 父设备/区域/系统ID
        String parentID = getText(itemDevice, "ParentID");
        if (parentID != null && parentID.equalsIgnoreCase("null")) {
            parentID = null;
        }
        if (!ObjectUtils.isEmpty(parentID)) {
            deviceChannel.setParentId(parentID);
        }
        // 虚拟组织所属的业务分组ID,业务分组根据特定的业务需求制定,一个业务分组包含一组特定的虚拟组织
        String businessGroupID = getText(itemDevice, "BusinessGroupID");

        // 注册方式(必选)缺省为1;1:符合IETFRFC3261标准的认证注册模式;2:基于口令的双向认证注册模式;3:基于数字证书的双向认证注册模式
        String registerWay = getText(itemDevice, "RegisterWay");
        if (!ObjectUtils.isEmpty(registerWay)) {
            try {
                deviceChannel.setRegisterWay(Integer.parseInt(registerWay));
            }catch (NumberFormatException exception) {
                logger.warn("[xml解析] 从通道数据获取registerWay失败： {}", registerWay);
            }
        }

        // 保密属性(必选)缺省为0;0:不涉密,1:涉密
        String secrecy = getText(itemDevice, "Secrecy");
        if (!ObjectUtils.isEmpty(secrecy)) {
            deviceChannel.setSecrecy(secrecy);
        }
        // 安装地址
        String address = getText(itemDevice, "Address");
        if (!ObjectUtils.isEmpty(address)) {
            deviceChannel.setAddress(address);
        }
        // 警区
        String block = getText(itemDevice, "Block");
        if (!ObjectUtils.isEmpty(block)) {
            deviceChannel.setBlock(block);
        }

        // 当为设备时,是否有子设备(必选)1有,0没有
        String parental = getText(itemDevice, "Parental");
        if (!ObjectUtils.isEmpty(parental)) {
            try {
                // 由于海康会错误的发送65535作为这里的取值,所以这里除非是0否则认为是1
                if (!ObjectUtils.isEmpty(parental) && parental.length() == 1 && Integer.parseInt(parental) == 0) {
                    deviceChannel.setParental(0);
                }else {
                    deviceChannel.setParental(1);
                }
            }catch (NumberFormatException e) {
                logger.warn("[xml解析] 从通道数据获取 parental失败： {}", parental);
            }
        }
// 信令安全模式(可选)缺省为0; 0:不采用;2:S/MIME 签名方式;3:S/MIME加密签名同时采用方式;4:数字摘要方式
        String safetyWay = getText(itemDevice, "SafetyWay");
        if (!ObjectUtils.isEmpty(safetyWay)) {
            try {
                deviceChannel.setSafetyWay(Integer.parseInt(safetyWay));
            }catch (NumberFormatException e) {
                logger.warn("[xml解析] 从通道数据获取 safetyWay失败： {}", safetyWay);
            }
        }

        // 证书序列号(有证书的设备必选)
        String certNum = getText(itemDevice, "CertNum");
        if (!ObjectUtils.isEmpty(certNum)) {
            deviceChannel.setCertNum(certNum);
        }

        // 证书有效标识(有证书的设备必选)缺省为0;证书有效标识:0:无效 1:有效
        String certifiable = getText(itemDevice, "Certifiable");
        if (!ObjectUtils.isEmpty(certifiable)) {
            try {
                deviceChannel.setCertifiable(Integer.parseInt(certifiable));
            }catch (NumberFormatException e) {
                logger.warn("[xml解析] 从通道数据获取 Certifiable失败： {}", certifiable);
            }
        }

        // 无效原因码(有证书且证书无效的设备必选)
        String errCode = getText(itemDevice, "ErrCode");
        if (!ObjectUtils.isEmpty(errCode)) {
            try {
                deviceChannel.setErrCode(Integer.parseInt(errCode));
            }catch (NumberFormatException e) {
                logger.warn("[xml解析] 从通道数据获取 ErrCode失败： {}", errCode);
            }
        }

        // 证书终止有效期(有证书的设备必选)
        String endTime = getText(itemDevice, "EndTime");
        if (!ObjectUtils.isEmpty(endTime)) {
            deviceChannel.setEndTime(endTime);
        }


        // 设备/区域/系统IP地址
        String ipAddress = getText(itemDevice, "IPAddress");
        if (!ObjectUtils.isEmpty(ipAddress)) {
            deviceChannel.setIpAddress(ipAddress);
        }

        // 设备/区域/系统端口
        String port = getText(itemDevice, "Port");
        if (!ObjectUtils.isEmpty(port)) {
            try {
                deviceChannel.setPort(Integer.parseInt(port));
            }catch (NumberFormatException e) {
                logger.warn("[xml解析] 从通道数据获取 Port失败： {}", port);
            }
        }

        // 设备口令
        String password = getText(itemDevice, "Password");
        if (!ObjectUtils.isEmpty(password)) {
            deviceChannel.setPassword(password);
        }


        // 设备状态
        String status = getText(itemDevice, "Status");
        if (status != null) {
            // ONLINE OFFLINE HIKVISION DS-7716N-E4 NVR的兼容性处理
            if (status.equals("ON") || status.equals("On") || status.equals("ONLINE") || status.equals("OK")) {
                deviceChannel.setStatus(1);
            }
            if (status.equals("OFF") || status.equals("Off") || status.equals("OFFLINE")) {
                deviceChannel.setStatus(0);
            }
        }
        // 经度
        String longitude = getText(itemDevice, "Longitude");
        if (NumericUtil.isDouble(longitude)) {
            deviceChannel.setLongitude(Double.parseDouble(longitude));
        } else {
            deviceChannel.setLongitude(0.00);
        }

        // 纬度
        String latitude = getText(itemDevice, "Latitude");
        if (NumericUtil.isDouble(latitude)) {
            deviceChannel.setLatitude(Double.parseDouble(latitude));
        } else {
            deviceChannel.setLatitude(0.00);
        }
        // -摄像机类型扩展,标识摄像机类型:1-球机;2-半球;3-固定枪机;4-遥控枪机。当目录项为摄像机时可选
        String ptzType = getText(itemDevice, "PTZType");
        if (ObjectUtils.isEmpty(ptzType)) {
            //兼容INFO中的信息
            Element info = itemDevice.element("Info");
            String ptzTypeFromInfo = XmlUtil.getText(info, "PTZType");
            if(!ObjectUtils.isEmpty(ptzTypeFromInfo)){
                try {
                    deviceChannel.setPtzType(Integer.parseInt(ptzTypeFromInfo));
                }catch (NumberFormatException e){
                    logger.warn("[xml解析] 从通道数据info中获取PTZType失败： {}", ptzTypeFromInfo);
                }
            }
        } else {
            try {
                deviceChannel.setPtzType(Integer.parseInt(ptzType));
            }catch (NumberFormatException e){
                logger.warn("[xml解析] 从通道数据中获取PTZType失败： {}", ptzType);
            }
        }

        int code = 0;

        if(channelId.length() <= 8) {
            //行政区划的编码 暂不考虑特殊处理

        }else {
            if (channelId.length() != 20) {
                logger.warn("[xml解析] 失败，编号不符合国标28181定义： {}", channelId);
                return null;
            }
        }
        code = Integer.parseInt(channelId.substring(10, 13));
        //区分节点与通道的数据处理

        deviceChannel.setGbCode(code);
        switch (code){
            case 215:
                // 业务分组
                if (!ObjectUtils.isEmpty(parentID)) {
                    if (!parentID.trim().equalsIgnoreCase(device.getDeviceId())) {
                        logger.warn(LogTemplate.PROCESS_LOG_TEMPLATE,"同步xlm解析","异常业务分组，和平台的编码不一致",parentID);
                        deviceChannel.setParentId(device.getDeviceId());
                    }
                }
                break;
            case 216:
                // 虚拟组织
                if (!ObjectUtils.isEmpty(businessGroupID)) {
                    deviceChannel.setBusinessGroupId(businessGroupID);
                }

                if (!ObjectUtils.isEmpty(parentID)) {
                    if (parentID.contains("/")) {
                        //兼容多条路径的数据
                        String[] parentIdArray = parentID.split("/");
                        parentID = parentIdArray[parentIdArray.length - 1];
                    }
                    deviceChannel.setParentId(parentID);
                }
                break;

        }
        return deviceChannel;
    }




    /**
     * 简单类型处理
     *
     * @param tClass
     * @param val
     * @return
     */
    private static Object simpleTypeDeal(Class<?> tClass, Object val) {
        if (tClass.equals(String.class)) {
            return val.toString();
        }
        if (tClass.equals(Integer.class)) {
            return Integer.valueOf(val.toString());
        }
        if (tClass.equals(Double.class)) {
            return Double.valueOf(val.toString());
        }
        if (tClass.equals(Long.class)) {
            return Long.valueOf(val.toString());
        }
        return val;
    }
}