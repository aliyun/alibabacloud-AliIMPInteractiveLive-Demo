package com.aliyun.standard.liveroom.lib.floatwindow;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.aliyun.roompaas.uibase.util.AppUtil;

/**
 * @author puke
 * @version 2021/12/24
 */
public class ItemViewTouchListener implements View.OnTouchListener {

    private final GestureDetector gestureDetector;
    private final WindowManager windowManager;
    private final WindowManager.LayoutParams layoutParams;

    private View view;
    private boolean isScrolling;

    public ItemViewTouchListener(Context context, final WindowManager.LayoutParams layoutParams,
                                 final WindowManager windowManager) {
        this.windowManager = windowManager;
        this.layoutParams = layoutParams;
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            private int x = 0;
            private int y = 0;

            @Override
            public boolean onDown(MotionEvent e) {
                // 按下
                isScrolling = false;
                x = layoutParams.x;
                y = layoutParams.y;
                return super.onDown(e);
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                // 拖动
                isScrolling = true;
                layoutParams.x = (int) (x + e2.getRawX() - e1.getRawX());
                layoutParams.y = (int) (y + e2.getRawY() - e1.getRawY());
                windowManager.updateViewLayout(view, layoutParams);
                return super.onFling(e1, e2, distanceX, distanceY);
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                // 点击
                isScrolling = false;
                onClick();
                return true;
            }
        });
    }

    protected void onClick() {

    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouch(final View view, final MotionEvent event) {
        this.view = view;
        if (event.getAction() == MotionEvent.ACTION_UP && isScrolling) {
            int screenWidth = AppUtil.getScreenWidth();
            int viewWidth = view.getWidth() / 2;
            // 拖动结束, 自动靠边
            isScrolling = false;
            // 当前小窗中心点x坐标
            int currentCenterX = layoutParams.x + viewWidth;
            // 屏幕中心点x坐标
            int screenCenterX = screenWidth / 2;

            // 动画开始x坐标
            int startX = layoutParams.x;
            // 动画结束x坐标
            int endX = currentCenterX < screenCenterX ? 0 : screenWidth - view.getWidth();

            // 停靠动画
            float pathRate = Math.abs(startX - endX) / ((screenWidth - viewWidth) / 2f);
            long duration = (long) (200 * pathRate);
            ValueAnimator animator = ValueAnimator.ofInt(startX, endX)
                    .setDuration(duration);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    layoutParams.x = (int) animation.getAnimatedValue();
                    windowManager.updateViewLayout(view, layoutParams);
                }
            });
            animator.start();
        }
        return gestureDetector.onTouchEvent(event);
    }
}