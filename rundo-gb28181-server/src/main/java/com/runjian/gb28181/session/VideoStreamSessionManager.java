package com.runjian.gb28181.session;

import com.runjian.common.constant.LogTemplate;
import com.runjian.common.constant.MarkConstant;
import com.runjian.common.constant.VideoManagerConstants;
import com.runjian.common.utils.redis.RedisCommonUtil;
import com.runjian.gb28181.bean.SipTransactionInfo;
import com.runjian.gb28181.bean.SsrcTransaction;
import gov.nist.javax.sip.message.SIPResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.List;

/**    
 * @description:视频流session管理器，管理视频预览、预览回放的通信句柄 
 * @author: swwheihei
 * @date:   2020年5月13日 下午4:03:02     
 */
@Component
public class VideoStreamSessionManager {


	@Autowired
	RedisTemplate redisTemplate;

	public enum SessionType {
		play,
		playback,
		download
	}

	private final Logger logger = LoggerFactory.getLogger(VideoStreamSessionManager.class);

	/**
	 * 添加一个点播/回放的事务信息
	 * 后续可以通过流Id/callID
	 * @param deviceId 设备ID
	 * @param channelId 通道ID
	 * @param callId 一次请求的CallID
	 * @param stream 流名称
	 * @param mediaServerId 所使用的流媒体ID
	 * @param response 回复
	 */
	public void putSsrcTransaction(String deviceId, String channelId, String callId, String stream, String ssrc, String mediaServerId, SIPResponse response, SessionType type,String dispatchUrl){
		SsrcTransaction ssrcTransaction = new SsrcTransaction();
		ssrcTransaction.setDeviceId(deviceId);
		ssrcTransaction.setChannelId(channelId);
		ssrcTransaction.setStream(stream);
		ssrcTransaction.setSipTransactionInfo(new SipTransactionInfo(response));
		ssrcTransaction.setCallId(callId);
		ssrcTransaction.setSsrc(ssrc);
		ssrcTransaction.setMediaServerId(mediaServerId);
		ssrcTransaction.setType(type);
		ssrcTransaction.setDispatchUrl(dispatchUrl);

		RedisCommonUtil.set(redisTemplate,VideoManagerConstants.MEDIA_TRANSACTION_USED_PREFIX + MarkConstant.MARK_SPLIT_RAIL +  deviceId + MarkConstant.MARK_SPLIT_RAIL + channelId + MarkConstant.MARK_SPLIT_RAIL + callId + MarkConstant.MARK_SPLIT_RAIL + stream, ssrcTransaction);
	}

	/**
	 * 获取点播相关的视频流信息
	 * @param deviceId
	 * @param channelId
	 * @param callId
	 * @param stream
	 * @return
	 */
	public SsrcTransaction getSsrcTransaction(String deviceId, String channelId, String callId, String stream){

		if (ObjectUtils.isEmpty(deviceId)) {
			deviceId ="*";
		}
		if (ObjectUtils.isEmpty(channelId)) {
			channelId ="*";
		}
		if (ObjectUtils.isEmpty(callId)) {
			callId ="*";
		}
		if (ObjectUtils.isEmpty(stream)) {
			stream ="*";
		}
		String key = VideoManagerConstants.MEDIA_TRANSACTION_USED_PREFIX + MarkConstant.MARK_SPLIT_RAIL +  deviceId + MarkConstant.MARK_SPLIT_RAIL + channelId + MarkConstant.MARK_SPLIT_RAIL + callId + MarkConstant.MARK_SPLIT_RAIL + stream;
		List<Object> scanResult = RedisCommonUtil.scan(redisTemplate,key);
		if (scanResult.size() == 0) {
			return null;
		}
		return (SsrcTransaction)RedisCommonUtil.get(redisTemplate, (String) scanResult.get(0));
	}

	public void removeSsrcTransaction(String deviceId, String channelId, String stream) {
		SsrcTransaction ssrcTransaction = getSsrcTransaction(deviceId, channelId, null, stream);
		logger.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "视频流session管理器", "删除设备会话", ssrcTransaction);
		if (ssrcTransaction == null) {
			return;
		}
		RedisCommonUtil.del(redisTemplate,VideoManagerConstants.MEDIA_TRANSACTION_USED_PREFIX,VideoManagerConstants.MEDIA_TRANSACTION_USED_PREFIX + MarkConstant.MARK_SPLIT_RAIL +  deviceId + MarkConstant.MARK_SPLIT_RAIL + channelId + MarkConstant.MARK_SPLIT_RAIL + ssrcTransaction.getCallId() + MarkConstant.MARK_SPLIT_RAIL + stream);
	}

	public void removeSsrcTransaction(SsrcTransaction ssrcTransaction) {
		RedisCommonUtil.del(redisTemplate,VideoManagerConstants.MEDIA_TRANSACTION_USED_PREFIX,VideoManagerConstants.MEDIA_TRANSACTION_USED_PREFIX + MarkConstant.MARK_SPLIT_RAIL +  ssrcTransaction.getDeviceId() + MarkConstant.MARK_SPLIT_RAIL + ssrcTransaction.getChannelId() + MarkConstant.MARK_SPLIT_RAIL + ssrcTransaction.getCallId() + MarkConstant.MARK_SPLIT_RAIL + ssrcTransaction.getStream());
	}
}
