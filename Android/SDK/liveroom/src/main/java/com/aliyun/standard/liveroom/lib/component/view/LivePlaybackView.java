package com.aliyun.standard.liveroom.lib.component.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.alibaba.dingpaas.room.RoomDetail;
import com.aliyun.roompaas.live.SampleLiveEventHandler;
import com.aliyun.roompaas.live.exposable.event.LiveCommonEvent;
import com.aliyun.roompaas.uibase.view.ControlView;
import com.aliyun.standard.liveroom.lib.LiveContext;
import com.aliyun.standard.liveroom.lib.R;
import com.aliyun.standard.liveroom.lib.component.BaseComponent;
import com.aliyun.standard.liveroom.lib.component.ComponentHolder;
import com.aliyun.standard.liveroom.lib.component.IComponent;

/**
 * @author puke
 * @version 2021/11/1
 */
public class LivePlaybackView extends FrameLayout implements ComponentHolder {

    private final Component component = new Component();

    private final ControlView controlView;

    public LivePlaybackView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View.inflate(context, R.layout.ilr_view_live_playback, this);
        controlView = findViewById(R.id.view_control_view);

        controlView.setPlayStatusClickListener(component);
        controlView.setOnSeekListener(component);
    }

    private void setVisible(boolean visible) {
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (layoutParams != null) {
            layoutParams.height = visible ? LayoutParams.WRAP_CONTENT : 0;
            setLayoutParams(layoutParams);
        }
    }

    @Override
    public IComponent getComponent() {
        return component;
    }

    private class Component extends BaseComponent
            implements ControlView.PlayStatusChange, ControlView.OnSeekListener {

        boolean isEnd;
        boolean isDragging;

        @Override
        public void onInit(LiveContext liveContext) {
            super.onInit(liveContext);
            liveService.addEventHandler(new SampleLiveEventHandler() {
                @Override
                public void onLiveStopped(LiveCommonEvent event) {
//                    if (supportPlayback()) {
//                        setVisible(true);
//                    }
                }

                @Override
                public void onRenderStart() {
                    controlView.setDuration(getPlayerService().getDuration());
                    controlView.setPlayStatus(true);
                }

                @Override
                public void onPlayerCurrentPosition(long position) {
                    if (!isDragging) {
                        controlView.setCurrentPosition(position);
                        controlView.updateProgress(position);
                    }
                }

                @Override
                public void onPlayerEnd() {
                    controlView.setPlayStatus(false);
                    controlView.setCurrentPosition(0);
                    controlView.updateProgress(0);
                    isEnd = true;
                }
            });
        }

        @Override
        public void onEnterRoomSuccess(RoomDetail roomDetail) {
            setVisible(needPlayback());
        }

        @Override
        public void onPause() {
            getPlayerService().pausePlay();
        }

        @Override
        public void onResume() {
            if (isEnd) {
                isEnd = false;
                getPlayerService().refreshPlay();
            } else {
                getPlayerService().resumePlay();
            }
        }

        @Override
        public void onSeekStart(int position) {
            isDragging = true;
            getPlayerService().pausePlay();
        }

        @Override
        public void onSeekEnd(int position) {
            isDragging = false;
            getPlayerService().resumePlay();
            getPlayerService().seekTo(position);
        }

        @Override
        public void onProgressChanged(int position) {

        }
    }
}
