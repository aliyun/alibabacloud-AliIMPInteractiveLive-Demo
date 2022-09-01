package com.aliyun.standard.liveroom.lib.component.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.aliyun.roompaas.live.LiveEvent;
import com.aliyun.roompaas.live.SampleLiveEventHandler;
import com.aliyun.standard.liveroom.lib.LiveContext;
import com.aliyun.standard.liveroom.lib.LivePrototype;
import com.aliyun.standard.liveroom.lib.R;
import com.aliyun.standard.liveroom.lib.component.BaseComponent;
import com.aliyun.standard.liveroom.lib.component.ComponentHolder;
import com.aliyun.standard.liveroom.lib.component.IComponent;

/**
 * @author puke
 * @version 2022/3/21
 */
public class LiveLoadingView extends FrameLayout implements ComponentHolder {

    public static final String ACTION_SHOW_LOADING = "show_loading";
    public static final String ACTION_HIDE_LOADING = "hide_loading";

    protected final ContentLoadingProgressBar loadingBar;

    private final Component component = new Component();
    private final boolean supportLoadingView;

    public LiveLoadingView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        supportLoadingView = LivePrototype.getInstance().getOpenLiveParam().supportLoadingView;
        setVisibility(GONE);
        inflate(context, R.layout.ilr_view_live_loading, this);
        loadingBar = findViewById(R.id.loading_bar);
    }

    protected void startLoading() {
        if (supportLoadingView) {
            setVisibility(VISIBLE);
            loadingBar.show();
        }
    }

    protected void endLoading() {
        setVisibility(GONE);
        loadingBar.hide();
    }

    @Override
    public IComponent getComponent() {
        return component;
    }

    private class Component extends BaseComponent {
        @Override
        public void onInit(LiveContext liveContext) {
            super.onInit(liveContext);
            if (!supportLoadingView) {
                return;
            }

            liveService.addEventHandler(new SampleLiveEventHandler() {
                @Override
                public void onLoadingBegin() {
                    startLoading();
                }

                @Override
                public void onLoadingEnd() {
                    endLoading();
                }

                @Override
                public void onPrepared() {
                    endLoading();
                }

                @Override
                public void onPusherEvent(LiveEvent event) {
                    switch (event) {
                        case RECONNECT_START:
                            startLoading();
                            break;
                        // 重新推流成功时, 会回调FIRST_FRAME_PUSHED
                        case FIRST_FRAME_PUSHED:
                        case RECONNECT_SUCCESS:
                        case RECONNECT_FAIL:
                        case CONNECTION_FAIL:
                            endLoading();
                            break;
                    }
                }
            });
        }

        @Override
        public void onEvent(String action, Object... args) {
            switch (action) {
                case ACTION_SHOW_LOADING:
                    startLoading();
                    break;
                case ACTION_HIDE_LOADING:
                    endLoading();
                    break;
            }
        }
    }
}
