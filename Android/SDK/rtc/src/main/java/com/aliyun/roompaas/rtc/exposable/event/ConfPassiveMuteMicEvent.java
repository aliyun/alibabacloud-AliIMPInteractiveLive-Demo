package com.aliyun.roompaas.rtc.exposable.event;

import java.io.Serializable;
import java.util.List;

/**
 * 停止响铃消息
 *
 * @author puke
 * @version 2021/6/2
 */
public class ConfPassiveMuteMicEvent implements Serializable {
    // 类型 15
    public int type;

    // 会议Id
    public String confId;

    // 版本
    public long version;

    // 麦克风状态变更的用户ID列表
    public List<String> userList;

    public boolean passiveMute;
}
