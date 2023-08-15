package com.runjian.domain.req;

import lombok.Data;

@Data
public class DeviceReq {
    String ip;
    short port;
    String user;
    String psw;

}
