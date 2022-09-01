package com.aliyun.roompaas.rtc.exposable.event;

import java.io.Serializable;
import java.util.List;

/**
 * 摄像头开关
 */
public class ConfMuteCameraEvent implements Serializable {

    public int type;

    // 消息版本
    public long version;

    // 会议ID
    public String confId;

    // 被禁用用户ID
    public String userId;

    // true: 不禁音; false: 禁音
    public boolean open;
}
