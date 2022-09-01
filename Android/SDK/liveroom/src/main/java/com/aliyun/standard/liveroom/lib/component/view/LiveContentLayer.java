package com.aliyun.standard.liveroom.lib.component.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.aliyun.roompaas.live.LiveEvent;
import com.aliyun.roompaas.live.SampleLiveEventHandler;
import com.aliyun.roompaas.live.exposable.event.LiveCommonEvent;
import com.aliyun.standard.liveroom.lib.IContentLayer;
import com.aliyun.standard.liveroom.lib.LiveContext;
import com.aliyun.standard.liveroom.lib.LivePrototype;
import com.aliyun.standard.liveroom.lib.component.BaseComponent;
import com.aliyun.standard.liveroom.lib.component.ComponentHolder;
import com.aliyun.standard.liveroom.lib.component.IComponent;
import com.aliyun.standard.liveroom.lib.linkmic.impl.SampleLinkMicEventHandler;

/**
 * 直播内容图层
 *
 * @author puke
 * @version 2021/7/30
 */
public class LiveContentLayer extends RelativeLayout implements IContentLayer, ComponentHolder {

    private final Component component = new Component();

    public LiveContentLayer(@NonNull Context context) {
        this(context, null, 0);
    }

    public LiveContentLayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LiveContentLayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setClipChildren(false);
    }

    @Override
    public IComponent getComponent() {
        return component;
    }

    private class Component extends BaseComponent {
        @Override
        public void onInit(LiveContext liveContext) {
            super.onInit(liveContext);
            if (liveContext.getRole() != LivePrototype.Role.ANCHOR || needPlayback()) {
                // 观众身份, 一直显示该图层
                setVisibility(VISIBLE);
                return;
            }

            // 以下是主播逻辑
            setVisibility(GONE);
            liveService.addEventHandler(new SampleLiveEventHandler() {
                @Override
                public void onLiveStarted(LiveCommonEvent event) {
                    setVisibility(View.VISIBLE);
                }

                @Override
                public void onLiveStopped(LiveCommonEvent event) {
                    setVisibility(View.GONE);
                }

                @Override
                public void onPusherEvent(LiveEvent event) {
                    switch (event) {
                        case PUSH_STARTED:
                            setVisibility(View.VISIBLE);
                            break;
                    }
                }
            });

            if (supportLinkMic()) {
                liveService.getLinkMicPusherService().addEventHandler(new SampleLinkMicEventHandler() {
                    @Override
                    public void onJoinedSuccess(View view) {
                        setVisibility(View.VISIBLE);
                    }
                });
            }
        }

        @Override
        public void onEvent(String action, Object... args) {
//            if (Actions.GET_LIVE_DETAIL_SUCCESS.equals(action)) {
//                LiveDetail liveDetail = liveService.getLiveDetail();
//                if (liveDetail != null) {
//                    LiveInfo liveInfo = liveDetail.liveInfo;
//                    if (liveInfo != null) {
//                        LiveStatus status = LiveStatus.of(liveInfo.status);
//                        // 直播结束时, 显示内容图层
//                        if (status == LiveStatus.END) {
//                            setVisibility(GONE);
//                        }
//                    }
//                }
//            }
        }
    }
}
