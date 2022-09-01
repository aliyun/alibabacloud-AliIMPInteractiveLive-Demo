package com.aliyun.roompaas.chat.exposable.event;

import java.io.Serializable;

/**
 * @author puke
 * @version 2021/7/16
 */
public class CustomMessageEvent implements Serializable {

    /**
     * 消息Id
     */
    public String messageId;

    /**
     * 消息体
     */
    public String data;
}
