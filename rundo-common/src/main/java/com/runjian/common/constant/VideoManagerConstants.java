package com.runjian.common.constant;

/**    
 * @description: 定义常量   
 * @author: swwheihei
 * @date:   2019年5月30日 下午3:04:04   
 *   
 */
public class VideoManagerConstants {

	/** redis key相关 start **/
	public static final String SIP_CSEQ_PREFIX = "CMS_SIP_CSEQ";

	/** redis key相关 end **/


	/***zlm相关 start  暂时不考虑缓存的问题****/
	public static final String MEDIA_SERVER_PREFIX = "ZLM_MEDIA_SERVER_";

	public static final String MEDIA_SERVERS_ONLINE_PREFIX = "ZLM_MEDIA_ONLINE_SERVERS_";

	public static final String MEDIA_STREAM_PREFIX = "ZLM_MEDIA_STREAM";

	public static final String MEDIA_TRANSACTION_USED_PREFIX = "ZLM_MEDIA_TRANSACTION_";


	public static final String MEDIA_RTP_SERVER_REQ = "ZLM_RTP_SERVER_REQ_";

	public static final String GB28181_APP = "rtp";

	public static final String GB28181_SCHEAM = "rtsp";




	/**zlm相关  end ****/

	/***ssrc缓存  start****/

	public static final String SSRC_CACHE_KEY = "SSRC_CACHE_KEY_";
	/***ssrc缓存  end****/
}
