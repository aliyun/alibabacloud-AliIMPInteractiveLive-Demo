package com.aliyun.roompaas.rtc.exposable.event;

import com.alibaba.dingpaas.rtc.ConfInfoModel;

import java.io.Serializable;

/**
 * 会议状态变更消息
 *
 * @author puke
 * @version 2021/6/2
 */
public class ConfEvent implements Serializable {

    /**
     * 类型
     */
    public int type;

    /**
     * 状态
     */
    public int status;

    /**
     * 版本
     */
    public long version;

    /**
     * 会议信息
     */
    public ConfInfoModel confInfoModel;
}
