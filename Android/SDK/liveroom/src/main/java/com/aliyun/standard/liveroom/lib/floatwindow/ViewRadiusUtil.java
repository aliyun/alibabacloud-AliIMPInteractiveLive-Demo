package com.aliyun.standard.liveroom.lib.floatwindow;

import android.graphics.Outline;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;

/**
 * 设置SurfaceView圆角
 *
 * @author puke
 * @version 2021/12/23
 */
class ViewRadiusUtil {

    /**
     * 设置圆角
     *
     * @param view   包含SurfaceView的View (如果是容器, 会继续遍历, 找到为止)
     * @param radius 圆角大小
     */
    static void setRadius4RenderView(View view, float radius) {
        SurfaceView surfaceView = findSurfaceView(view);
        if (surfaceView == null) {
            return;
        }

        if (radius > 0) {
            surfaceView.setClipToOutline(true);
            surfaceView.setOutlineProvider(new ViewRoundRectOutlineProvider(radius));
        } else {
            surfaceView.setClipToOutline(false);
            surfaceView.setOutlineProvider(null);
        }
    }

    private static SurfaceView findSurfaceView(View view) {
        if ((view instanceof SurfaceView)) {
            return (SurfaceView) view;
        } else if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                SurfaceView surfaceView = findSurfaceView(child);
                if (surfaceView != null) {
                    return surfaceView;
                }
            }
        }
        return null;
    }

    private static class ViewRoundRectOutlineProvider extends ViewOutlineProvider {

        final float radius;

        ViewRoundRectOutlineProvider(float radius) {
            this.radius = radius;
        }

        @Override
        public void getOutline(View view, Outline outline) {
            outline.setRoundRect(
                    0,
                    0,
                    view.getWidth(),
                    view.getHeight(),
                    radius);
        }
    }
}
