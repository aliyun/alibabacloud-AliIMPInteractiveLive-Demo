package com.aliyun.roompaas.live.exposable.event;

import java.io.Serializable;

/**
 * @author puke
 * @version 2021/5/17
 */
public class LiveCommonEvent implements Serializable {

    /**
     * 直播的uuid
     */
    public String liveId;

    /**
     * 推流地址
     */
    public String pushUrl;

    /**
     * 拉流地址
     */
    public String pullUrl;

    /**
     * 直播的状态，"0":未开始；"1"直播中；"2":"直播结束"
     */
    public String status;
}
