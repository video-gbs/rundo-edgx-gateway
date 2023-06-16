package com.runjian.media.manager.controller.hook;

/**
 * hook类型
 * @author lin
 */

public enum HookType {

    on_flow_report,
    on_http_access,
    on_play,
    on_publish,
    on_record_mp4,
    on_rtsp_auth,
    on_rtsp_realm,
    on_shell_login,
    on_stream_arrive,
    on_stream_disconnect,
    on_stream_none_reader,
    on_stream_not_found,
    on_server_started,
    on_server_keepalive,
    on_rtp_server_timeout
}
