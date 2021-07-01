package com.aliyun.roompaas.app.model;

import android.graphics.Color;

import com.aliyun.roompaas.app.util.ColorUtil;

import java.util.Random;

/**
 * @author puke
 * @version 2021/5/13
 */
public class MessageModel {

    public String type;

    public String content;

    public final int color = ColorUtil.randomColor();

    public MessageModel(String type, String content) {
        this.type = type;
        this.content = content;
    }
}
