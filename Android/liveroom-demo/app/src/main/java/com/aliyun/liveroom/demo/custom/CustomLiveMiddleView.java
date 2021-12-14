package com.aliyun.liveroom.demo.custom;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.aliyun.liveroom.demo.R;
import com.aliyun.standard.liveroom.lib.component.BaseComponent;
import com.aliyun.standard.liveroom.lib.component.ComponentHolder;
import com.aliyun.standard.liveroom.lib.component.IComponent;

/**
 * 自定义直播间腰部视图 (位于页面中央)
 *
 * @author puke
 * @version 2021/12/13
 */
public class CustomLiveMiddleView extends RelativeLayout implements ComponentHolder {

    private final Component component = new Component();

    public CustomLiveMiddleView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(Color.parseColor("#33ff0000"));
        inflate(context, R.layout.view_live_middle, this);

        // 设置长按后的组件提示
        LongClickHelper.attach(this);
    }

    @Override
    public IComponent getComponent() {
        return component;
    }

    private static class Component extends BaseComponent {

    }
}
