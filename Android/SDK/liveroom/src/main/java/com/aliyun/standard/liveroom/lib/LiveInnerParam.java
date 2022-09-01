package com.aliyun.standard.liveroom.lib;

import com.aliyun.roompaas.biz.exposable.enums.LiveStatus;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @author puke
 * @version 2021/12/15
 */
public class LiveInnerParam implements Serializable {

    public String liveId;
    public LivePrototype.Role role;
    public LiveStatus liveStatus;
    public HashMap<String, String> extension;
    public boolean supportLinkMic;
}
