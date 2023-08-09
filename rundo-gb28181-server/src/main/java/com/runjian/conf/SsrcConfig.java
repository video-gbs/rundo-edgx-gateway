package com.runjian.conf;

import com.runjian.common.constant.ConfigConst;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Schema(description = "ssrc信息")
public class SsrcConfig implements Serializable {


    @Schema(description = "SSRC前缀")
    private String ssrcPrefix;

    /**
     * zlm流媒体服务器已用会话句柄
     */
    @Schema(description = "zlm流媒体服务器已用会话句柄")
    private List<String> isUsed;

    /**
     * zlm流媒体服务器可用会话句柄
     */
    @Schema(description = "zlm流媒体服务器可用会话句柄")
    private List<String> notUsed;

    public SsrcConfig() {
    }

    public SsrcConfig(Set<String> usedSet, String sipDomain) {
        this.isUsed = new ArrayList<>();
        this.ssrcPrefix = sipDomain.substring(3, 8);
        this.notUsed = new ArrayList<>();
        for (int i = 1; i < ConfigConst.MAX_STRTEAM_COUNT; i++) {
            String ssrc;
            if (i < 10) {
                ssrc = "000" + i;
            } else if (i < 100) {
                ssrc = "00" + i;
            } else if (i < 1000) {
                ssrc = "0" + i;
            } else {
                ssrc = String.valueOf(i);
            }
            if (null == usedSet || !usedSet.contains(ssrc)) {
                this.notUsed.add(ssrc);
            } else {
                this.isUsed.add(ssrc);
            }
        }
    }

    private String playSsrc;

    private String playBackSsrc;

    private String sn;

    /**
     * 获取视频预览的SSRC值,第一位固定为0
     * @return ssrc
     */
    public String getPlaySsrc() {
        return "0" + getSsrcPrefix() + getSn();
    }

    /**
     * 获取录像回放的SSRC值,第一位固定为1
     *
     */
    public String getPlayBackSsrc() {
        return "1" + getSsrcPrefix() + getSn();
    }

    /**
     * 释放ssrc，主要用完的ssrc一定要释放，否则会耗尽
     * @param ssrc 需要重置的ssrc
     */
    public Boolean releaseSsrc(String ssrc) {
        Boolean result;
        if (ssrc == null) {
            result =  false;
            return result;
        }
        String sn = ssrc.substring(6);
        try {
            //判断一下isUsed中是否有该参数
            if(isUsed.contains(sn)){
                isUsed.remove(sn);
                notUsed.add(sn);
                result = true;
            }else {
                result = false;
            }

        }catch (NullPointerException e){
            result = false;
        }
        return result;
    }

    /**
     * 获取后四位数SN,随机数
     *
     */
    private String getSn() {
        String sn = null;
        int index = 0;
        if (notUsed.size() == 0) {
            throw new RuntimeException("ssrc已经用完");
        } else if (notUsed.size() == 1) {
            sn = notUsed.get(0);
        } else {
            index = new Random().nextInt(notUsed.size() - 1);
            sn = notUsed.get(index);
        }
        notUsed.remove(index);
        isUsed.add(sn);
        return sn;
    }

    public String getSsrcPrefix() {
        return ssrcPrefix;
    }

    public void setSsrcPrefix(String ssrcPrefix) {
        this.ssrcPrefix = ssrcPrefix;
    }

    public List<String> getIsUsed() {
        return isUsed;
    }

    public void setIsUsed(List<String> isUsed) {
        this.isUsed = isUsed;
    }

    public List<String> getNotUsed() {
        return notUsed;
    }

    public void setNotUsed(List<String> notUsed) {
        this.notUsed = notUsed;
    }

    public boolean checkSsrc(String ssrcInResponse) {
        return !isUsed.contains(ssrcInResponse);
    }


    public void setPlaySsrc(String playSsrc) {
        this.playSsrc = playSsrc;
    }

    public void setPlayBackSsrc(String playBackSsrc) {
        this.playBackSsrc = playBackSsrc;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }
}
