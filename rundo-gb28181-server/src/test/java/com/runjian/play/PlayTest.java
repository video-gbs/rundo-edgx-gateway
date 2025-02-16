package com.runjian.play;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.runjian.common.commonDto.Gateway.req.PlayReq;
import com.runjian.common.commonDto.Gb28181Media.resp.StreamAudioMediaInfoResp;
import com.runjian.common.commonDto.Gb28181Media.resp.StreamMediaInfoResp;
import com.runjian.common.commonDto.Gb28181Media.resp.StreamVideoMediaInfoResp;
import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.common.constant.GatewayMsgType;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.gb28181.bean.Device;
import com.runjian.service.IDeviceService;
import com.runjian.service.IplayService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class PlayTest {

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;
    @Autowired
    IplayService playService;
    @Test
    public void testPlay(){
        PlayReq playReq = new PlayReq();
        playReq.setChannelId("34020000001310000001");
        playReq.setDeviceId("34020000001110000001");
        playReq.setMsgId("12345678");
        playReq.setStreamMode(0);

        playService.play(playReq);
    }

    @Test
    public void testPlayBack(){

    }

    @Test
    public void zlmMediaInfo(){
        String mediainfo = "{\n" +
                "  \"code\" : 0,\n" +
                "  \"online\" : true,\n" +
                "  \"readerCount\" : 0,\n" +
                "  \"totalReaderCount\" : 0,\n" +
                "  \"tracks\" : [\n" +
                "        {\n" +
                "           \"channels\" : 1,\n" +
                "           \"codec_id\" : 2,\n" +
                "           \"codec_id_name\" : \"CodecAAC\",\n" +
                "           \"codec_type\" : 1,\n" +
                "           \"ready\" : true,\n" +
                "           \"sample_bit\" : 16,\n" +
                "           \"sample_rate\" : 8000\n" +
                "        },\n" +
                "        {\n" +
                "           \"codec_id\" : 0,\n" +
                "           \"codec_id_name\" : \"CodecH264\",\n" +
                "           \"codec_type\" : 0,\n" +
                "           \"fps\" : 59, \n" +
                "           \"height\" : 720,\n" +
                "           \"ready\" : true, \n" +
                "           \"width\" : 1280\n" +
                "        }\n" +
                "  ]\n" +
                "}";
        JSONObject jsonObject = JSONObject.parseObject(mediainfo);
        StreamMediaInfoResp streamMediaInfoResp = JSONObject.toJavaObject(jsonObject, StreamMediaInfoResp.class);
        //数值转驼峰  jsonarray
        JSONArray tracksArray = (JSONArray)streamMediaInfoResp.getTracks();

        JSONArray objectsArr = new JSONArray();
        StreamAudioMediaInfoResp streamAudioMediaInfoResp = new StreamAudioMediaInfoResp();
        StreamVideoMediaInfoResp streamVideoMediaInfoResp = new StreamVideoMediaInfoResp();
        for (Object trackOne : tracksArray) {
            JSONObject trackJson = (JSONObject)trackOne;
            if(trackJson.getInteger("codec_type") == 0){
                //视频
                streamVideoMediaInfoResp.setCodecName(trackJson.getString("codec_id_name"));
                streamVideoMediaInfoResp.setCodecType(0);
                streamVideoMediaInfoResp.setFps(trackJson.getInteger("fps"));
                streamVideoMediaInfoResp.setHeight(trackJson.getInteger("height"));
                streamVideoMediaInfoResp.setWidth(trackJson.getInteger("width"));
                streamVideoMediaInfoResp.setReady(trackJson.getBoolean("ready"));
                objectsArr.add(streamVideoMediaInfoResp);
            }else {
                streamAudioMediaInfoResp.setChannels(trackJson.getInteger("channels"));
                streamAudioMediaInfoResp.setCodecName(trackJson.getString("codec_id_name"));
                streamAudioMediaInfoResp.setCodecType(1);
                streamAudioMediaInfoResp.setReady(trackJson.getBoolean("ready"));
                streamAudioMediaInfoResp.setSampleBit(trackJson.getInteger("sample_bit"));
                streamAudioMediaInfoResp.setSampleRate(trackJson.getInteger("sample_rate"));
                objectsArr.add(streamAudioMediaInfoResp);
            }

        }
        streamMediaInfoResp.setTracks(objectsArr);
        log.info("哈哈={}",objectsArr);
        log.info("哈哈={}",streamMediaInfoResp);

    }

    @Test
    public void testaaPlay(){
        String ss = "{\"serialNum\":\"eb48104ddf2b4760be123783b3621051\",\"msgType\":\"CHANNEL_PLAY\",\"msgId\":\"4357\",\"time\":\"2023-04-19 19:04:10\",\"code\":0,\"\n" +
                "msg\":\"SUCCESS\",\"data\":{\"streamMode\":\"UDP\",\"deviceId\":\"34020000001330000185\",\"channelId\":\"34020000001320000185\",\"dispatchUrl\":\"http://192.168.0.84:18081\",\"streamId\":\"LIVE_91\",\"ssrcInfo\":{\"port\":22136,\"ssrc\":\"0102002823\",\"sdpIp\":\"124.71.21.11\",\"streamId\":\"LIVE_91\",\"mediaServerId\":\"LORVY158beilorvy\"},\"msgId\":null}}";
        JSONObject jsonMsg = JSONObject.parseObject(ss);

        CommonMqDto commonMqDto = JSONObject.toJavaObject(jsonMsg, CommonMqDto.class);

        JSONObject dataJson = (JSONObject) commonMqDto.getData();
        //设备信息同步  获取设备信息
        PlayReq playReq = JSONObject.toJavaObject(dataJson, PlayReq.class);
        playReq.setMsgId(commonMqDto.getMsgId());



    }

}
