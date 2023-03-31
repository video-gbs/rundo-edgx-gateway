package com.runjian.gb28181.transmit.event.request.impl.message.response.cmd;

import com.alibaba.fastjson.JSONObject;
import com.runjian.common.commonDto.Gateway.dto.ChannelRecordInfo;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.common.constant.BusinessSceneConstants;
import com.runjian.common.constant.GatewayMsgType;
import com.runjian.common.constant.LogTemplate;
import com.runjian.common.constant.VideoManagerConstants;
import com.runjian.common.utils.BeanUtil;
import com.runjian.common.utils.DateUtils;
import com.runjian.common.utils.redis.RedisCommonUtil;
import com.runjian.conf.UserSetting;
import com.runjian.gb28181.bean.*;
import com.runjian.gb28181.event.EventPublisher;
import com.runjian.gb28181.session.RecordDataCatch;
import com.runjian.gb28181.transmit.callback.DeferredResultHolder;
import com.runjian.gb28181.transmit.callback.RequestMessage;
import com.runjian.gb28181.transmit.event.request.SIPRequestProcessorParent;
import com.runjian.gb28181.transmit.event.request.impl.message.IMessageHandler;
import com.runjian.gb28181.transmit.event.request.impl.message.response.ResponseMessageHandler;
import com.runjian.service.IRedisCatchStorageService;
import com.runjian.utils.DateUtil;
import com.runjian.utils.UJson;
import gov.nist.javax.sip.message.SIPRequest;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.SipException;
import javax.sip.message.Response;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import static com.runjian.gb28181.utils.XmlUtil.getText;

/**
 * 国标级联-国标录像
 * @author lin
 */
@Component
public class RecordInfoResponseMessageHandler extends SIPRequestProcessorParent implements InitializingBean, IMessageHandler {

    private final Logger logger = LoggerFactory.getLogger(RecordInfoResponseMessageHandler.class);
    private final String cmdType = "RecordInfo";


    @Autowired
    private ResponseMessageHandler responseMessageHandler;


    @Autowired
    private RecordDataCatch recordDataCatch;


    @Autowired
    private EventPublisher eventPublisher;

    @Qualifier("taskExecutor")
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    private UserSetting userSetting;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    IRedisCatchStorageService redisCatchStorageService;


    private Long expireRecordInfoTtl = 300L;


    @Override
    public void afterPropertiesSet() throws Exception {
        responseMessageHandler.addHandler(cmdType, this);
    }

    @Override
    public void handForDevice(RequestEvent evt, Device device, Element rootElement) {
        try {
            // 回复200 OK
             responseAck((SIPRequest) evt.getRequest(), Response.OK);
        }catch (SipException | InvalidArgumentException | ParseException e) {
            logger.error(LogTemplate.ERROR_LOG_TEMPLATE, "国标级联-国标录像", "命令发送失败", e);
        }
        taskExecutor.execute(()->{
            try {
                String sn = getText(rootElement, "SN");
                String channelId = getText(rootElement, "DeviceID");
                String businessSceneKey = GatewayMsgType.RECORD_INFO.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+device.getDeviceId()+ BusinessSceneConstants.SCENE_STREAM_KEY+channelId;

                RecordInfo recordInfo = new RecordInfo();
                recordInfo.setChannelId(channelId);
                recordInfo.setDeviceId(device.getDeviceId());
                recordInfo.setSn(sn);
                recordInfo.setName(getText(rootElement, "Name"));
                String sumNumStr = getText(rootElement, "SumNum");
                int sumNum = 0;
                if (!ObjectUtils.isEmpty(sumNumStr)) {
                    sumNum = Integer.parseInt(sumNumStr);
                }
                recordInfo.setSumNum(sumNum);
                Element recordListElement = rootElement.element("RecordList");
                if (recordListElement == null || sumNum == 0) {
                    logger.info("无录像数据");
                    releaseRequest(businessSceneKey, new RecordInfo());
                } else {
                    Iterator<Element> recordListIterator = recordListElement.elementIterator();
                    if (recordListIterator != null) {
                        List<RecordItem> recordList = new ArrayList<>();
                        // 遍历DeviceList
                        while (recordListIterator.hasNext()) {
                            Element itemRecord = recordListIterator.next();
                            Element recordElement = itemRecord.element("DeviceID");
                            if (recordElement == null) {
                                logger.info("记录为空，下一个...");
                                continue;
                            }
                            RecordItem record = new RecordItem();
                            record.setDeviceId(getText(itemRecord, "DeviceID"));
                            record.setName(getText(itemRecord, "Name"));
                            record.setFilePath(getText(itemRecord, "FilePath"));
                            record.setFileSize(getText(itemRecord, "FileSize"));
                            record.setAddress(getText(itemRecord, "Address"));

                            String startTimeStr = getText(itemRecord, "StartTime");
                            record.setStartTime(DateUtil.ISO8601Toyyyy_MM_dd_HH_mm_ss(startTimeStr));

                            String endTimeStr = getText(itemRecord, "EndTime");
                            record.setEndTime(DateUtil.ISO8601Toyyyy_MM_dd_HH_mm_ss(endTimeStr));

                            record.setSecrecy(itemRecord.element("Secrecy") == null ? 0
                                    : Integer.parseInt(getText(itemRecord, "Secrecy")));
                            record.setType(getText(itemRecord, "Type"));
                            record.setRecorderId(getText(itemRecord, "RecorderID"));
                            recordList.add(record);
                        }
                        Map<String, String> map = recordList.stream()
                                .filter(record -> record.getDeviceId() != null)
                                .collect(Collectors.toMap(record -> record.getStartTime()+ record.getEndTime(), UJson::writeJson));
                        // 获取任务结果数据
                        String resKey = VideoManagerConstants.REDIS_RECORD_INFO_RES_PRE + channelId + sn;
                        RedisCommonUtil.hmset(redisTemplate, resKey, map, expireRecordInfoTtl);
                        String resCountKey = VideoManagerConstants.REDIS_RECORD_INFO_RES_COUNT_PRE + channelId + sn;
                        long incr = RedisCommonUtil.incr(resCountKey, map.size(),redisTemplate);
                        RedisCommonUtil.expire(redisTemplate,resCountKey, expireRecordInfoTtl);
                        recordInfo.setRecordList(recordList);
                        if (incr < sumNum) {
                            return;
                        }
                        // 已接收完成
                        List<RecordItem> resList = RedisCommonUtil.hmget(redisTemplate,resKey).values().stream().map(e -> UJson.readJson(e.toString(), RecordItem.class)).collect(Collectors.toList());
                        if (resList.size() < sumNum) {
                            logger.error("[国标录像] 缓存异常={}| ",channelId);
                            return;
                        }
                        recordInfo.setRecordList(resList);
                        releaseRequest(businessSceneKey,recordInfo);

                    }
                }
            } catch (Exception e) {
                logger.error("[国标录像] 发现未处理的异常, "+e.getMessage(), e);
            }
        });
    }

