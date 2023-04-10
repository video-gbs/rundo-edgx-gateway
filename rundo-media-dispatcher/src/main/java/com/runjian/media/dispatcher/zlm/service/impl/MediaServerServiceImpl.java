package com.runjian.media.dispatcher.zlm.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.runjian.common.commonDto.Gb28181Media.BaseRtpServerDto;
import com.runjian.common.commonDto.Gb28181Media.ZlmStreamDto;
import com.runjian.common.commonDto.Gb28181Media.req.GatewayBindReq;
import com.runjian.common.commonDto.Gb28181Media.resp.StreamCheckListResp;
import com.runjian.common.commonDto.SsrcInfo;
import com.runjian.common.commonDto.StreamPlayDto;
import com.runjian.common.commonDto.StreamInfo;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.exception.BusinessException;
import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.common.constant.*;
import com.runjian.common.mq.RabbitMqSender;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.common.utils.UuidUtil;
import com.runjian.common.utils.redis.RedisCommonUtil;
import com.runjian.media.dispatcher.conf.DynamicTask;
import com.runjian.media.dispatcher.conf.UserSetting;
import com.runjian.media.dispatcher.conf.mq.DispatcherSignInConf;
import com.runjian.media.dispatcher.dto.entity.OnlineStreamsEntity;
import com.runjian.media.dispatcher.service.IOnlineStreamsService;
import com.runjian.media.dispatcher.zlm.ZLMRESTfulUtils;
import com.runjian.media.dispatcher.zlm.ZLMRTPServerFactory;
import com.runjian.media.dispatcher.zlm.ZLMServerConfig;
import com.runjian.media.dispatcher.zlm.ZlmHttpHookSubscribe;
import com.runjian.media.dispatcher.zlm.dto.HookSubscribeFactory;
import com.runjian.media.dispatcher.zlm.dto.HookSubscribeForStreamChange;
import com.runjian.media.dispatcher.zlm.dto.MediaServerItem;
import com.runjian.media.dispatcher.zlm.event.publisher.EventPublisher;
import com.runjian.media.dispatcher.zlm.mapper.MediaServerMapper;
import com.runjian.media.dispatcher.zlm.service.IGatewayBindService;
import com.runjian.media.dispatcher.service.IRedisCatchStorageService;
import com.runjian.media.dispatcher.zlm.service.ImediaServerService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 媒体服务器节点管理
 */
@Service
@Slf4j
public class MediaServerServiceImpl implements ImediaServerService {

    private final static Logger logger = LoggerFactory.getLogger(MediaServerServiceImpl.class);

    private final String zlmKeepaliveKeyPrefix = "zlm-keepalive_";


    @Value("${server.ssl.enabled:false}")
    private boolean sslEnabled;

    @Value("${server.port}")
    private Integer serverPort;

    @Autowired
    private UserSetting userSetting;

    @Autowired
    private ZLMRESTfulUtils zlmresTfulUtils;

    @Autowired
    private MediaServerMapper mediaServerMapper;

    @Autowired
    DataSourceTransactionManager dataSourceTransactionManager;

    @Autowired
    TransactionDefinition transactionDefinition;

    @Autowired
    private ZLMRTPServerFactory zlmrtpServerFactory;

    @Autowired
    private EventPublisher publisher;

    @Autowired
    private DynamicTask dynamicTask;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ZlmHttpHookSubscribe subscribe;

    @Autowired
    RabbitMqSender rabbitMqSender;

    @Autowired
    IGatewayBindService gatewayBindService;

    @Autowired
    IRedisCatchStorageService redisCatchStorageService;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    DispatcherSignInConf dispatcherSignInConf;

    @Autowired
    IOnlineStreamsService onlineStreamsService;

    @Override
    public SsrcInfo openRTPServer(MediaServerItem mediaServerItem, BaseRtpServerDto baseRtpServerDto) {
        log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "调度服务", "创建端口请求", baseRtpServerDto);
        StreamPlayDto streamPlayResult = new StreamPlayDto();
        streamPlayResult.setStreamId(baseRtpServerDto.getStreamId());
        String businessSceneKey = GatewayMsgType.STREAM_PLAY_RESULT.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+baseRtpServerDto.getStreamId();
        SsrcInfo ssrcInfo = null;
        if (mediaServerItem == null || mediaServerItem.getId() == null) {
            throw new BusinessException(BusinessErrorEnums.VALID_BIND_EXCEPTION_ERROR);
        }
        String streamId = baseRtpServerDto.getStreamId();
        String ssrc = baseRtpServerDto.getSsrc();
        Boolean ssrcCheck = baseRtpServerDto.getSsrcCheck();
        Integer port = baseRtpServerDto.getPort();
        //缓存相关的请求参数
        RedisCommonUtil.set(redisTemplate,VideoManagerConstants.MEDIA_RTP_SERVER_REQ+ BusinessSceneConstants.SCENE_SEM_KEY+baseRtpServerDto.getStreamId(),baseRtpServerDto);

