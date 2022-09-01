package com.aliyun.standard.liveroom.lib.component.view;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.view.View;

import com.aliyun.standard.liveroom.lib.LiveContext;
import com.aliyun.standard.liveroom.lib.component.BaseComponent;
import com.aliyun.standard.liveroom.lib.component.ComponentHolder;
import com.aliyun.standard.liveroom.lib.component.IComponent;

/**
 * 横竖屏切换组件
 *
 * @author puke
 * @version 2021/12/28
 */
class LiveRotateScreenView extends AppCompatButton implements ComponentHolder {

    private final Component component = new Component();

    public LiveRotateScreenView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setText("横屏组件示例");
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                component.handleRotateEvent();
            }
        });
    }

    @Override
    public IComponent getComponent() {
        return component;
    }

    private static class Component extends BaseComponent {

        @Override
        public void onInit(LiveContext liveContext) {
            super.onInit(liveContext);
            // 设置屏幕随重力感应 (不设置默认为竖屏)
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }

        @Override
        public boolean interceptBackKey() {
            // 拦截横屏时点击返回的事件, 先置为竖屏
            if (isLandscape()) {
                setLandscape(false);
                return true;
            }

            return super.interceptBackKey();
        }

        private void handleRotateEvent() {
            // 横竖屏切换
            setLandscape(!isLandscape());
        }
    }
}
