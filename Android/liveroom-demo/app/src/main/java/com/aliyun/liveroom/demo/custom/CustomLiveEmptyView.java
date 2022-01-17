package com.aliyun.liveroom.demo.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.aliyun.standard.liveroom.lib.component.BaseComponent;
import com.aliyun.standard.liveroom.lib.component.ComponentHolder;
import com.aliyun.standard.liveroom.lib.component.IComponent;

/**
 * @author puke
 * @version 2022/1/11
 */
public class CustomLiveEmptyView extends View implements ComponentHolder {
    public CustomLiveEmptyView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public IComponent getComponent() {
        return new BaseComponent();
    }
}
