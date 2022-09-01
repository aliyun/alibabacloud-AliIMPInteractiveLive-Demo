package com.aliyun.standard.liveroom.lib;

import android.graphics.Color;

import com.aliyun.roompaas.uibase.util.ColorUtil;

/**
 * @author puke
 * @version 2021/5/13
 */
public class MessageModel {

    public String type;

    public String content;

    /**
     * type文字颜色
     */
    public int color = ColorUtil.randomColor();

    public String userId;

    /**
     * content文字颜色
     */
    public int contentColor = Color.WHITE;

    public MessageModel(String type, String content) {
        this.type = type;
        this.content = content;
    }

    public MessageModel(String userId, String type, String content) {
        this.userId = userId;
        this.content = content;
        this.type = type;
    }
}
