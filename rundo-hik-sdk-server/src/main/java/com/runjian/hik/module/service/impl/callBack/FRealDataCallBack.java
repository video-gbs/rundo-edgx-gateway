package com.runjian.hik.module.service.impl.callBack;

import com.runjian.common.constant.LogTemplate;
import com.runjian.conf.PlayHandleConf;
import com.runjian.hik.sdklib.HCNetSDK;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.ByteByReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.ObjectInput;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * @author chenjialing
 */
@Service
@Slf4j
public class FRealDataCallBack implements HCNetSDK.FRealDataCallBack_V30{

    @Autowired
    private PlayHandleConf playHandleConf;
    /**
     * 点播回调
     */
    //预览回调
    @Override
    public void invoke(int lRealHandle, int dwDataType, ByteByReference pBuffer, int dwBufSize, Pointer pUser) {
        switch (dwDataType) {
            case HCNetSDK.NET_DVR_SYSHEAD: //系统头

                break;
            case HCNetSDK.NET_DVR_STREAMDATA:   //码流数据
                if (dwBufSize > 0) {
                    try {


                        ByteBuffer byteBuffer = pBuffer.getPointer().getByteBuffer(0, dwBufSize);
                        byte[] bytes = new byte[byteBuffer.remaining()];
                        byteBuffer.get(bytes, 0, bytes.length);

                        Socket socket = (Socket) playHandleConf.getSocketHanderMap().get(lRealHandle);
                        if(ObjectUtils.isEmpty(socket)){
                            log.info(LogTemplate.PROCESS_LOG_TEMPLATE,"码流回调","连接暂时超时");
                            return;
                        }
                        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                        dataOutputStream.write(bytes);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        }
    }
}
