//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.alibaba.dingpaas.live;

public final class PlayUrlModel {
    public int codeLevel = 0;
    public String flvUrl = "";
    public String hlsUrl = "";
    public String rtmpUrl = "";

    public PlayUrlModel(int codeLevel, String flvUrl, String hlsUrl, String rtmpUrl) {
        this.codeLevel = codeLevel;
        this.flvUrl = flvUrl;
        this.hlsUrl = hlsUrl;
        this.rtmpUrl = rtmpUrl;
    }

    public PlayUrlModel() {
    }

    public int getCodeLevel() {
        return this.codeLevel;
    }

    public String getFlvUrl() {
        return this.flvUrl;
    }

    public String getHlsUrl() {
        return this.hlsUrl;
    }

    public String getRtmpUrl() {
        return this.rtmpUrl;
    }

    public String toString() {
        return "PlayUrlModel{codeLevel=" + this.codeLevel + ",flvUrl=" + this.flvUrl + ",hlsUrl=" + this.hlsUrl + ",rtmpUrl=" + this.rtmpUrl + "}";
    }
}

