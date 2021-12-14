package com.aliyun.liveroom.demo.custom;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.aliyun.liveroom.demo.R;
import com.aliyun.standard.liveroom.lib.Actions;
import com.aliyun.standard.liveroom.lib.component.BaseComponent;
import com.aliyun.standard.liveroom.lib.component.ComponentHolder;
import com.aliyun.standard.liveroom.lib.component.IComponent;

/**
 * 自定义右上角视图 (位于直播间右上角)
 *
 * @author puke
 * @version 2021/12/13
 */
public class CustomLiveRightUpperView extends LinearLayout implements ComponentHolder {

    private final Component component = new Component();

    public CustomLiveRightUpperView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(Color.parseColor("#33ff0000"));
        inflate(context, R.layout.view_live_right_uppper, this);

        Switch showGoodsCard = findViewById(R.id.show_goods_card);
        showGoodsCard.setOnCheckedChangeListener(
                (buttonView, isChecked) -> component.setGoodsCardVisible(isChecked));

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.gravity = Gravity.CENTER;
        setLayoutParams(layoutParams);

        // 设置长按后的组件提示
        LongClickHelper.attach(this);
    }

    @Override
    public IComponent getComponent() {
        return component;
    }

    private static class Component extends BaseComponent {

        private void setGoodsCardVisible(boolean visible) {
            // 发送事件通知 LiveGoodsLayout 组件进行显示/隐藏切换
            postEvent(visible ? Actions.SHOW_GOODS_CARD : Actions.HIDE_GOODS_CARD);
        }
    }
}
