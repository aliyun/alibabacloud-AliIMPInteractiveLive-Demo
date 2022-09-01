package com.aliyun.standard.liveroom.lib.component.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aliyun.roompaas.uibase.util.ViewUtil;
import com.aliyun.roompaas.biz.SampleRoomEventHandler;
import com.aliyun.roompaas.live.LiveEvent;
import com.aliyun.roompaas.live.SampleLiveEventHandler;
import com.aliyun.roompaas.live.exposable.event.LiveCommonEvent;
import com.aliyun.standard.liveroom.lib.Actions;
import com.aliyun.standard.liveroom.lib.LiveContext;
import com.aliyun.standard.liveroom.lib.R;
import com.aliyun.standard.liveroom.lib.component.BaseComponent;
import com.aliyun.standard.liveroom.lib.component.ComponentHolder;
import com.aliyun.standard.liveroom.lib.component.IComponent;
import com.aliyun.roompaas.uibase.util.AnimUtil;

/**
 * 直播未开始的视图
 *
 * @author puke
 * @version 2021/7/29
 */
public class LiveNotStartView extends RelativeLayout implements ComponentHolder {

    private final Component component = new Component();
    private final TextView tips;

    public LiveNotStartView(Context context) {
        this(context, null, 0);
    }

    public LiveNotStartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LiveNotStartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setBackgroundColor(getResources().getColor(R.color.liveNotStartCurtainBg));
        View.inflate(context, R.layout.ilr_view_live_not_start, this);
        tips = findViewById(R.id.liveNotStartCurtain);
    }

    private void showUpLiveNotStartCurtainIfNecessary() {
        if (!component.isOwner() && ViewUtil.isNotVisible(this)) {
            ViewUtil.setVisible(this);
            ViewUtil.clickableView(this);
            AnimUtil.animIn(this);
        }
    }

    private void hideLiveNotStartCurtainIfNecessary() {
        if (!component.isOwner() && ViewUtil.isVisible(this)) {
            AnimUtil.animOut(this, new Runnable() {
                @Override
                public void run() {
                    ViewUtil.setGone(LiveNotStartView.this);
                }
            });
            ViewUtil.notClickableView(this);
        }
    }

    @Override
    public IComponent getComponent() {
        return component;
    }

    private class Component extends BaseComponent {
        @Override
        public void onInit(LiveContext liveContext) {
            super.onInit(liveContext);
            roomChannel.addEventHandler(new SampleRoomEventHandler() {

            });
            liveService.addEventHandler(new SampleLiveEventHandler() {
                @Override
                public void onLiveStarted(LiveCommonEvent event) {
                    if (!isOwner()) {
                        hideLiveNotStartCurtainIfNecessary();
                    }
                }

                @Override
                public void onLiveStopped(LiveCommonEvent event) {
                    if (!isOwner()) {
                        tips.setText("直播已结束");
                        showUpLiveNotStartCurtainIfNecessary();
                    }
                }

                @Override
                public void onPusherEvent(LiveEvent event) {
                    super.onPusherEvent(event);
                }

                @Override
                public void onPlayerError() {
                    showUpLiveNotStartCurtainIfNecessary();
                }
            });
        }

        @Override
        public void onEvent(String action, Object... args) {
            if (Actions.TRY_PLAY_LIVE_SUCCESS.equals(action)) {
                if (!liveService.hasLive()) {
                    showUpLiveNotStartCurtainIfNecessary();
                }
            }
        }
    }
}
