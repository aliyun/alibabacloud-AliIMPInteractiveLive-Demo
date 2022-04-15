package com.aliyun.liveroom.demo.linkmic;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.aliyun.standard.liveroom.lib.LiveContext;
import com.aliyun.standard.liveroom.lib.LivePrototype;
import com.aliyun.standard.liveroom.lib.component.BaseComponent;
import com.aliyun.standard.liveroom.lib.component.ComponentHolder;
import com.aliyun.standard.liveroom.lib.component.IComponent;
import com.aliyun.standard.liveroom.lib.linkmic.impl.SampleLinkMicEventHandler;

/**
 * 连麦直播的内容图层
 *
 * @author puke
 * @version 2021/7/30
 */
public class CustomContentLayer extends RelativeLayout implements ComponentHolder {

    private final Component component = new Component();

    public CustomContentLayer(@NonNull Context context) {
        this(context, null, 0);
    }

    public CustomContentLayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomContentLayer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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

            // 主播身份, 刚进来时隐藏该图层
            setVisibility(GONE);
            roomChannel.getLinkMicService().getAnchorService().addEventHandler(new SampleLinkMicEventHandler() {
                @Override
                public void onJoinedSuccess(View view) {
                    // 入会成功时, 展示出来
                    setVisibility(VISIBLE);
                }
            });
        }
    }
}
