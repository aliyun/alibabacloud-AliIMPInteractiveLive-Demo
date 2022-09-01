package com.aliyun.standard.liveroom.lib.component.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.aliyun.standard.liveroom.lib.LiveContext;
import com.aliyun.standard.liveroom.lib.R;
import com.aliyun.standard.liveroom.lib.component.BaseComponent;
import com.aliyun.standard.liveroom.lib.component.ComponentHolder;
import com.aliyun.standard.liveroom.lib.component.IComponent;
import com.aliyun.roompaas.uibase.util.KeyboardHelper;

/**
 * @author puke
 * @version 2021/7/30
 */
public class LiveBottomLayout extends LinearLayout implements ComponentHolder {

    private final Component component = new Component();

    public LiveBottomLayout(Context context) {
        this(context, null, 0);
    }

    public LiveBottomLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LiveBottomLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setClipChildren(false);
        setGravity(Gravity.BOTTOM);
        setOrientation(HORIZONTAL);
    }

    @Override
    public IComponent getComponent() {
        return component;
    }

    private class Component extends BaseComponent {
        @Override
        public void onInit(LiveContext liveContext) {
            super.onInit(liveContext);
            initKeyboard();
        }

        private void initKeyboard() {
            KeyboardHelper keyboardHelper = new KeyboardHelper(activity);
            keyboardHelper.setOnSoftKeyBoardChangeListener(new KeyboardHelper.OnSoftKeyBoardChangeListener() {
                @Override
                public void keyBoardShow(int height) {
                    ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) getLayoutParams();
                    layoutParams.bottomMargin = 12;
                    setLayoutParams(layoutParams);
                }

                @Override
                public void keyBoardHide(int height) {
                    ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) getLayoutParams();
                    layoutParams.bottomMargin = getResources().getDimensionPixelOffset(R.dimen.room_bottom_layout_margin_bottom);
                    setLayoutParams(layoutParams);
                }
            });
        }
    }
}
