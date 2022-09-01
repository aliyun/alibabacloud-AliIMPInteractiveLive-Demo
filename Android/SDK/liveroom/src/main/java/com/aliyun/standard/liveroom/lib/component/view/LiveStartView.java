package com.aliyun.standard.liveroom.lib.component.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.standard.liveroom.lib.R;
import com.aliyun.standard.liveroom.lib.component.BaseComponent;
import com.aliyun.standard.liveroom.lib.component.ComponentHolder;
import com.aliyun.standard.liveroom.lib.component.IComponent;

/**
 * 开始直播视图
 *
 * @author puke
 * @version 2021/7/29
 */
public class LiveStartView extends FrameLayout implements ComponentHolder {

    private final Component component = new Component();

    public LiveStartView(@NonNull Context context) {
        this(context, null, 0);
    }

    public LiveStartView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LiveStartView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.ilr_view_live_start, this);

        findViewById(R.id.room_start_live).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                component.handleStartLive();
            }
        });
    }

    @Override
    public IComponent getComponent() {
        return component;
    }

    private static class Component extends BaseComponent {

        private void handleStartLive() {
            getPusherService().startLive(new Callback<View>() {
                @Override
                public void onSuccess(View data) {

                }

                @Override
                public void onError(String errorMsg) {
                    showToast("开始直播失败: " + errorMsg);
                }
            });
        }
    }
}
