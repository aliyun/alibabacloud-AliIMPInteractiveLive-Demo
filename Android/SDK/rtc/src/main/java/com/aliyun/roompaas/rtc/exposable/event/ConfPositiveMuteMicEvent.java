package com.aliyun.roompaas.rtc.exposable.event;

import java.io.Serializable;
import java.util.List;

/**
 * 停止响铃消息
 *
 * @author puke
 * @version 2021/6/2
 */
public class ConfPositiveMuteMicEvent implements Serializable {
    // 类型 14
    public int type;

    // 会议Id
    public String confId;

    // 版本
    public long version;

    // 麦克风状态变更的用户ID列表
    public List<String> userList;

    // true：主动关闭麦克风；false：主动取消关闭
    public boolean positiveMute;
}
