package com.aliyun.liveroom.demo.custom;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.aliyun.standard.liveroom.lib.component.view.LiveShareView;

/**
 * 直播间分享视图 (页面底部)
 *
 * @author puke
 * @version 2021/12/13
 */
public class CustomLiveShareView extends LiveShareView {

    public CustomLiveShareView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(Color.parseColor("#33ff0000"));

        // 设置长按后的组件提示
        LongClickHelper.attach(this);
    }
}
