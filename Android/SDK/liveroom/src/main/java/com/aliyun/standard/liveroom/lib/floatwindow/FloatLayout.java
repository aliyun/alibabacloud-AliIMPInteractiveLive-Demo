package com.aliyun.standard.liveroom.lib.floatwindow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import com.aliyun.standard.liveroom.lib.R;

import java.lang.ref.WeakReference;

/**
 * 悬浮窗父控件
 *
 * @author puke
 * @version 2021/12/24
 */
@SuppressLint("ViewConstructor")
public class FloatLayout extends FrameLayout {

    @NonNull
    private final View renderView;
    @NonNull
    private final OriginViewInfo originViewInfo;

    public FloatLayout(@NonNull Context context,
                       @NonNull View renderView,
                       @NonNull FloatWindowConfig config,
                       @Nullable final Runnable closeClickListener) {
        super(context);
        inflate(context, R.layout.ilr_layout_float_window, this);
        ViewGroup renderContainer = findViewById(R.id.layout_render_container);

        this.renderView = renderView;
        this.originViewInfo = new OriginViewInfo(renderView);

        // 取出配置信息
        int radius = config.radius;
        int expectBorderSize = config.borderSize;
        int borderColor = config.borderColor;

        // 设置渲染视图圆角
        ViewRadiusUtil.setRadius4RenderView(renderView, radius);

        // 设置背景
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(borderColor);
        bg.setCornerRadius(radius);
        setBackground(bg);

        // 设置内边距
//        int borderSize = getBorderSize(expectBorderSize, radius);
//        setPadding(borderSize, borderSize, borderSize, borderSize);
        setPadding(expectBorderSize, expectBorderSize, expectBorderSize, expectBorderSize);

        // 从当前父控件中移除
        removeFromParent(renderView);

        // 添加渲染视图
        renderContainer.addView(renderView);

        findViewById(R.id.layout_close).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (closeClickListener != null) {
                    closeClickListener.run();
                }
            }
        });
    }

    /**
     * 为了解决无法直接裁剪圆角问题 https://juejin.cn/post/6844904131631529992 <br/>
     *
     * @param expectBorderSize 期望大小
     * @param radius           圆角大小
     * @return 时机大小
     */
    private int getBorderSize(int expectBorderSize, int radius) {
        int minBorderSize = (int) Math.ceil(0.423f * radius);
        return Math.max(expectBorderSize, minBorderSize);
    }

    /**
     * 移除渲染视图
     *
     * @return 被移除的渲染视图
     */
    public View removeRenderView() {
        // 移除之前, 将圆角置为0
        ViewRadiusUtil.setRadius4RenderView(renderView, 0);
        removeFromParent(renderView);
        return renderView;
    }

    @NonNull
    public OriginViewInfo getOriginViewInfo() {
        return originViewInfo;
    }

    public View restoreRenderView() {
        // 塞回原父控件中
        WeakReference<ViewGroup> layoutRef = originViewInfo.parentLayout;
        ViewGroup layout = layoutRef == null ? null : layoutRef.get();
        if (layout != null) {
            layout.addView(renderView, originViewInfo.index, originViewInfo.layoutParams);
        }
        return renderView;
    }

    private static void removeFromParent(View view) {
        if (view == null) {
            return;
        }

        ViewParent parent = view.getParent();
        if ((parent instanceof ViewGroup)) {
            ((ViewGroup) parent).removeView(view);
        }
    }

    /**
     * 渲染视图变为小窗之前的原数据信息
     */
    public static class OriginViewInfo {
        public final ViewGroup.LayoutParams layoutParams;
        public final int index;
        public final WeakReference<ViewGroup> parentLayout;

        OriginViewInfo(View renderView) {
            layoutParams = renderView.getLayoutParams();
            ViewParent parent = renderView.getParent();
            if ((parent instanceof ViewGroup)) {
                ViewGroup group = (ViewGroup) parent;
                index = group.indexOfChild(renderView);
                parentLayout = new WeakReference<>(group);
            } else {
                parentLayout = null;
                index = -1;
            }
        }
    }
}
