//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.alibaba.dingpaas.live;

import java.util.ArrayList;

public final class LiveInfo {
    public String anchorId = "";
    public String liveId = "";
    public String title = "";
    public String playUrl = "";
    public long createDate = 0L;
    public long endDate = 0L;
    public long preStartDate = 0L;
    public long preEndDate = 0L;
    public long duration = 0L;
    public String pushUrl = "";
    public String liveUrl = "";
    public int status = 0;
    public String introduction = "";
    public int codeLevel = 0;
    public ArrayList<PlayUrlModel> playUrlList;
    public String hlsUrl = "";
    public ArtcInfoModel artcInfo;
    public String coverUrl = "";
    public String userDefineField = "";
    public String roomId = "";
    public boolean enableLinkMic = false;

    public String getAnchorId() {
        return this.anchorId;
    }

    public String getLiveId() {
        return this.liveId;
    }

    public String getTitle() {
        return this.title;
    }

    public String getPlayUrl() {
        return this.playUrl;
    }

    public long getCreateDate() {
        return this.createDate;
    }

    public long getEndDate() {
        return this.endDate;
    }

    public long getPreStartDate() {
        return this.preStartDate;
    }

    public long getPreEndDate() {
        return this.preEndDate;
    }

    public long getDuration() {
        return this.duration;
    }

    public String getPushUrl() {
        return this.pushUrl;
    }

    public String getLiveUrl() {
        return this.liveUrl;
    }

    public int getStatus() {
        return this.status;
    }

    public String getIntroduction() {
        return this.introduction;
    }

    public int getCodeLevel() {
        return this.codeLevel;
    }

    public ArrayList<PlayUrlModel> getPlayUrlList() {
        return this.playUrlList;
    }

    public String getHlsUrl() {
        return this.hlsUrl;
    }

    public ArtcInfoModel getArtcInfo() {
        return this.artcInfo;
    }

    public String getCoverUrl() {
        return this.coverUrl;
    }

    public String getUserDefineField() {
        return this.userDefineField;
    }

    public String getRoomId() {
        return this.roomId;
    }

    public boolean isEnableLinkMic() {
        return enableLinkMic;
    }

    public String toString() {
        return "LiveInfo{anchorId=" + this.anchorId + ",liveId=" + this.liveId + ",title=" + this.title + ",playUrl=" + this.playUrl + ",createDate=" + this.createDate + ",endDate=" + this.endDate + ",preStartDate=" + this.preStartDate + ",preEndDate=" + this.preEndDate + ",duration=" + this.duration + ",pushUrl=" + this.pushUrl + ",liveUrl=" + this.liveUrl + ",status=" + this.status + ",introduction=" + this.introduction + ",codeLevel=" + this.codeLevel + ",playUrlList=" + this.playUrlList + ",hlsUrl=" + this.hlsUrl + ",artcInfo=" + this.artcInfo + ",coverUrl=" + this.coverUrl + ",userDefineField=" + this.userDefineField + ",roomId=" + this.roomId + "}";
    }
}

