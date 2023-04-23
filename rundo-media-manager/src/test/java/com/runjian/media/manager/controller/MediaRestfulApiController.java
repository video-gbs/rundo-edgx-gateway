package com.runjian.media.manager.controller;

import com.runjian.media.manager.conf.MediaConfig;
import com.runjian.media.manager.dto.entity.MediaServerEntity;
import com.runjian.media.manager.service.IMediaRestfulApiService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class MediaRestfulApiController {
    @Autowired
    MediaConfig mediaConfig;

    @Autowired
    IMediaRestfulApiService iMediaRestfulApiService;

    @Test
    public void getMediaServerConfigApiTest(){
        MediaServerEntity mediaSerConfig = mediaConfig.getMediaSerConfig();
        iMediaRestfulApiService.getMediaServerConfigApi(mediaSerConfig);
    }


}
