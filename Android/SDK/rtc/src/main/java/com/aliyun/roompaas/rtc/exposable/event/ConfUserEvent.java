package com.aliyun.roompaas.rtc.exposable.event;

import com.alibaba.dingpaas.rtc.ConfUserModel;

import java.io.Serializable;
import java.util.List;

/**
 * 成员状态变更消息 (含入会成功, 入会失败, 离会, 踢人)
 *
 * @author puke
 * @version 2021/6/2
 */
public class ConfUserEvent implements Serializable {

    /**
     * 类型
     */
    public int type;

    /**
     * 消息版本
     */
    public long version;

    /**
     * 会议Id
     */
    public String confId;

    /**
     * 新加成员信息
     */
    public List<ConfUserModel> userList;
}
