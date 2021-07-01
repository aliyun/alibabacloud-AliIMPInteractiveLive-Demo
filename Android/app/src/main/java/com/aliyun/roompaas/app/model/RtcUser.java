package com.aliyun.roompaas.app.model;

import com.aliyun.roompaas.app.util.ColorUtil;
import com.aliyun.roompaas.rtc.RtcUserStatus;

import java.io.Serializable;

/**
 * @author puke
 * @version 2021/6/15
 */
public class RtcUser implements Serializable {

    public String userId;

    public String nick;

    public RtcUserStatus status;

    public final int color = ColorUtil.randomColor();
}
