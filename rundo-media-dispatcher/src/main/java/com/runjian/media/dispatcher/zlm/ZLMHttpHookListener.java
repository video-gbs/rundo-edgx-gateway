package com.runjian.media.dispatcher.zlm;

import java.util.*;
import java.text.ParseException;
import com.alibaba.fastjson.JSON;
import com.runjian.common.commonDto.Gateway.req.NoneStreamReaderReq;
import com.runjian.common.commonDto.Gb28181Media.BaseRtpServerDto;
import com.runjian.common.constant.*;
import com.runjian.common.mq.RabbitMqSender;
import com.runjian.common.mq.domain.GatewayMqDto;
import com.runjian.common.utils.UuidUtil;
import com.runjian.common.utils.redis.RedisCommonUtil;
import com.runjian.media.dispatcher.conf.UserSetting;
import com.runjian.media.dispatcher.zlm.dto.*;
import com.runjian.media.dispatcher.zlm.dto.dao.GatewayBind;
import com.runjian.media.dispatcher.zlm.dto.hook.OnRtpServerTimeoutHookParam;
import com.runjian.media.dispatcher.zlm.event.publisher.EventPublisher;
import com.runjian.media.dispatcher.zlm.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import com.alibaba.fastjson.JSONObject;
import javax.servlet.http.HttpServletRequest;


/**    
 * @description:针对 ZLMediaServer的hook事件监听
 * @author: swwheihei
 * @date:   2020年5月8日 上午10:46:48     
 */
@RestController
@RequestMapping("/index/hook")
public class ZLMHttpHookListener {

	private final static Logger logger = LoggerFactory.getLogger(ZLMHttpHookListener.class);


	@Autowired
	private ImediaServerService mediaServerService;


	@Autowired
	private ZlmHttpHookSubscribe subscribe;

	@Autowired
	private UserSetting userSetting;

	@Autowired
	IRedisCatchStorageService redisCatchStorageService;
	@Autowired
	RabbitMqSender rabbitMqSender;

	@Autowired
	IGatewayBindService gatewayBindService;

	@Qualifier("taskExecutor")
	@Autowired
	private ThreadPoolTaskExecutor taskExecutor;


	@Autowired
	private RedisTemplate redisTemplate;
	/**
	 * 服务器定时上报时间，上报间隔可配置，默认10s上报一次
	 *
	 */
	@ResponseBody
	@PostMapping(value = "/on_server_keepalive", produces = "application/json;charset=UTF-8")
	public JSONObject onServerKeepalive(@RequestBody JSONObject json){

//		logger.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "ZLM HOOK", "on_server_keepalive API调用", json);
		String mediaServerId = json.getString("mediaServerId");
		List<ZlmHttpHookSubscribe.Event> subscribes = this.subscribe.getSubscribes(HookType.on_server_keepalive);
		if (subscribes != null  && subscribes.size() > 0) {
			for (ZlmHttpHookSubscribe.Event subscribe : subscribes) {
				subscribe.response(null, json);
			}
		}
		mediaServerService.updateMediaServerKeepalive(mediaServerId, json.getJSONObject("data"));

		JSONObject ret = new JSONObject();
		ret.put("code", 0);
		ret.put("msg", "success");

		return ret;
	}