        //流注册成功 回调
        HookSubscribeForStreamChange hookSubscribe = HookSubscribeFactory.on_stream_changed(VideoManagerConstants.GB28181_APP, baseRtpServerDto.getStreamId(),true, VideoManagerConstants.GB28181_SCHEAM ,mediaServerItem.getId());
        subscribe.addSubscribe(hookSubscribe, (MediaServerItem mediaServerItemInUse, JSONObject json) -> {
            //流注册处理  发送指定mq消息
            logger.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "zlm推流注册成功通知", "收到推流订阅消息", json.toJSONString());
            //拼接拉流的地址
            String pushStreamId = json.getString("stream");


            GatewayBindReq gatewayBind = baseRtpServerDto.getGatewayBindReq();
            if(ObjectUtils.isEmpty(gatewayBind)){
                logger.error(LogTemplate.ERROR_LOG_TEMPLATE,"zlm回调点播成功异常","网关绑定异常",baseRtpServerDto);
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.STREAM_PLAY_RESULT,BusinessErrorEnums.MEDIA_ZLM_RTPSERVER_CREATE_ERROR,null);
                return;
            }
            StreamInfo streamInfoByAppAndStream = getStreamInfoByAppAndStream(mediaServerItem, VideoManagerConstants.GB28181_APP, pushStreamId);
            CommonMqDto mqInfo = redisCatchStorageService.getMqInfo(GatewayMsgType.PLAY_STREAM_CALLBACK.getTypeName(), GatewayCacheConstants.DISPATCHER_BUSINESS_SN_INCR, GatewayCacheConstants.GATEWAY_BUSINESS_SN_prefix,null);
            //通知网关进行播放结果显示
            mqInfo.setData(streamInfoByAppAndStream);
            rabbitMqSender.sendMsgByExchange(gatewayBind.getMqExchange(), gatewayBind.getMqRouteKey(), UuidUtil.toUuid(),mqInfo,true);
            //发送调度服务的业务队列 通知流实际成功
            streamPlayResult.setIsSuccess(true);
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.STREAM_PLAY_RESULT,BusinessErrorEnums.SUCCESS,streamPlayResult);

            // hook响应
            subscribe.removeSubscribe(hookSubscribe);
        });

        int rtpServerPort;
        if (mediaServerItem.isRtpEnable()) {
            rtpServerPort = zlmrtpServerFactory.createRTPServer(mediaServerItem, streamId, ssrcCheck?Integer.parseInt(ssrc):0, port);
        } else {
            // todo 暂时不考虑单端口服用的情况
            rtpServerPort = mediaServerItem.getRtpProxyPort();
        }
        if(rtpServerPort <=0 ){
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.STREAM_PLAY_RESULT,BusinessErrorEnums.MEDIA_ZLM_RTPSERVER_CREATE_ERROR,streamPlayResult);
            throw new BusinessException(BusinessErrorEnums.MEDIA_ZLM_RTPSERVER_CREATE_ERROR);
        }
        ssrcInfo = new SsrcInfo(rtpServerPort, ssrc, streamId,mediaServerItem.getId());
        ssrcInfo.setSdpIp(mediaServerItem.getSdpIp());



        return ssrcInfo;

    }



    @Override
    public Boolean closeRTPServer(MediaServerItem mediaServerItem, String streamId) {
        if (mediaServerItem == null) {
            return false;
        }
        return zlmrtpServerFactory.closeRTPServer(mediaServerItem, streamId);
    }

    @Override
    public Boolean closeRTPServer(String mediaServerId, String streamId) {
        MediaServerItem mediaServerItem = this.getOne(mediaServerId);
        return  closeRTPServer(mediaServerItem, streamId);
    }




    @Override
    public void update(MediaServerItem mediaSerItem) {
        mediaServerMapper.update(mediaSerItem);
    }

    @Override
    public List<MediaServerItem> getAllMediaServer() {

        return mediaServerMapper.queryAll();
    }


    @Override
    public List<MediaServerItem> getAllFromDatabase() {
        return mediaServerMapper.queryAll();
    }



    /**
     * 获取单个zlm服务器
     * @param mediaServerId 服务id
     * @return MediaServerItem
     */
    @Override
    public MediaServerItem getOne(String mediaServerId) {
        return mediaServerMapper.queryOne(mediaServerId);
    }

    @Override
    public MediaServerItem getDefaultMediaServer() {

        return mediaServerMapper.queryDefault();
    }

    @Override
    public MediaServerItem getDefaultMediaServer(String id) {
        return mediaServerMapper.queryOneDefault(id);
    }



    @Override
    public void add(MediaServerItem mediaServerItem) {
        mediaServerItem.setHookAliveInterval(120);
        JSONObject responseJSON = zlmresTfulUtils.getMediaServerConfig(mediaServerItem);
        if (responseJSON != null) {
            JSONArray data = responseJSON.getJSONArray("data");
            if (data != null && data.size() > 0) {
                ZLMServerConfig zlmServerConfig= JSON.parseObject(JSON.toJSONString(data.get(0)), ZLMServerConfig.class);
                if (mediaServerMapper.queryOne(zlmServerConfig.getGeneralMediaServerId()) != null) {
                    logger.error(LogTemplate.ERROR_LOG_TEMPLATE,"ZLM流媒体添加","流媒体添加失败",mediaServerItem);
                    throw new BusinessException(BusinessErrorEnums.MEDIA_ZLM_EXSITED_ERROR);
                }
                mediaServerItem.setId(zlmServerConfig.getGeneralMediaServerId());
                zlmServerConfig.setIp(mediaServerItem.getIp());
                mediaServerMapper.add(mediaServerItem);
                zlmServerOnline(zlmServerConfig);
            }else {
                logger.error(LogTemplate.ERROR_LOG_TEMPLATE,"ZLM流媒体添加","流媒体连接失败",mediaServerItem);
                throw new BusinessException(BusinessErrorEnums.MEDIA_ZLM_COLLECT_ERROR);
            }

        }else {
            logger.error(LogTemplate.ERROR_LOG_TEMPLATE,"ZLM流媒体添加","流媒体连接失败",mediaServerItem);
            throw new BusinessException(BusinessErrorEnums.MEDIA_ZLM_COLLECT_ERROR);
        }
    }

    @Override
    public int addToDatabase(MediaServerItem mediaSerItem) {
        return mediaServerMapper.add(mediaSerItem);
    }

    @Override
    public int updateToDatabase(MediaServerItem mediaSerItem) {
        int result = 0;
        if (mediaSerItem.isDefaultServer()) {
            TransactionStatus transactionStatus = dataSourceTransactionManager.getTransaction(transactionDefinition);
            int delResult = mediaServerMapper.delDefault();
            if (delResult == 0) {
                logger.error(LogTemplate.ERROR_LOG_TEMPLATE, "媒体服务器节点管理服务", "移除数据库默认zlm节点失败", null);

                //事务回滚
                dataSourceTransactionManager.rollback(transactionStatus);
                return 0;
            }
            result = mediaServerMapper.add(mediaSerItem);
            dataSourceTransactionManager.commit(transactionStatus);     //手动提交
        }else {
            result = mediaServerMapper.update(mediaSerItem);
        }
        return result;
    }

    /**
     * 处理zlm上线
     * @param zlmServerConfig zlm上线携带的参数
     */
    @Override
    public void zlmServerOnline(ZLMServerConfig zlmServerConfig) {

        MediaServerItem serverItem = mediaServerMapper.queryOne(zlmServerConfig.getGeneralMediaServerId());
        if (serverItem == null) {
            logger.warn(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "媒体服务器节点管理服务", String.format("[未注册的zlm] 拒接接入：%s来自%s：%S", zlmServerConfig.getGeneralMediaServerId(), zlmServerConfig.getIp(),zlmServerConfig.getHttpPort()), "请检查ZLM的<general.mediaServerId>配置是否与WVP的<media.id>一致");
            return;
        }else {
            logger.warn(LogTemplate.PROCESS_LOG_TEMPLATE, "媒体服务器节点管理服务", String.format("[ZLM] 正在连接: %s -> %s:%s", zlmServerConfig.getGeneralMediaServerId(), zlmServerConfig.getIp(), zlmServerConfig.getHttpPort()));
        }
        serverItem.setHookAliveInterval(zlmServerConfig.getHookAliveInterval());
        if (serverItem.getHttpPort() == 0) {
            serverItem.setHttpPort(zlmServerConfig.getHttpPort());
        }
        if (serverItem.getHttpSslPort() == 0) {
            serverItem.setHttpSslPort(zlmServerConfig.getHttpSslport());
        }
        if (serverItem.getRtmpPort() == 0) {
            serverItem.setRtmpPort(zlmServerConfig.getRtmpPort());
        }
        if (serverItem.getRtmpSslPort() == 0) {
            serverItem.setRtmpSslPort(zlmServerConfig.getRtmpSslPort());
        }
        if (serverItem.getRtspPort() == 0) {
            serverItem.setRtspPort(zlmServerConfig.getRtspPort());
        }
        if (serverItem.getRtspSslPort() == 0) {
            serverItem.setRtspSslPort(zlmServerConfig.getRtspSslport());
        }
        if (serverItem.getRtpProxyPort() == 0) {
            serverItem.setRtpProxyPort(zlmServerConfig.getRtpProxyPort());
        }
        serverItem.setStatus(true);

        if (ObjectUtils.isEmpty(serverItem.getId())) {
            logger.warn(LogTemplate.PROCESS_LOG_TEMPLATE, "媒体服务器节点管理服务", String.format("[未注册的zlm] serverItem缺少ID， 无法接入：%s：%s", zlmServerConfig.getIp(),zlmServerConfig.getHttpPort()));
            return;
        }
        mediaServerMapper.update(serverItem);

        if (serverItem.isAutoConfig()) {
            setZLMConfig(serverItem, "0".equals(zlmServerConfig.getHookEnable()));
        }
        final String zlmKeepaliveKey = zlmKeepaliveKeyPrefix + serverItem.getId();
        dynamicTask.stop(zlmKeepaliveKey);
        dynamicTask.startDelay(zlmKeepaliveKey, new KeepAliveTimeoutRunnable(serverItem), (serverItem.getHookAliveInterval() + 5) * 1000);
        publisher.zlmOnlineEventPublish(serverItem.getId());
        logger.info(LogTemplate.PROCESS_LOG_TEMPLATE, "媒体服务器节点管理服务", String.format("[ZLM] 连接成功: %s -> %s:%s", zlmServerConfig.getGeneralMediaServerId(), zlmServerConfig.getIp(), zlmServerConfig.getHttpPort()));
        //判断当前流列表数据是否与流媒体中数据一致  主要解决重启和网关异常断开重连播放流的同步问题
        clearOnlineStream(serverItem);

    }


    private void clearOnlineStream(MediaServerItem serverItem){
        List<OnlineStreamsEntity> onlineStreamsEntities = onlineStreamsService.streamList(serverItem.getId());
        if(!CollectionUtils.isEmpty(onlineStreamsEntities)){
            //查询当前流媒体中存在的流
            JSONObject rtspOnlineS = zlmresTfulUtils.getMediaListBySchema(serverItem, VideoManagerConstants.GB28181_APP, "rtsp");
            if(rtspOnlineS.getInteger("code") == 0){
                JSONArray dataArray = rtspOnlineS.getJSONArray("data");
                if(!CollectionUtils.isEmpty(dataArray)){
                    ArrayList<String> streamIdList = new ArrayList<>();
                    List<String> streamCollect = onlineStreamsEntities.stream().map(OnlineStreamsEntity::getStreamId).collect(Collectors.toList());
                    for(Object streamInfo : dataArray){
                        ZlmStreamDto zlmStreamDto = JSONObject.parseObject(streamInfo.toString(), ZlmStreamDto.class);
                        if(!streamCollect.contains(zlmStreamDto.getStream())){
                            streamIdList.add(zlmStreamDto.getStream());
                        }
                    }
                    //已经失效的播放流
                    if(!CollectionUtils.isEmpty(streamIdList)){
                        logger.info(LogTemplate.PROCESS_LOG_TEMPLATE, "zlm上线清理,已经停止的在线流",streamIdList);
                        onlineStreamsService.removeByStreamList(streamIdList);

                    }
                }else {
                    onlineStreamsService.removeAll();
                }

            }else {
                //连接失败  清理全部的流列表
                onlineStreamsService.removeAll();
            }

        }
    }

    class KeepAliveTimeoutRunnable implements Runnable{

        private MediaServerItem serverItem;

        public KeepAliveTimeoutRunnable(MediaServerItem serverItem) {
            this.serverItem = serverItem;
        }

        @Override
        public void run() {
            logger.info(LogTemplate.PROCESS_LOG_TEMPLATE, "媒体服务器节点管理服务", "[zlm心跳到期]：" + serverItem.getId());

            // 发起http请求验证zlm是否确实无法连接，如果确实无法连接则发送离线事件，否则不作处理
            JSONObject mediaServerConfig = zlmresTfulUtils.getMediaServerConfig(serverItem);
            if (mediaServerConfig != null && mediaServerConfig.getInteger("code") == 0) {
                logger.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "媒体服务器节点管理服务", "[ZLM] 心跳到期,验证后zlm仍在线，恢复心跳信息,请检查zlm是否可以正常向wvp发送心跳", serverItem.getId());
                // 添加zlm信息
                updateMediaServerKeepalive(serverItem.getId(), mediaServerConfig);
            }else {
                publisher.zlmOfflineEventPublish(serverItem.getId());
            }
        }
    }


    @Override
    public void zlmServerOffline(String mediaServerId) {
        final String zlmKeepaliveKey = zlmKeepaliveKeyPrefix + mediaServerId;
        dynamicTask.stop(zlmKeepaliveKey);
        //执行下线流列表的维护
        MediaServerItem serverItem = mediaServerMapper.queryOne(mediaServerId);
        if (serverItem == null) {
            logger.warn(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "媒体服务器节点下线管理服务","流媒体zlm不存在",mediaServerId);
            return;
        }
        clearOnlineStream(serverItem);
    }


    /**
     * 对zlm服务器进行基础配置
     * @param mediaServerItem 服务ID
     * @param restart 是否重启zlm
     */
    @Override
    public void setZLMConfig(MediaServerItem mediaServerItem, boolean restart) {
        logger.info(LogTemplate.PROCESS_LOG_TEMPLATE, "媒体服务器节点管理服务", String.format("[ZLM] 正在设置 ：%s -> %s:%s", mediaServerItem.getId(), mediaServerItem.getIp(), mediaServerItem.getHttpPort()));

        String protocol = sslEnabled ? "https" : "http";
        String hookPrex = String.format("%s://%s:%s/index/hook", protocol, mediaServerItem.getHookIp(), serverPort);

        Map<String, Object> param = new HashMap<>();
        param.put("api.secret",mediaServerItem.getSecret()); // -profile:v Baseline
        param.put("hook.enable","1");
        param.put("hook.on_flow_report",String.format("%s/on_flow_report", hookPrex));
        param.put("hook.on_play",String.format("%s/on_play", hookPrex));
        param.put("hook.on_http_access",String.format("%s/on_http_access", hookPrex));
        param.put("hook.on_publish", String.format("%s/on_publish", hookPrex));
        param.put("hook.on_record_ts",String.format("%s/on_record_ts", hookPrex));
        param.put("hook.on_rtsp_auth",String.format("%s/on_rtsp_auth", hookPrex));
        param.put("hook.on_rtsp_realm",String.format("%s/on_rtsp_realm", hookPrex));
        param.put("hook.on_server_started",String.format("%s/on_server_started", hookPrex));
        param.put("hook.on_shell_login",String.format("%s/on_shell_login", hookPrex));
        param.put("hook.on_stream_changed",String.format("%s/on_stream_changed", hookPrex));
        param.put("hook.on_stream_none_reader",String.format("%s/on_stream_none_reader", hookPrex));
        param.put("hook.on_stream_not_found",String.format("%s/on_stream_not_found", hookPrex));
        param.put("hook.on_server_keepalive",String.format("%s/on_server_keepalive", hookPrex));
        param.put("hook.on_send_rtp_stopped",String.format("%s/on_send_rtp_stopped", hookPrex));
        if (mediaServerItem.getRecordAssistPort() > 0) {
            param.put("hook.on_record_mp4",String.format("http://127.0.0.1:%s/api/record/on_record_mp4", mediaServerItem.getRecordAssistPort()));
        }else {
            param.put("hook.on_record_mp4","");
        }
        param.put("hook.timeoutSec","20");
        // 推流断开后可以在超时时间内重新连接上继续推流，这样播放器会接着播放。
        // 置0关闭此特性(推流断开会导致立即断开播放器)
        // 此参数不应大于播放器超时时间
        // 优化此消息以更快的收到流注销事件
        param.put("general.continue_push_ms", "3000" );
        // 最多等待未初始化的Track时间，单位毫秒，超时之后会忽略未初始化的Track, 设置此选项优化那些音频错误的不规范流，
        // 等zlm支持给每个rtpServer设置关闭音频的时候可以不设置此选项
//        param.put("general.wait_track_ready_ms", "3000" );
        if (mediaServerItem.isRtpEnable() && !ObjectUtils.isEmpty(mediaServerItem.getRtpPortRange())) {
            param.put("rtp_proxy.port_range", mediaServerItem.getRtpPortRange().replace(",", "-"));
        }

        JSONObject responseJSON = zlmresTfulUtils.setServerConfig(mediaServerItem, param);

        if (responseJSON != null && responseJSON.getInteger("code") == 0) {
            if (restart) {
                logger.info(LogTemplate.PROCESS_LOG_TEMPLATE, "媒体服务器节点管理服务", String.format("[ZLM] 设置成功,开始重启以保证配置生效：%s -> %s:%s", mediaServerItem.getId(), mediaServerItem.getIp(), mediaServerItem.getHttpPort()));

                zlmresTfulUtils.restartServer(mediaServerItem);
            }else {
                logger.info(LogTemplate.PROCESS_LOG_TEMPLATE, "媒体服务器节点管理服务", String.format("[ZLM] 设置成功：%s -> %s:%s", mediaServerItem.getId(), mediaServerItem.getIp(), mediaServerItem.getHttpPort()));
            }
        }else {
            logger.info(LogTemplate.PROCESS_LOG_TEMPLATE, "媒体服务器节点管理服务", String.format("[ZLM] 设置zlm失败：%s -> %s:%s", mediaServerItem.getId(), mediaServerItem.getIp(), mediaServerItem.getHttpPort()));
        }
    }


    @Override
    public MediaServerItem checkMediaServer(String ip, int port, String secret) {
        if (mediaServerMapper.queryOneByHostAndPort(ip, port) != null) {
            throw new BusinessException(BusinessErrorEnums.MEDIA_ZLM_EXSITED_ERROR);
        }
        MediaServerItem mediaServerItem = new MediaServerItem();
        mediaServerItem.setIp(ip);
        mediaServerItem.setHttpPort(port);
        mediaServerItem.setSecret(secret);
        JSONObject responseJSON = zlmresTfulUtils.getMediaServerConfig(mediaServerItem);
        if (responseJSON == null) {
            throw new BusinessException(BusinessErrorEnums.MEDIA_ZLM_COLLECT_ERROR);
        }
        JSONArray data = responseJSON.getJSONArray("data");
        ZLMServerConfig zlmServerConfig = JSON.parseObject(JSON.toJSONString(data.get(0)), ZLMServerConfig.class);
        if (zlmServerConfig == null) {
            throw new BusinessException(BusinessErrorEnums.MEDIA_ZLM_COLLECT_ERROR);
        }
        if (mediaServerMapper.queryOne(zlmServerConfig.getGeneralMediaServerId()) != null) {
            throw new BusinessException(BusinessErrorEnums.MEDIA_ZLM_EXSITED_ERROR);
        }
        mediaServerItem.setHttpSslPort(zlmServerConfig.getHttpPort());
        mediaServerItem.setRtmpPort(zlmServerConfig.getRtmpPort());
        mediaServerItem.setRtmpSslPort(zlmServerConfig.getRtmpSslPort());
        mediaServerItem.setRtspPort(zlmServerConfig.getRtspPort());
        mediaServerItem.setRtspSslPort(zlmServerConfig.getRtspSslport());
        mediaServerItem.setRtpProxyPort(zlmServerConfig.getRtpProxyPort());
        mediaServerItem.setStreamIp(ip);
        mediaServerItem.setHookIp(zlmServerConfig.getHookIp());
        mediaServerItem.setSdpIp(ip);
        return mediaServerItem;
    }

    @Override
    public boolean checkMediaRecordServer(String ip, int port) {
        boolean result = false;
        OkHttpClient client = new OkHttpClient();
        String url = String.format("http://%s:%s/index/api/record",  ip, port);
        Request request = new Request.Builder()
                .get()
                .url(url)
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response != null) {
                result = true;
            }
        } catch (Exception e) {}

        return result;
    }


    @Override
    public void deleteDb(String id){
        //同步删除数据库中的数据
        mediaServerMapper.delOne(id);
    }

    @Override
    public void updateMediaServerKeepalive(String mediaServerId, JSONObject data) {
        MediaServerItem mediaServerItem = getOne(mediaServerId);
        if (mediaServerItem == null) {
            // 缓存不存在，从数据库查询，如果数据库不存在则是错误的
            MediaServerItem mediaServerItemFromDatabase = getOneFromDatabase(mediaServerId);
            if (mediaServerItemFromDatabase == null) {
                return;
            }
            // zlm连接重试
            logger.info(LogTemplate.PROCESS_LOG_TEMPLATE, "媒体服务器节点管理服务", "[更新ZLM 保活信息]失败，未找到流媒体信息,尝试重连zlm");
//            reloadZlm();
            mediaServerItem = getOne(mediaServerId);
            if (mediaServerItem == null) {
                // zlm连接重试
                logger.info(LogTemplate.PROCESS_LOG_TEMPLATE, "媒体服务器节点管理服务", "[更新ZLM 保活信息]失败，未找到流媒体信息");

                return;
            }
        }
        final String zlmKeepaliveKey = zlmKeepaliveKeyPrefix + mediaServerItem.getId();
        dynamicTask.stop(zlmKeepaliveKey);
        dynamicTask.startDelay(zlmKeepaliveKey, new KeepAliveTimeoutRunnable(mediaServerItem), (mediaServerItem.getHookAliveInterval() + 5) * 1000);
    }

    private MediaServerItem getOneFromDatabase(String mediaServerId) {
        return mediaServerMapper.queryOne(mediaServerId);
    }



    @Override
    public boolean checkRtpServer(MediaServerItem mediaServerItem, String app, String stream) {
        JSONObject rtpInfo = zlmresTfulUtils.getRtpInfo(mediaServerItem, stream);
        if(rtpInfo.getInteger("code") == 0){
            return rtpInfo.getBoolean("exist");
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeMediaServer(String id) {

        deleteDb(id);
    }

    public StreamInfo getStreamInfoByAppAndStream(MediaServerItem mediaInfo, String app, String stream) {
        StreamInfo streamInfoResult = new StreamInfo();
        streamInfoResult.setStreamId(stream);
        String addr = mediaInfo.getStreamIp();
        streamInfoResult.setMediaServerId(mediaInfo.getId());
        streamInfoResult.setRtmp(String.format("rtmp://%s:%s/%s/%s", addr, mediaInfo.getRtmpPort(), app,  stream));
        if (mediaInfo.getRtmpSslPort() != 0) {
            streamInfoResult.setRtmps(String.format("rtmps://%s:%s/%s/%s", addr, mediaInfo.getRtmpSslPort(), app,  stream));
        }
        streamInfoResult.setRtsp(String.format("rtsp://%s:%s/%s/%s", addr, mediaInfo.getRtspPort(), app,  stream));
        if (mediaInfo.getRtspSslPort() != 0) {
            streamInfoResult.setRtsps(String.format("rtsps://%s:%s/%s/%s", addr, mediaInfo.getRtspSslPort(), app,  stream));
        }
        streamInfoResult.setHttpFlv(String.format("http://%s:%s/%s/%s.live.flv", addr, mediaInfo.getHttpPlayPort(), app,  stream));
        streamInfoResult.setWsFlv(String.format("ws://%s:%s/%s/%s.live.flv", addr, mediaInfo.getHttpPlayPort(), app,  stream));
        streamInfoResult.setHttpHls(String.format("http://%s:%s/%s/%s/hls.m3u8", addr, mediaInfo.getHttpPlayPort(), app,  stream));
        streamInfoResult.setWsHls(String.format("ws://%s:%s/%s/%s/hls.m3u8", addr, mediaInfo.getHttpPlayPort(), app,  stream));
        streamInfoResult.setHttpFmp4(String.format("http://%s:%s/%s/%s.live.mp4", addr, mediaInfo.getHttpPlayPort(), app,  stream));
        streamInfoResult.setWsFmp4(String.format("ws://%s:%s/%s/%s.live.mp4", addr, mediaInfo.getHttpPlayPort(), app,  stream));
        streamInfoResult.setHttpTs(String.format("http://%s:%s/%s/%s.live.ts", addr, mediaInfo.getHttpPlayPort(), app,  stream));
        streamInfoResult.setWsTs(String.format("ws://%s:%s/%s/%s.live.ts", addr, mediaInfo.getHttpPlayPort(), app,  stream));
        streamInfoResult.setRtc(String.format("http://%s:%s/index/api/webrtc?app=%s&stream=%s&type=play", mediaInfo.getStreamIp(), mediaInfo.getHttpPlayPort(), app,  stream));
        if (mediaInfo.getHttpSslPort() != 0) {
            streamInfoResult.setHttpsFlv(String.format("https://%s:%s/%s/%s.live.flv", addr, mediaInfo.getHttpSslPort(), app,  stream));
            streamInfoResult.setWssFlv(String.format("wss://%s:%s/%s/%s.live.flv", addr, mediaInfo.getHttpSslPort(), app,  stream));
            streamInfoResult.setHttpsHls(String.format("https://%s:%s/%s/%s/hls.m3u8", addr, mediaInfo.getHttpSslPort(), app,  stream));
            streamInfoResult.setWssHls(String.format("wss://%s:%s/%s/%s/hls.m3u8", addr, mediaInfo.getHttpSslPort(), app,  stream));
            streamInfoResult.setHttpsFmp4(String.format("https://%s:%s/%s/%s.live.mp4", addr, mediaInfo.getHttpSslPort(), app,  stream));
            streamInfoResult.setWssFmp4(String.format("wss://%s:%s/%s/%s.live.mp4", addr, mediaInfo.getHttpSslPort(), app,  stream));
            streamInfoResult.setHttpsTs(String.format("https://%s:%s/%s/%s.live.ts", addr, mediaInfo.getHttpSslPort(), app,  stream));
            streamInfoResult.setWssTs(String.format("wss://%s:%s/%s/%s.live.ts", addr, mediaInfo.getHttpSslPort(), app,  stream));
            streamInfoResult.setRtcs(String.format("https://%s:%s/index/api/webrtc?app=%s&stream=%s&type=play", mediaInfo.getStreamIp(), mediaInfo.getHttpSslPort(), app,  stream));
        }

        return streamInfoResult;
    }

    @Override
    public StreamInfo getRtpInfo(String mediaServerId, String streamId,String app) {
        MediaServerItem mediaInfo = this.getOne(mediaServerId);
        StreamInfo streamInfo = null;
        if(ObjectUtils.isEmpty(mediaInfo)){
            return null;
        }
        JSONObject rtpInfo = zlmresTfulUtils.getRtpInfo(mediaInfo, streamId);
        logger.info(LogTemplate.PROCESS_LOG_TEMPLATE, "获取流存在信息服务", rtpInfo);
        if(rtpInfo.getInteger("code") == 0){
            if (rtpInfo.getBoolean("exist")) {
                //判断是否正常
                streamInfo = getStreamInfoByAppAndStream(mediaInfo, app ,streamId);
                //流直接返回
                StreamPlayDto streamPlayResult = new StreamPlayDto();
                streamPlayResult.setStreamId(streamId);
                streamPlayResult.setIsSuccess(true);
                String businessSceneKey = GatewayMsgType.STREAM_PLAY_RESULT.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+streamId;
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.STREAM_PLAY_RESULT,BusinessErrorEnums.SUCCESS,streamPlayResult);
            }
        }
        return streamInfo;

    }

    @Override
    public void streamBye(String streamId, String msgId) {

        //缓存bye相关的请求参数--1分钟  用作异常断流的判断
        RedisCommonUtil.set(redisTemplate,VideoManagerConstants.MEDIA_STREAM_BYE+ BusinessSceneConstants.SCENE_SEM_KEY+streamId,streamId,60);
        //通知网关进行bye请求的发送
        BaseRtpServerDto baseRtpServerDto = (BaseRtpServerDto)RedisCommonUtil.get(redisTemplate, VideoManagerConstants.MEDIA_RTP_SERVER_REQ + BusinessSceneConstants.SCENE_SEM_KEY + streamId);
        if(ObjectUtils.isEmpty(baseRtpServerDto)){
            logger.error(LogTemplate.ERROR_LOG_TEMPLATE,"流停止请求","停止失败,流的缓存信息不存在",streamId);
            return;
        }
        CommonMqDto byeMqInfo = redisCatchStorageService.getMqInfo(GatewayMsgType.STOP_PLAY.getTypeName(), GatewayCacheConstants.DISPATCHER_BUSINESS_SN_INCR, GatewayCacheConstants.GATEWAY_BUSINESS_SN_prefix,msgId);
        StreamPlayDto streamPlayDto = new StreamPlayDto();
        streamPlayDto.setStreamId(streamId);
        byeMqInfo.setData(streamPlayDto);
        GatewayBindReq gatewayBindReq = baseRtpServerDto.getGatewayBindReq();
        rabbitMqSender.sendMsgByExchange(gatewayBindReq.getMqExchange(), gatewayBindReq.getMqRouteKey(), UuidUtil.toUuid(),byeMqInfo,true);

    }

    @Override
    public void streamStop(String streamId, String msgId) {
        MediaServerItem defaultMediaServer = getDefaultMediaServer();
        //查看流是否存在
        JSONObject rtpInfo = zlmresTfulUtils.getRtpInfo(defaultMediaServer, streamId);
        logger.info(LogTemplate.PROCESS_LOG_TEMPLATE, "bye之前先获取流是否存在", rtpInfo);
        CommonMqDto mqInfo = redisCatchStorageService.getMqInfo(GatewayMsgType.STREAM_PLAY_STOP.getTypeName(), GatewayCacheConstants.DISPATCHER_BUSINESS_SN_INCR, GatewayCacheConstants.GATEWAY_BUSINESS_SN_prefix,msgId);
        mqInfo.setData(false);
        if(rtpInfo.getInteger("code") == 0){
            if (!rtpInfo.getBoolean("exist")) {
                //流不存在 通知调度中心可以关闭
                mqInfo.setData(true);
            }
        }
        //查看是否有人观看
        int i = zlmrtpServerFactory.totalReaderCount(defaultMediaServer, VideoManagerConstants.GB28181_APP, streamId);
        //通知调度中心 进行bye场景的控制
        if(i>0){
            //不允许关闭
            rabbitMqSender.sendMsgByExchange(dispatcherSignInConf.getMqExchange(), dispatcherSignInConf.getMqSetQueue(), UuidUtil.toUuid(),mqInfo,true);
            return;
        }else {
            mqInfo.setData(true);
            rabbitMqSender.sendMsgByExchange(dispatcherSignInConf.getMqExchange(), dispatcherSignInConf.getMqSetQueue(), UuidUtil.toUuid(),mqInfo,true);
        }
        streamBye(streamId,msgId);

    }

    @Override
    public Boolean streamNotify(String streamId) {
        logger.info(LogTemplate.PROCESS_LOG_TEMPLATE, "点播流通知", streamId);
        StreamPlayDto streamPlayResult = new StreamPlayDto();
        streamPlayResult.setStreamId(streamId);
        String businessSceneKey = GatewayMsgType.STREAM_PLAY_RESULT.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+streamId;
        redisCatchStorageService.addBusinessSceneKey(businessSceneKey,GatewayMsgType.STREAM_PLAY_RESULT,null);

        return Boolean.TRUE;
    }
    @Override
    public List<OnlineStreamsEntity> streamListByStreamIds(StreamCheckListResp streamCheckListResp, String msgId) {
        List<String> streamLists = streamCheckListResp.getStreamIdList();
        if(CollectionUtils.isEmpty(streamLists)){
            logger.info(LogTemplate.PROCESS_LOG_TEMPLATE, "流检查为空", streamCheckListResp);
            return null;
        }
        LocalDateTime checkTime = streamCheckListResp.getCheckTime();
        //获取数据库中的数据
        List<OnlineStreamsEntity> onlineStreamsEntities = onlineStreamsService.streamListByCheckTime(streamLists,checkTime);
        List<String> collect = onlineStreamsEntities.stream().map(OnlineStreamsEntity::getStreamId).collect(Collectors.toList());
        //获取差集
        List<String> streamListsBye = new ArrayList<>();
        collect.forEach(streamId->{
            if(!streamLists.contains(streamId)){
                //脏数据或则遗留数据，进行流的bye 关闭
                streamBye(streamId,null);
            }
        });
        //业务队列发送流的列表
        CommonMqDto mqinfo = redisCatchStorageService.getMqInfo(GatewayMsgType.STREAM_CHECK_STREAM.getTypeName(), GatewayCacheConstants.DISPATCHER_BUSINESS_SN_INCR, GatewayCacheConstants.GATEWAY_BUSINESS_SN_prefix,msgId);
        mqinfo.setData(collect);
        rabbitMqSender.sendMsgByExchange(dispatcherSignInConf.getMqExchange(), dispatcherSignInConf.getMqSetQueue(), UuidUtil.toUuid(),mqinfo,true);

        return onlineStreamsEntities;
    }

    @Override
    public void streamStopAll() {
        //删除全部的当前的流列表数据
        List<OnlineStreamsEntity> onlineStreamsEntities = onlineStreamsService.streamAll();

        if(!CollectionUtils.isEmpty(onlineStreamsEntities)){
            onlineStreamsEntities.forEach(onlineStreamsEntity -> {
                streamBye(onlineStreamsEntity.getStreamId(),null);
            });
        }

    }
}
