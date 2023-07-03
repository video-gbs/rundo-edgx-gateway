package com.runjian.media.manager.storager;

import cn.hutool.core.bean.BeanUtil;
import com.runjian.common.constant.VideoManagerConstants;
import com.runjian.common.utils.redis.RedisCommonUtil;
import com.runjian.media.manager.dto.entity.MediaServerEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class MediaServerRedisStorage {
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 更新流媒体信息
     * @param mediaServerEntity
     * @return
     */
    public Boolean update(MediaServerEntity mediaServerEntity){

        return RedisCommonUtil.set(redisTemplate, VideoManagerConstants.SELF_MEDIA_SERVER_PREFIX+mediaServerEntity.getId(),mediaServerEntity);
    }

    public MediaServerEntity getDefaultMediaServer(){
        List<Object> scan = RedisCommonUtil.scan(redisTemplate, VideoManagerConstants.SELF_MEDIA_SERVER_PREFIX + "*");
        MediaServerEntity mediaServerEntity = new MediaServerEntity();
        ArrayList<MediaServerEntity> mediaServerEntities = new ArrayList<>();
        if(!ObjectUtils.isEmpty(scan)){
            scan.forEach(obj->{
                //key
                MediaServerEntity mediaServerEntityOne = (MediaServerEntity)RedisCommonUtil.get(redisTemplate, (String) obj);
                if(mediaServerEntityOne.isDefaultServer()){
                    BeanUtil.copyProperties(mediaServerEntityOne,mediaServerEntity);
                }
            });
        }
        return mediaServerEntity;
    }

    public List<MediaServerEntity> selectAllMediaserver(){
        List<Object> scan = RedisCommonUtil.scan(redisTemplate, VideoManagerConstants.SELF_MEDIA_SERVER_PREFIX + "*");
        ArrayList<MediaServerEntity> mediaServerEntities = new ArrayList<>();
        if(!ObjectUtils.isEmpty(scan)){
            scan.forEach(obj->{
                //key
                mediaServerEntities.add((MediaServerEntity)RedisCommonUtil.get(redisTemplate, (String) obj));

            });
        }
        return mediaServerEntities;
    }

    public MediaServerEntity selectByMediaServerId(String mediaServerId){

        return (MediaServerEntity)RedisCommonUtil.get(redisTemplate, VideoManagerConstants.SELF_MEDIA_SERVER_PREFIX+mediaServerId);
    }
}
