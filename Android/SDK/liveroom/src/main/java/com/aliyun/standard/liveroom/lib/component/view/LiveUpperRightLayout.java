package com.aliyun.standard.liveroom.lib.component.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.aliyun.standard.liveroom.lib.LiveHook;
import com.aliyun.standard.liveroom.lib.LivePrototype;
import com.aliyun.standard.liveroom.lib.ViewSlot;

/**
 * 右上方视图容器
 *
 * @author puke
 * @version 2021/7/30
 */
public class LiveUpperRightLayout extends FrameLayout {

    public LiveUpperRightLayout(@NonNull Context context) {
        this(context, null, 0);
    }

    public LiveUpperRightLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LiveUpperRightLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LiveHook liveHook = LivePrototype.getInstance().getLiveHook();
        if (liveHook != null) {
            ViewSlot upperRightSlot = liveHook.getUpperRightSlot();
            if (upperRightSlot != null) {
                View upperRightView = upperRightSlot.createView(context);
                if (upperRightView != null) {
                    addView(upperRightView);
                }
            }
        }
    }
}