	/**
	 * 流量统计事件，播放器或推流器断开时并且耗用流量超过特定阈值时会触发此事件，阈值通过配置文件general.flowThreshold配置；此事件对回复不敏感。
	 *  
	 */
	@ResponseBody
	@PostMapping(value = "/on_flow_report", produces = "application/json;charset=UTF-8")
	public JSONObject onFlowReport(@RequestBody JSONObject json){
		
		logger.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "ZLM HOOK", "on_flow_report API调用", json);
		JSONObject ret = new JSONObject();
		ret.put("code", 0);
		ret.put("msg", "success");
		return ret;
	}
	
	/**
	 * 访问http文件服务器上hls之外的文件时触发。
	 *  
	 */
	@ResponseBody
	@PostMapping(value = "/on_http_access", produces = "application/json;charset=UTF-8")
	public JSONObject onHttpAccess(@RequestBody JSONObject json){
		
		logger.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "ZLM HOOK", "on_http_access API 调用", json);
		String mediaServerId = json.getString("mediaServerId");
		JSONObject ret = new JSONObject();
		ret.put("code", 0);
		ret.put("err", "");
		ret.put("path", "");
		ret.put("second", 600);
		return ret;
	}
	
	/**
	 * 播放器鉴权事件，rtsp/rtmp/http-flv/ws-flv/hls的播放都将触发此鉴权事件。
	 *  
	 */
	@ResponseBody
	@PostMapping(value = "/on_play", produces = "application/json;charset=UTF-8")
	public JSONObject onPlay(@RequestBody OnPlayHookParam param){

		JSONObject json = (JSONObject)JSON.toJSON(param);
		logger.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "ZLM HOOK", "on_play API调用", JSON.toJSONString(param));
		String mediaServerId = param.getMediaServerId();
		ZlmHttpHookSubscribe.Event subscribe = this.subscribe.sendNotify(HookType.on_play, json);
		if (subscribe != null ) {
			MediaServerItem mediaInfo = mediaServerService.getOne(mediaServerId);
			if (mediaInfo != null) {
				subscribe.response(mediaInfo, json);
			}
		}
		JSONObject ret = new JSONObject();
		if (!"rtp".equals(param.getApp())) {
			Map<String, String> paramMap = urlParamToMap(param.getParams());

			return ret;

		}

		ret.put("code", 0);
		ret.put("msg", "success");
		return ret;
	}
	
	/**
	 * rtsp/rtmp/rtp推流鉴权事件。
	 *  
	 */
	@ResponseBody
	@PostMapping(value = "/on_publish", produces = "application/json;charset=UTF-8")
	public JSONObject onPublish(@RequestBody OnPublishHookParam param) {

		JSONObject json = (JSONObject) JSON.toJSON(param);
		logger.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "ZLM HOOK", "on_publish API调用", json);
		JSONObject ret = new JSONObject();
		String mediaServerId = json.getString("mediaServerId");
		MediaServerItem mediaInfo = mediaServerService.getOne(mediaServerId);
		String app = json.getString("app");
		String stream = json.getString("stream");
		ret.put("code", 0);
		ret.put("msg", "success");
		ret.put("enable_hls", true);

		//todo 判断是否录制mp4 以及是否开启音频
		BaseRtpServerDto baseRtpServerDto = (BaseRtpServerDto)RedisCommonUtil.get(redisTemplate, VideoManagerConstants.MEDIA_RTP_SERVER_REQ+BusinessSceneConstants.SCENE_SEM_KEY+stream);
		if(ObjectUtils.isEmpty(baseRtpServerDto)){
			//缓存不存在或则推流超时了
			logger.error(LogTemplate.ERROR_LOG_TEMPLATE,"on_publish API调用","rtpserver信息缓存不存在",json);
			ret.put("code", 1);
			ret.put("msg", "rtpServer not exists");
		}else {
			//正常推流，判断是否开启音频
			ret.put("enable_audio", baseRtpServerDto.getEnableAudio());
		}
		if ("rtp".equals(app)) {

			//todo 判断是否要进行级联推流
			//zlmMediaListManager.sendStreamEvent(param.getApp(),param.getStream(), param.getMediaServerId());
		}else {
			//todo 非国标的推流鉴权

		}
		return ret;
	}



	/**
	 * 录制mp4完成后通知事件；此事件对回复不敏感。
	 *  
	 */
	@ResponseBody
	@PostMapping(value = "/on_record_mp4", produces = "application/json;charset=UTF-8")
	public JSONObject onRecordMp4(@RequestBody JSONObject json){
		
		logger.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "ZLM HOOK", "on_record_mp4 API调用", json);
		String mediaServerId = json.getString("mediaServerId");
		JSONObject ret = new JSONObject();
		ret.put("code", 0);
		ret.put("msg", "success");
		return ret;
	}
	/**
	 * 录制hls完成后通知事件；此事件对回复不敏感。
	 *
	 */
	@ResponseBody
	@PostMapping(value = "/on_record_ts", produces = "application/json;charset=UTF-8")
	public JSONObject onRecordTs(@RequestBody JSONObject json){

		logger.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "ZLM HOOK", "on_record_ts API调用", json);
		JSONObject ret = new JSONObject();
		ret.put("code", 0);
		ret.put("msg", "success");
		return ret;
	}
	
	/**
	 * rtsp专用的鉴权事件，先触发on_rtsp_realm事件然后才会触发on_rtsp_auth事件。
	 *  
	 */
	@ResponseBody
	@PostMapping(value = "/on_rtsp_realm", produces = "application/json;charset=UTF-8")
	public JSONObject onRtspRealm(@RequestBody JSONObject json){
		
		logger.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "ZLM HOOK", "on_rtsp_realm API调用", json);
		JSONObject ret = new JSONObject();
		ret.put("code", 0);
		ret.put("realm", "");
		return ret;
	}
	
	
	/**
	 * 该rtsp流是否开启rtsp专用方式的鉴权事件，开启后才会触发on_rtsp_auth事件。需要指出的是rtsp也支持url参数鉴权，它支持两种方式鉴权。
	 *  
	 */
	@ResponseBody
	@PostMapping(value = "/on_rtsp_auth", produces = "application/json;charset=UTF-8")
	public JSONObject onRtspAuth(@RequestBody JSONObject json){
		
		logger.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "ZLM HOOK", "on_rtsp_auth API调用", json);
		String mediaServerId = json.getString("mediaServerId");
		JSONObject ret = new JSONObject();
		ret.put("code", 0);
		ret.put("encrypted", false);
		ret.put("passwd", "test");
		return ret;
	}
	
	/**
	 * shell登录鉴权，ZLMediaKit提供简单的telnet调试方式，使用telnet 127.0.0.1 9000能进入MediaServer进程的shell界面。
	 *  
	 */
	@ResponseBody
	@PostMapping(value = "/on_shell_login", produces = "application/json;charset=UTF-8")
	public JSONObject onShellLogin(@RequestBody JSONObject json){
		
		logger.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "ZLM HOOK", "on_shell_login API调用", json);
		String mediaServerId = json.getString("mediaServerId");
		JSONObject ret = new JSONObject();
		ret.put("code", 0);
		ret.put("msg", "success");
		return ret;
	}
	
	/**
	 * rtsp/rtmp流注册或注销时触发此事件；此事件对回复不敏感。
	 *  
	 */
	@ResponseBody
	@PostMapping(value = "/on_stream_changed", produces = "application/json;charset=UTF-8")
	public JSONObject onStreamChanged(@RequestBody MediaItem item){
		logger.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "ZLM HOOK", "on_stream_changed API调用", JSONObject.toJSONString(item));
		String mediaServerId = item.getMediaServerId();
		JSONObject json = (JSONObject) JSON.toJSON(item);
		ZlmHttpHookSubscribe.Event subscribe = this.subscribe.sendNotify(HookType.on_stream_changed, json);
		if (subscribe != null ) {
			//返回订阅的信息
			MediaServerItem mediaInfo = mediaServerService.getOne(mediaServerId);
			if (mediaInfo != null) {
				subscribe.response(mediaInfo, json);
			}
		}

		JSONObject ret = new JSONObject();
		ret.put("code", 0);
		ret.put("msg", "success");
		return ret;
	}
	
	/**
	 * 流无人观看时事件，用户可以通过此事件选择是否关闭无人看的流。
	 *  
	 */
	@ResponseBody
	@PostMapping(value = "/on_stream_none_reader", produces = "application/json;charset=UTF-8")
	public JSONObject onStreamNoneReader(@RequestBody JSONObject json){
		logger.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "ZLM HOOK", "on_stream_none_reader API调用", json);
		String mediaServerId = json.getString("mediaServerId");
		String streamId = json.getString("stream");
		String app = json.getString("app");
		JSONObject ret = new JSONObject();
		ret.put("code", 0);
		ret.put("close", userSetting.getStreamOnDemand());
		if (VideoManagerConstants.GB28181_APP.equals(app)){
			// 国标流， 点播/录像回放/录像下载

		}else {
			// 非国标流 推流/拉流代理
			// 拉流代理

		}
		//todo 根据上层的判断进行是否进行无人观看的拉流关闭
		NoneStreamReaderReq noneStreamReaderReq = new NoneStreamReaderReq();
		noneStreamReaderReq.setApp(app);
		noneStreamReaderReq.setMediaServerId(mediaServerId);
		noneStreamReaderReq.setSchema(json.getString("schema"));
		noneStreamReaderReq.setStreamId(streamId);
		//获取绑定的网关信息 根据流媒体id
		GatewayBind gatewayBind = gatewayBindService.findOneByMediaId(mediaServerId);
		if(!ObjectUtils.isEmpty(gatewayBind)){
			GatewayMqDto mqInfo = redisCatchStorageService.getMqInfo(GatewayMsgType.PLAY_NONE_STREAM_READER_CALLBACK.getTypeName(), GatewayCacheConstants.GATEWAY_BUSINESS_SN_INCR, GatewayCacheConstants.GATEWAY_BUSINESS_SN_prefix,null, gatewayBind.getGatewayId());
			mqInfo.setData(noneStreamReaderReq);
			logger.info("流无人观看调用={}",mqInfo);
			rabbitMqSender.sendMsgByExchange(gatewayBind.getMqExchange(), gatewayBind.getMqRouteKey(), UuidUtil.toUuid(),mqInfo,true);
		}

		return ret;
	}
	
	/**
	 * 流未找到事件，用户可以在此事件触发时，立即去拉流，这样可以实现按需拉流；此事件对回复不敏感。
	 *  
	 */
	@ResponseBody
	@PostMapping(value = "/on_stream_not_found", produces = "application/json;charset=UTF-8")
	public JSONObject onStreamNotFound(@RequestBody JSONObject json){
		//暂不处理 针对流未点播，但是用户却要拉流观看的场景
		logger.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "ZLM HOOK", "on_stream_not_found API调用",  json);
		String mediaServerId = json.getString("mediaServerId");
		MediaServerItem mediaInfo = mediaServerService.getOne(mediaServerId);

		JSONObject ret = new JSONObject();
		ret.put("code", 0);
		ret.put("msg", "success");
		return ret;
	}
	
	/**
	 * 服务器启动事件，可以用于监听服务器崩溃重启；此事件对回复不敏感。
	 *  
	 */
	@ResponseBody
	@PostMapping(value = "/on_server_started", produces = "application/json;charset=UTF-8")
	public JSONObject onServerStarted(HttpServletRequest request, @RequestBody JSONObject jsonObject){

		if (logger.isDebugEnabled()) {
			logger.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "ZLM HOOK", "on_server_started API调用",  jsonObject);
		}
		String remoteAddr = request.getRemoteAddr();
		jsonObject.put("ip", remoteAddr);
		List<ZlmHttpHookSubscribe.Event> subscribes = this.subscribe.getSubscribes(HookType.on_server_started);
		if (subscribes != null  && subscribes.size() > 0) {
			for (ZlmHttpHookSubscribe.Event subscribe : subscribes) {
				subscribe.response(null, jsonObject);
			}
		}

		ZLMServerConfig zlmServerConfig = JSONObject.toJavaObject(jsonObject, ZLMServerConfig.class);
		if (zlmServerConfig !=null ) {
			//进行zlm流媒体上线
			mediaServerService.zlmServerOnline(zlmServerConfig);
		}
		JSONObject ret = new JSONObject();
		ret.put("code", 0);
		ret.put("msg", "success");
		return ret;
	}

	/**
	 * 发送rtp(startSendRtp)被动关闭时回调
	 */
	@ResponseBody
	@PostMapping(value = "/on_send_rtp_stopped", produces = "application/json;charset=UTF-8")
	public JSONObject onSendRtpStopped(HttpServletRequest request, @RequestBody JSONObject jsonObject){

		logger.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "ZLM HOOK", "on_send_rtp_stopped API调用",  jsonObject);

		JSONObject ret = new JSONObject();
		ret.put("code", 0);
		ret.put("msg", "success");

		// 查找对应的上级推流，发送停止
		String app = jsonObject.getString("app");
		if (!"rtp".equals(app)) {
			return ret;
		}
		String stream = jsonObject.getString("stream");
		//todo 处理国标级联中的关闭流媒体推流的场景


		return ret;
	}

	/**
	 * rtpServer收流超时
	 */
	@ResponseBody
	@PostMapping(value = "/on_rtp_server_timeout", produces = "application/json;charset=UTF-8")
	public JSONObject onRtpServerTimeout(HttpServletRequest request, @RequestBody OnRtpServerTimeoutHookParam param){
		logger.info("[ZLM HOOK] rtpServer收流超时：{}->{}({})", param.getMediaServerId(), param.getStream_id(), param.getSsrc());

		JSONObject ret = new JSONObject();
		ret.put("code", 0);
		ret.put("msg", "success");
		//todo 针对zlm中的非国标一致的端口进行的推流
		taskExecutor.execute(()->{
			JSONObject json = (JSONObject) JSON.toJSON(param);
			List<ZlmHttpHookSubscribe.Event> subscribes = this.subscribe.getSubscribes(HookType.on_rtp_server_timeout);
			if (subscribes != null  && subscribes.size() > 0) {
				for (ZlmHttpHookSubscribe.Event subscribe : subscribes) {
					subscribe.response(null, json);
				}
			}
		});

		return ret;
	}

	private Map<String, String> urlParamToMap(String params) {
		HashMap<String, String> map = new HashMap<>();
		if (ObjectUtils.isEmpty(params)) {
			return map;
		}
		String[] paramsArray = params.split("&");
		if (paramsArray.length == 0) {
			return map;
		}
		for (String param : paramsArray) {
			String[] paramArray = param.split("=");
			if (paramArray.length == 2){
				map.put(paramArray[0], paramArray[1]);
			}
		}
		return map;
	}
}
