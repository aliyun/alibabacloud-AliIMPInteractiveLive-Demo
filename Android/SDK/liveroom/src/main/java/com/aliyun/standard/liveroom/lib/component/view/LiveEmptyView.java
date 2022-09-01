package com.aliyun.standard.liveroom.lib.component.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.aliyun.standard.liveroom.lib.component.BaseComponent;
import com.aliyun.standard.liveroom.lib.component.ComponentHolder;
import com.aliyun.standard.liveroom.lib.component.IComponent;

/**
 * @author puke
 * @version 2021/11/10
 */
public class LiveEmptyView extends View implements ComponentHolder {

    private final IComponent component = new BaseComponent();

    public LiveEmptyView(Context context) {
        this(context, null);
    }

    public LiveEmptyView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public IComponent getComponent() {
        return component;
    }
}
