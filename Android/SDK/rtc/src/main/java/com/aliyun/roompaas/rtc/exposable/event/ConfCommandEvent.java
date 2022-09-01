package com.aliyun.roompaas.rtc.exposable.event;

import com.alibaba.dingpaas.rtc.ConfUserModel;

import java.io.Serializable;

/**
 * 命令消息
 *
 * @author puke
 * @version 2021/6/2
 */
public class ConfCommandEvent implements Serializable {

    /**
     * 类型
     */
    public int type;

    /**
     * 版本
     */
    public long version;

    /**
     * 会议Id
     */
    public String confId;

    /**
     * 命令类型
     */
    public int commandType;

    /**
     * 命令内容
     */
    public String commandContent;

    /**
     * 接收命令人员信息
     */
    public ConfUserModel commandUser;
}
