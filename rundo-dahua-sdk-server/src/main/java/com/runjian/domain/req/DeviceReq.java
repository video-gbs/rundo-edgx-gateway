package com.runjian.domain.req;

import lombok.Data;

@Data
public class DeviceReq {
    String ip;
    int port;
    String user;
    String psw;

}
