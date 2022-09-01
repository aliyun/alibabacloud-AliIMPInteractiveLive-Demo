package com.aliyun.liveroom.demo.custom;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.aliyun.liveroom.demo.R;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.standard.liveroom.lib.component.BaseComponent;
import com.aliyun.standard.liveroom.lib.component.ComponentHolder;
import com.aliyun.standard.liveroom.lib.component.IComponent;

/**
 * 自定义主播的启播页面 (主播侧, 刚进入直播间时看到的「开始直播」按钮)
 *
 * @author puke
 * @version 2021/12/13
 */
public class CustomLiveStartView extends RelativeLayout implements ComponentHolder {

    private final Component component = new Component();

    public CustomLiveStartView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(Color.parseColor("#3300ff00"));
        inflate(context, R.layout.view_live_start, this);

        // 开始直播
        findViewById(R.id.start_live).setOnClickListener(v -> component.handleStartLive());
        // 切换摄像头
        findViewById(R.id.switch_camera).setOnClickListener(v -> component.handleSwitchCamera());

        // 设置布局撑满父控件
        setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        // 设置长按后的组件提示
        LongClickHelper.attach(this);
    }

    @Override
    public IComponent getComponent() {
        return component;
    }

    private static class Component extends BaseComponent {

        private void handleStartLive() {
            // 开始直播 (开播成功后, 当前视图会自动被隐藏掉)
            liveService.getPusherService().startLive(new Callback<View>() {
                @Override
                public void onSuccess(View view) {

                }

                @Override
                public void onError(String errorMsg) {
                    showToast(String.format("开播失败, %s", errorMsg));
                }
            });
        }

        private void handleSwitchCamera() {
            // 切换摄像头
            liveService.getPusherService().switchCamera();
        }
    }
}
