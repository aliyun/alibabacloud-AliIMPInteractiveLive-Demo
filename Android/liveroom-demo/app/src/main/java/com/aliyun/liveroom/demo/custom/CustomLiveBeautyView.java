package com.aliyun.liveroom.demo.custom;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.aliyun.standard.liveroom.lib.component.view.LiveBeautyView;

/**
 * 直播间美颜视图 (页面底部)
 *
 * @author puke
 * @version 2021/12/13
 */
public class CustomLiveBeautyView extends LiveBeautyView {

    public CustomLiveBeautyView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(Color.parseColor("#3300ff00"));

        // 设置长按后的组件提示
        LongClickHelper.attach(this);
    }
}
