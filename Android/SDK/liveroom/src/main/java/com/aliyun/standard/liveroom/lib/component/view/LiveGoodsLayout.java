package com.aliyun.standard.liveroom.lib.component.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.aliyun.standard.liveroom.lib.Actions;
import com.aliyun.standard.liveroom.lib.LiveHook;
import com.aliyun.standard.liveroom.lib.LivePrototype;
import com.aliyun.standard.liveroom.lib.ViewSlot;
import com.aliyun.standard.liveroom.lib.component.BaseComponent;
import com.aliyun.standard.liveroom.lib.component.ComponentHolder;
import com.aliyun.standard.liveroom.lib.component.IComponent;

/**
 * @author puke
 * @version 2021/7/30
 */
public class LiveGoodsLayout extends FrameLayout implements ComponentHolder {

    private final Component component = new Component();

    public LiveGoodsLayout(@NonNull Context context) {
        this(context, null, 0);
    }

    public LiveGoodsLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LiveGoodsLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LiveHook liveHook = LivePrototype.getInstance().getLiveHook();
        if (liveHook != null) {
            ViewSlot goodsSlot = liveHook.getGoodsSlot();
            if (goodsSlot != null) {
                View goodsView = goodsSlot.createView(context);
                if (goodsView != null) {
                    addView(goodsView);
                }
            }
        }
    }

    @Override
    public IComponent getComponent() {
        return component;
    }

    private class Component extends BaseComponent {
        @Override
        public void onEvent(String action, Object... args) {
            switch (action) {
                case Actions.SHOW_GOODS_CARD:
                    setVisibility(VISIBLE);
                    break;
                case Actions.HIDE_GOODS_CARD:
                    setVisibility(GONE);
                    break;
            }
        }
    }
}
