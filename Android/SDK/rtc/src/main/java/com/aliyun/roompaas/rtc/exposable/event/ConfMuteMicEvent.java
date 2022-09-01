package com.aliyun.roompaas.rtc.exposable.event;

import java.io.Serializable;
import java.util.List;

/**
 * 静音/取消静音
 */
public class ConfMuteMicEvent implements Serializable {

    public int type;

    // 消息版本
    public long version;

    // 会议ID
    public String confId;

    // 被禁用用户ID列表
    public List<String> userList;

    // true: 不禁音; false: 禁音
    public boolean open;
}