    @Override
    public void handForPlatform(RequestEvent evt, ParentPlatform parentPlatform, Element element) {

    }

    public void releaseRequest(String businessSceneKey,RecordInfo recordInfo){
        // 对数据进行排序
        logger.info("录像数据数据完成={}",businessSceneKey);
        List<RecordItem> recordList = recordInfo.getRecordList();
        List<RecordItem> recordItems = new ArrayList<>();
        if(!CollectionUtils.isEmpty(recordList)){
            Collections.sort(recordList);
            //对数据进行整点的封装
            recordItems = packingRecordTime(recordList);
        }
        recordInfo.setRecordList(recordItems);
        redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.RECORD_INFO, BusinessErrorEnums.SUCCESS,recordInfo);

    }

    /**
     * 对数据进行封装,大于30分钟的进行切割，小于30分钟的不管
     * 针对首尾跨天的部分视频时间段进行兼容如2022-01-01 23：06--2022-01-02 00：12
     * 数据示例
     * {
     *         "deviceId": "34020000001310000002",
     *         "name": "IPC",
     *         "filePath": "1663170780_1663171378",
     *         "fileSize": null,
     *         "address": "Address 1",
     *         "startTime": "2022-09-14 23:53:00",
     *         "endTime": "2022-09-15 00:02:58",
     *         "secrecy": 0,
     *         "type": "time",
     *         "recorderId": null
     *       },
     *       {
     *         "deviceId": "34020000001310000002",
     *         "name": "IPC",
     *         "filePath": "1663171378_1663171976",
     *         "fileSize": null,
     *         "address": "Address 1",
     *         "startTime": "2022-09-15 00:02:58",
     *         "endTime": "2022-09-15 00:12:56",
     *         "secrecy": 0,
     *         "type": "time",
     *         "recorderId": null
     *       },
     *
     *       {
     *         "deviceId": "34020000001310000002",
     *         "name": "IPC",
     *         "filePath": "1663136783_1663137381",
     *         "fileSize": null,
     *         "address": "Address 1",
     *         "startTime": "2022-09-14 14:26:23",
     *         "endTime": "2022-09-14 14:36:21",
     *         "secrecy": 0,
     *         "type": "time",
     *         "recorderId": null
     *       },
     *       {
     *         "deviceId": "34020000001310000002",
     *         "name": "IPC",
     *         "filePath": "1663137381_1663137979",
     *         "fileSize": null,
     *         "address": "Address 1",
     *         "startTime": "2022-09-14 14:36:21",
     *         "endTime": "2022-09-14 14:46:19",
     *         "secrecy": 0,
     *         "type": "time",
     *         "recorderId": null
     *       },
     * 00:00--01:00
     * 01:00--02:00
     *
     * @param recordItemList
     */
    private List<RecordItem> packingRecordTime(List<RecordItem> recordItemList){
        //先聚合为大段的视频数据

        List<RecordItem> mergeRecordItemList = new ArrayList<>();
        String lastStart = "";
        String lastEnd = "";

        for (RecordItem recordItem : recordItemList){
            String startTime = recordItem.getStartTime();
            String endTime = recordItem.getEndTime();
            if(ObjectUtils.isEmpty(lastStart) || ObjectUtils.isEmpty(lastEnd)){
                //首次循环不进行比较 只进行赋值
                lastStart = startTime;
                lastEnd = endTime;
            }else {
                if(!lastEnd.equals(startTime)){
                    //清空此次循环,数组组装暂时结束，开启新的循环
                    RecordItem tmpRecordItem = new RecordItem();
                    tmpRecordItem.setDeviceId(recordItem.getDeviceId());
                    tmpRecordItem.setName(recordItem.getName());
                    tmpRecordItem.setAddress(recordItem.getAddress());
                    tmpRecordItem.setStartTime(lastStart);
                    tmpRecordItem.setEndTime(lastEnd);
                    tmpRecordItem.setSecrecy(recordItem.getSecrecy());
                    tmpRecordItem.setType(recordItem.getType());

                    mergeRecordItemList.add(tmpRecordItem);
                    lastStart = startTime;
                    lastEnd = endTime;

                }else {
                    //结束时间替换为此次循环的结束时间
                    lastEnd = endTime;
                }
            }

        }
        //补全最后缺失的一次循环
        RecordItem lastRecordItem = new RecordItem();
        lastRecordItem.setDeviceId(recordItemList.get(0).getDeviceId());
        lastRecordItem.setName(recordItemList.get(0).getName());
        lastRecordItem.setAddress(recordItemList.get(0).getAddress());
        lastRecordItem.setStartTime(lastStart);
        lastRecordItem.setEndTime(lastEnd);
        lastRecordItem.setSecrecy(recordItemList.get(0).getSecrecy());
        lastRecordItem.setType(recordItemList.get(0).getType());
        mergeRecordItemList.add(lastRecordItem);

        List<RecordItem> newRecordItemList = new ArrayList<>();
        mergeRecordItemList.forEach(recordItem -> {
            dealPackingRecordTime(recordItem,newRecordItemList);
        });


        return newRecordItemList;

    }

    private void dealPackingRecordTime(RecordItem recordItem,List<RecordItem> newRecordItemList){
        int deviceDownloadTimeCycle = userSetting.getDeviceDownloadTimeCycle();
        //防止deviceDownloadTimeCycle设置的过小导致堆栈溢出,最小为10分钟
        if(deviceDownloadTimeCycle < 600){
            //不进行处理，参数不合法
            logger.error(LogTemplate.PROCESS_LOG_TEMPLATE, "国标级联-国标录像", "国标录像列表最大录像切割参数设置非法,不进行处理，参数不合法");
            return;
        }

        String startTime = recordItem.getStartTime();
        String endTime = recordItem.getEndTime();
        long startLong = DateUtils.StringToTimeStamp(startTime);
        long endLong = DateUtils.StringToTimeStamp(endTime);
        if(startLong == endLong){
            //处理结束
            return;
        }
        //大于30分钟的进行文件日期切割,判断开始时间与当前时间半点的大小，如：12：30
        long dealhalfDateHourTime = DateUtils.dealDateHourTime(startTime, deviceDownloadTimeCycle);
        //下个整点时间戳
        long nextClockHourTime = dealhalfDateHourTime+deviceDownloadTimeCycle;
        String dealStartTime;
        if(startLong < dealhalfDateHourTime){
            //半小时内，结束时间为整点半dealDateHourTime
            if(endLong < dealhalfDateHourTime){
                dealStartTime = endTime;
            }else {
                dealStartTime = DateUtils.TimeStampToString(dealhalfDateHourTime,null);
            }
        }else {
            //半小时外，结束时间为下个整点dealDateHourTime+30分钟
            if(endLong < nextClockHourTime){
                dealStartTime = endTime;

            }else {
                dealStartTime = DateUtils.TimeStampToString(dealhalfDateHourTime,deviceDownloadTimeCycle);

            }

        }

        RecordItem dealRecordItem = new RecordItem();
        BeanUtil.copyProperties(recordItem,dealRecordItem);
        dealRecordItem.setStartTime(dealStartTime);
        dealRecordItem.setEndTime(endTime);

        RecordItem recordItem1 = new RecordItem();
        BeanUtil.copyProperties(recordItem,recordItem1);
        recordItem1.setEndTime(dealStartTime);
        newRecordItemList.add(recordItem1);
        dealPackingRecordTime(dealRecordItem,newRecordItemList);

    }
}
