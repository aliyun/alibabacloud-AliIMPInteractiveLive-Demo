package com.aliyun.liveroom.demo.linkmic;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.aliyun.liveroom.demo.R;
import com.aliyun.standard.liveroom.lib.LiveContext;
import com.aliyun.standard.liveroom.lib.LivePrototype;
import com.aliyun.standard.liveroom.lib.component.BaseComponent;
import com.aliyun.standard.liveroom.lib.component.ComponentHolder;
import com.aliyun.standard.liveroom.lib.component.IComponent;
import com.aliyun.standard.liveroom.lib.linkmic.impl.SampleLinkMicEventHandler;

/**
 * @author puke
 * @version 2022/3/31
 */
public class CustomReadyLayer extends FrameLayout implements ComponentHolder {

    private final Component component = new Component();

    public CustomReadyLayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_linkmic_ready_layer, this);

        findViewById(R.id.view_ready_switch_camera).setOnClickListener(v -> component.switchCamera());
        findViewById(R.id.view_ready_start_live).setOnClickListener(v -> component.startLive());
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

            // 主播身份默认展示
            setVisibility(VISIBLE);
            roomChannel.getLinkMicService().getAnchorService().addEventHandler(new SampleLinkMicEventHandler() {
                @Override
                public void onJoinedSuccess(View view) {
                    // 入会成功后, 隐藏
                    setVisibility(View.GONE);
                }
            });
        }

        private void startLive() {
            roomChannel.getLinkMicService().getAnchorService().startLive();
        }

        private void switchCamera() {
            roomChannel.getLinkMicService().getAnchorService().switchCamera();
        }
    }
}
