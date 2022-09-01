package com.aliyun.standard.liveroom.lib.component.view;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.aliyun.roompaas.live.LiveEvent;
import com.aliyun.roompaas.live.SampleLiveEventHandler;
import com.aliyun.roompaas.live.exposable.event.LiveCommonEvent;
import com.aliyun.standard.liveroom.lib.LiveContext;
import com.aliyun.standard.liveroom.lib.LiveHook;
import com.aliyun.standard.liveroom.lib.LivePrototype;
import com.aliyun.standard.liveroom.lib.ViewSlot;
import com.aliyun.standard.liveroom.lib.component.BaseComponent;
import com.aliyun.standard.liveroom.lib.component.ComponentHolder;
import com.aliyun.standard.liveroom.lib.component.IComponent;
import com.aliyun.standard.liveroom.lib.linkmic.impl.SampleLinkMicEventHandler;

/**
 * 直播准备图层
 *
 * @author puke
 * @version 2021/7/30
 */
public class LiveReadyLayer extends RelativeLayout implements ComponentHolder {

    private final Component component = new Component();

    public LiveReadyLayer(@NonNull Context context) {
        this(context, null, 0);
    }

    public LiveReadyLayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LiveReadyLayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LiveHook liveHook = LivePrototype.getInstance().getLiveHook();
        View startLiveView = null;
        if (liveHook != null) {
            ViewSlot readySlot = liveHook.getReadySlot();
            if (readySlot != null) {
                startLiveView = readySlot.createView(context);
            }
        }

        // 外部未设置时, 提供默认兜底的开始直播按钮
        if (startLiveView == null) {
            setBackgroundColor(Color.parseColor("#66000000"));
            startLiveView = new LiveStartView(context);
            LayoutParams layoutParams = new LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            startLiveView.setLayoutParams(layoutParams);
        }

        addView(startLiveView);
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
                // 观众身份 或者 观看直播回放, 都不显示该图层
                setVisibility(GONE);
                return;
            }

            // 以下是主播逻辑
            setVisibility(VISIBLE);
            liveService.addEventHandler(new SampleLiveEventHandler() {
                @Override
                public void onLiveStarted(LiveCommonEvent event) {
                    setVisibility(View.GONE);
                }

                @Override
                public void onPusherEvent(LiveEvent event) {
                    switch (event) {
                        case PUSH_STARTED:
                            setVisibility(View.GONE);
                            break;
                    }
                }
            });

            if (supportLinkMic()) {
                liveService.getLinkMicPusherService().addEventHandler(new SampleLinkMicEventHandler() {
                    @Override
                    public void onJoinedSuccess(View view) {
                        setVisibility(View.GONE);
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
//                        // 直播结束时, 隐藏启播图层
//                        if (status == LiveStatus.END) {
//                            setVisibility(VISIBLE);
//                        }
//                    }
//                }
//            }
        }
    }
}
