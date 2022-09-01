package com.aliyun.standard.liveroom.lib.component.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.aliyun.standard.liveroom.lib.Actions;
import com.aliyun.standard.liveroom.lib.component.BaseComponent;
import com.aliyun.standard.liveroom.lib.component.ComponentHolder;
import com.aliyun.standard.liveroom.lib.component.IComponent;

/**
 * @author puke
 * @version 2021/7/30
 */
public class LiveGestureView extends View implements ComponentHolder {

    private final Component component = new Component();

    public LiveGestureView(Context context) {
        this(context, null, 0);
    }

    public LiveGestureView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LiveGestureView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                component.postEvent(Actions.EMPTY_SPACE_CLICK);
            }
        });
    }

    @Override
    public IComponent getComponent() {
        return component;
    }

    private static class Component extends BaseComponent {
    }
}
