package com.aliyun.liveroom.demo.custom;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.aliyun.standard.liveroom.lib.component.view.LiveInputView;

/**
 * 直播间输入框 (页面底部)
 *
 * @author puke
 * @version 2021/12/13
 */
public class CustomLiveInputView extends LiveInputView {

    public CustomLiveInputView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(Color.parseColor("#330000ff"));

        // 设置长按后的组件提示
        LongClickHelper.attach(this);
    }
}
