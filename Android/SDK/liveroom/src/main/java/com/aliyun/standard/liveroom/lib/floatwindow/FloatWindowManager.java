package com.aliyun.standard.liveroom.lib.floatwindow;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import com.aliyun.roompaas.base.log.Logger;
import com.aliyun.roompaas.player.LivePlayerManagerHolder;
import com.aliyun.standard.liveroom.lib.LogoutTaskManager;

import java.lang.ref.WeakReference;

/**
 * @author puke
 * @version 2021/12/21
 */
public class FloatWindowManager {

    private static final String TAG = FloatWindowManager.class.getSimpleName();
    private static final byte[] sInstanceLock = new byte[0];

    private static FloatWindowManager sInstance;

    private FloatWindowConfig config;
    private WeakReference<FloatLayout> floatLayoutRef;

    public static FloatWindowManager instance() {
        if (sInstance == null) {
            synchronized (sInstanceLock) {
                if (sInstance == null) {
                    sInstance = new FloatWindowManager();
                }
            }
        }
        return sInstance;
    }

    private FloatWindowManager() {
    }

    public void setConfig(FloatWindowConfig config) {
        this.config = config;
    }

    private FloatWindowConfig getConfig() {
        if (config == null) {
            config = new FloatWindowConfig();
        }
        return config;
    }

    public boolean isShowing() {
        return getFloatLayout() != null;
    }

    @Nullable
    private FloatLayout getFloatLayout() {
        return floatLayoutRef == null ? null : floatLayoutRef.get();
    }

    public void show(View view, Activity activity) {
        show(view, new DefaultFloatWindowListener(activity));
    }

    @SuppressLint("ClickableViewAccessibility")
    public void show(View view, @Nullable final FloatWindowListener clickListener) {
        if (view == null) {
            Logger.e(TAG, "Show error, the view is null");
            return;
        }
        FloatLayout currentFloatLayout = getFloatLayout();
        if (currentFloatLayout != null) {
            Logger.e(TAG, "Already has float window show.");
            return;
        }

        Context context = view.getContext().getApplicationContext();

        // 读取配置信息
        FloatWindowConfig config = getConfig();
        FloatLayout floatLayout = new FloatLayout(context, view, config, new Runnable() {
            @Override
            public void run() {
                if (clickListener != null) {
                    clickListener.onCloseClick(FloatWindowManager.this);
                }
            }
        });
        floatLayoutRef = new WeakReference<>(floatLayout);

        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams lp = buildWindowLayoutParams(config);

        floatLayout.setOnTouchListener(new ItemViewTouchListener(context, lp, windowManager) {
            @Override
            protected void onClick() {
                if (clickListener != null) {
                    clickListener.onContentClick(FloatWindowManager.this);
                }
            }
        });

        windowManager.addView(floatLayout, lp);

        LivePlayerManagerHolder.hold(view);
        LogoutTaskManager.cancelLogout();
    }

    @NonNull
    private WindowManager.LayoutParams buildWindowLayoutParams(FloatWindowConfig config) {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            lp.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        lp.format = PixelFormat.RGBA_8888;
        lp.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        lp.width = config.width;
        lp.height = config.height;
        lp.gravity = Gravity.START | Gravity.TOP;
        lp.x = config.x;
        lp.y = config.y;
        return lp;
    }

    @Nullable
    public View dismiss() {
        // 默认贴回去
        return dismiss(true);
    }

    @Nullable
    public View dismiss(boolean restore) {
        FloatLayout floatLayout = getFloatLayout();
        if (floatLayout == null) {
            return null;
        }

        // 移除
        View renderView = floatLayout.removeRenderView();
        if (restore) {
            // 恢复
            floatLayout.restoreRenderView();
        }

        // 移除小窗视图
        WindowManager windowManager = (WindowManager) floatLayout.getContext()
                .getSystemService(Context.WINDOW_SERVICE);
        windowManager.removeViewImmediate(floatLayout);

        // 释放引用
        floatLayoutRef = null;

        LivePlayerManagerHolder.unHold(renderView);
        LogoutTaskManager.prepareLogout();

        return renderView;
    }
}
