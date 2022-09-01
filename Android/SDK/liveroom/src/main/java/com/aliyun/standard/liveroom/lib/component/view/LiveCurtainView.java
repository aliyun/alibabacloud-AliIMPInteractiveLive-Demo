package com.aliyun.standard.liveroom.lib.component.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.alibaba.dingpaas.room.RoomDetail;
import com.aliyun.roompaas.live.SampleLiveEventHandler;
import com.aliyun.roompaas.live.exposable.event.LiveCommonEvent;
import com.aliyun.roompaas.uibase.util.AnimUtil;
import com.aliyun.standard.liveroom.lib.LiveContext;
import com.aliyun.standard.liveroom.lib.R;
import com.aliyun.standard.liveroom.lib.component.BaseComponent;
import com.aliyun.standard.liveroom.lib.component.ComponentHolder;
import com.aliyun.standard.liveroom.lib.component.IComponent;
import com.aliyun.standard.liveroom.lib.floatwindow.FloatWindowManager;

/**
 * @author puke
 * @version 2021/7/29
 */
public class LiveCurtainView extends View implements ComponentHolder {

    private final Component component = new Component();

    public LiveCurtainView(Context context) {
        this(context, null, 0);
    }

    public LiveCurtainView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LiveCurtainView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setBackgroundResource(R.drawable.ilr_live_bg_alic);
    }

    private void showCurtain() {
        AnimUtil.animIn(LiveCurtainView.this);
    }

    private void hideCurtain() {
        AnimUtil.animOut(LiveCurtainView.this);
    }

    @Override
    public IComponent getComponent() {
        return component;
    }

    private class Component extends BaseComponent {
        @Override
        public void onInit(LiveContext liveContext) {
            super.onInit(liveContext);
            liveService.addEventHandler(new SampleLiveEventHandler() {
                @Override
                public void onLiveStopped(LiveCommonEvent event) {
                    super.onLiveStopped(event);
                    if (!isOwner()) {
                        showCurtain();
                    }
                }

                @Override
                public void onRenderStart() {
                    hideCurtain();
                }

                @Override
                public void onPlayerError() {
                    showCurtain();
                }
            });

            if (FloatWindowManager.instance().isShowing()) {
                // 小窗时, 直接隐藏掉
                hideCurtain();
            }
        }

        @Override
        public void onEnterRoomSuccess(RoomDetail roomDetail) {
            if (isOwner()) {
                hideCurtain();
            }
        }
    }
}
