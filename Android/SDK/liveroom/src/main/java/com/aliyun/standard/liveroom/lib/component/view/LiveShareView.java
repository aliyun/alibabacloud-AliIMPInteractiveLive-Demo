package com.aliyun.standard.liveroom.lib.component.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.aliyun.standard.liveroom.lib.Actions;
import com.aliyun.standard.liveroom.lib.R;
import com.aliyun.standard.liveroom.lib.component.BaseComponent;
import com.aliyun.standard.liveroom.lib.component.ComponentHolder;
import com.aliyun.standard.liveroom.lib.component.IComponent;

/**
 * @author puke
 * @version 2021/7/29
 */
public class LiveShareView extends FrameLayout implements ComponentHolder {

    private final Component component = new Component();

    public LiveShareView(@NonNull Context context) {
        this(context, null, 0);
    }

    public LiveShareView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LiveShareView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.ilr_view_live_share, this);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                component.handleShareClick();
            }
        });
    }

    @Override
    public IComponent getComponent() {
        return component;
    }

    private static class Component extends BaseComponent {

        private void handleShareClick() {
            // 抛出分享事件
            postEvent(Actions.SHARE_CLICKED);
        }
    }
}
