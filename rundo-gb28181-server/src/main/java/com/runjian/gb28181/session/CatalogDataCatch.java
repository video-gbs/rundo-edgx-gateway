package com.runjian.gb28181.session;

import com.runjian.dao.DeviceChannelMapper;
import com.runjian.gb28181.bean.CatalogData;
import com.runjian.gb28181.bean.Device;
import com.runjian.gb28181.bean.DeviceChannel;
import com.runjian.gb28181.bean.SyncStatus;
import com.runjian.service.IDeviceChannelService;
import com.runjian.service.IDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class CatalogDataCatch {

    public static Map<String, CatalogData> data = new ConcurrentHashMap<>();



    public void addReady(Device device, int sn ) {
        CatalogData catalogData = data.get(device.getDeviceId());
        if (catalogData == null || catalogData.getStatus().equals(CatalogData.CatalogDataStatus.end)) {
            catalogData = new CatalogData();
            catalogData.setChannelList(Collections.synchronizedList(new ArrayList<>()));
            catalogData.setDevice(device);
            catalogData.setSn(sn);
            catalogData.setStatus(CatalogData.CatalogDataStatus.ready);
            catalogData.setLastTime(Instant.now());
            data.put(device.getDeviceId(), catalogData);
        }
    }

    public void put(String deviceId, int sn, int total, Device device, List<DeviceChannel> deviceChannelList) {
        CatalogData catalogData = data.get(deviceId);
        if (catalogData == null) {
            catalogData = new CatalogData();
            catalogData.setSn(sn);
            catalogData.setTotal(total);
            catalogData.setDevice(device);
            catalogData.setChannelList(Collections.synchronizedList(new ArrayList<>()));
            catalogData.setStatus(CatalogData.CatalogDataStatus.runIng);
            catalogData.setLastTime(Instant.now());
            data.put(deviceId, catalogData);
        }else {
            // 同一个设备的通道同步请求只考虑一个，其他的直接忽略
            if (catalogData.getSn() != sn) {
                return;
            }
            catalogData.setTotal(total);
            catalogData.setDevice(device);
            catalogData.setStatus(CatalogData.CatalogDataStatus.runIng);
            catalogData.getChannelList().addAll(deviceChannelList);
            catalogData.setLastTime(Instant.now());
        }
    }

    public List<DeviceChannel> get(String deviceId) {
        CatalogData catalogData = data.get(deviceId);
        if (catalogData == null) {
            return null;
        }
        return catalogData.getChannelList();
    }

    /**
     * 返回通道同步数据
     * @param deviceId
     * @return
     */
    public CatalogData getData(String deviceId) {
        return data.get(deviceId);
    }
    public int getTotal(String deviceId) {
        CatalogData catalogData = data.get(deviceId);
        if (catalogData == null) {
            return 0;
        }
        return catalogData.getTotal();
    }

    public SyncStatus getSyncStatus(String deviceId) {
        CatalogData catalogData = data.get(deviceId);
        if (catalogData == null) {
            return null;
        }
        SyncStatus syncStatus = new SyncStatus();
        syncStatus.setCurrent(catalogData.getChannelList().size());
        syncStatus.setTotal(catalogData.getTotal());
        syncStatus.setErrorMsg(catalogData.getErrorMsg());
        if (catalogData.getStatus().equals(CatalogData.CatalogDataStatus.end)) {
            syncStatus.setSyncIng(false);
        }else {
            syncStatus.setSyncIng(true);
        }
        return syncStatus;
    }

    public boolean isSyncRunning(String deviceId) {
        CatalogData catalogData = data.get(deviceId);
        if (catalogData == null) {
            return false;
        }
        return !catalogData.getStatus().equals(CatalogData.CatalogDataStatus.end);
    }

    @Scheduled(fixedRate = 5 * 1000)   //每5秒执行一次, 发现数据5秒未更新则移除数据并认为数据接收超时
    private void timerTask(){
        Set<String> keys = data.keySet();

        Instant instantBefore5S = Instant.now().minusMillis(TimeUnit.SECONDS.toMillis(5));
        Instant instantBefore30S = Instant.now().minusMillis(TimeUnit.SECONDS.toMillis(30));

        for (String deviceId : keys) {
            CatalogData catalogData = data.get(deviceId);
            //30秒超时删除
            if(catalogData.getLastTime().isBefore(instantBefore30S)){
                data.remove(deviceId);
            }

        }
    }


    public void setChannelSyncEnd(String deviceId, String errorMsg,int code) {
        CatalogData catalogData = data.get(deviceId);
        if (catalogData == null) {
            return;
        }
        catalogData.setStatus(CatalogData.CatalogDataStatus.end);
        catalogData.setErrorMsg(errorMsg);
        catalogData.setCode(code);
    }
}
