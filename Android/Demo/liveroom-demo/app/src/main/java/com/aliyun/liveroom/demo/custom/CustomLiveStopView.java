package com.aliyun.liveroom.demo.custom;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.aliyun.standard.liveroom.lib.component.view.LiveStopView;

/**
 * 自定义停止直播视图 (位于直播页右上角)
 *
 * @author puke
 * @version 2021/12/13
 */
public class CustomLiveStopView extends LiveStopView {

    public CustomLiveStopView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(Color.parseColor("#3300ff00"));

        // 设置长按后的组件提示
        LongClickHelper.attach(this);
    }
}
