package com.aliyun.standard.liveroom.lib.floatwindow;

import android.graphics.Color;

import com.aliyun.roompaas.uibase.util.AppUtil;

/**
 * @author puke
 * @version 2021/12/23
 */
public class FloatWindowConfig {

    // 小窗样式
    public int borderSize = AppUtil.dp(3);
    public int borderColor = Color.parseColor("#fb622b");
    public int radius = AppUtil.dp(5);

    // 小窗大小
    public int width = AppUtil.getScreenWidth() / 3;
    public int height = width * 16 / 9;

    // 小窗位置
    public int x = AppUtil.getScreenWidth() - width;
    public int y = (AppUtil.getScreenHeight() - height) / 2;
}
