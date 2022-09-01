package com.aliyun.liveroom.demo.custom;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.aliyun.standard.liveroom.lib.component.view.LiveRenderView;

/**
 * 直播间渲染视图 (页面最下方的图层)
 *
 * @author puke
 * @version 2021/12/13
 */
public class CustomLiveRenderView extends LiveRenderView {

    public CustomLiveRenderView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(Color.parseColor("#3300ffff"));

        // 设置长按后的组件提示
        LongClickHelper.attach(this);
    }
}
