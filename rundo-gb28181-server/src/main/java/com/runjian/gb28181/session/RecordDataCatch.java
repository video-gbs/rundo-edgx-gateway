package com.runjian.gb28181.session;

import com.runjian.gb28181.bean.RecordInfo;
import com.runjian.gb28181.bean.RecordItem;
import com.runjian.gb28181.transmit.callback.DeferredResultHolder;
import com.runjian.gb28181.transmit.callback.RequestMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author lin
 */
@Component
public class RecordDataCatch {

    public static Map<String, RecordInfo> data = new ConcurrentHashMap<>();

    @Autowired
    private DeferredResultHolder deferredResultHolder;


    public int put(String deviceId, String sn, int sumNum, List<RecordItem> recordItems) {
        String key = deviceId + sn;
        RecordInfo recordInfo = data.get(key);
        if (recordInfo == null) {
            recordInfo = new RecordInfo();
            recordInfo.setDeviceId(deviceId);
            recordInfo.setSn(sn.trim());
            recordInfo.setSumNum(sumNum);
            recordInfo.setRecordList(Collections.synchronizedList(new ArrayList<>()));
            recordInfo.setLastTime(Instant.now());
            recordInfo.getRecordList().addAll(recordItems);
            data.put(key, recordInfo);
        }else {
            // 同一个设备的通道同步请求只考虑一个，其他的直接忽略
            if (!Objects.equals(sn.trim(), recordInfo.getSn())) {
                return 0;
            }
            recordInfo.getRecordList().addAll(recordItems);
            recordInfo.setLastTime(Instant.now());
        }
        return recordInfo.getRecordList().size();
    }


    public boolean isComplete(String deviceId, String sn) {
        RecordInfo recordInfo = data.get(deviceId + sn);
        return recordInfo != null && recordInfo.getRecordList().size() >= recordInfo.getSumNum();
    }

    public RecordInfo getRecordInfo(String deviceId, String sn) {
        return data.get(deviceId + sn);
    }

    public void remove(String deviceId, String sn) {
        data.remove(deviceId + sn);
    }
}
