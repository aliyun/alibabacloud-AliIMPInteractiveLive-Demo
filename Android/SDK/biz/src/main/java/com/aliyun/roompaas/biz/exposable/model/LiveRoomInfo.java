package com.aliyun.roompaas.biz.exposable.model;

import java.io.Serializable;
import java.util.HashMap;

public class LiveRoomInfo implements Serializable {

    public String liveId;
    public String title;
    public String notice;
    public String coverUrl;
    public HashMap<String, String> extension;
}
