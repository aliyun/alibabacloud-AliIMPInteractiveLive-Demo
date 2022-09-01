package com.aliyun.roompaas.rtc.exposable.event;

import java.io.Serializable;

/**
 * 停止响铃消息
 *
 * @author puke
 * @version 2021/6/2
 */
public class ConfMuteAllMicEvent implements Serializable {
    // 类型 16
    public int type;

    // 会议Id
    public String confId;

    // 版本
    public long version;

    public boolean muteAll;
}
