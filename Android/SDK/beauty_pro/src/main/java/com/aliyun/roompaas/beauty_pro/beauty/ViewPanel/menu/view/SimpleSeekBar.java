package com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.support.v4.math.MathUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;


@SuppressLint("AppCompatCustomView")
public class SimpleSeekBar extends SeekBar {

    private int mMinValue = 0;
    public SimpleSeekBar(Context context) {
        super(context);
    }

    public SimpleSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SimpleSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = super.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP
                || event.getAction() == MotionEvent.ACTION_MOVE
                || event.getAction() == MotionEvent.ACTION_DOWN ) {
            // 排除掉action_cancel等不会修改进度值的事件
            fixProgressValue(event);
        }

        return result;
    }

    private void fixProgressValue(MotionEvent event) {
        final int x = Math.round(event.getX());
        final int y = Math.round(event.getY());
        final int width = getWidth();
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        final int availableWidth = width - paddingLeft - paddingRight;
        final float scale;
        if (x < paddingLeft) {
            scale = 0.0f;
        } else if (x > width - paddingRight) {
            scale = 1.0f;
        } else {
            scale = (x - paddingLeft) / (float) availableWidth;
        }

        int progress = wrapperValue(scale, getProgressOffset());
        setProgress(progress);
    }

    private int getProgressOffset() {
        return getProgessMin();
    }

    public int getProgessMin() {
//        int min = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? getMin() : 0;
        return mMinValue;
    }

    public void setProgessMin(int minValue) {
        mMinValue = minValue;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setMin(minValue);
        }
    }

    // 返回逻辑进度值，需要根据双向进行转换
    @Override
    public synchronized int getProgress() {
        int value = super.getProgress();
        if (mMinValue > 0 || getMax() <= 0|| Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            return value;
        // 双向且不支持的低版本
        float scale = value / (float)getMax();
        int offset = getProgessMin();
        return wrapperValue(scale, offset);
    }

    // 传入设置为逻辑进度值，需要根据双向进行转换存入
    @Override
    public synchronized void setProgress(int progress) {
        if (mMinValue > 0 || Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            super.setProgress(progress);
            return;
        }

        progress = reverseWrapperValue(progress);
        super.setProgress(progress);
    }

    private int wrapperValue(float scale, int offset) {
        // 双向且不支持的低版本
        int maxValue = getMax();
        int minValue = getProgessMin();
        int range = maxValue - minValue;
        int realValue = Math.round(range * scale) + offset;
        realValue = MathUtils.clamp(realValue, minValue, maxValue);
        return realValue;
    }

    private int reverseWrapperValue(int value) {
        int maxValue = getMax();
        int minValue = getProgessMin();
        int offset = getProgessMin();
        int range = maxValue - minValue;
        if (range <= 0) return value;
        float scale = (value - offset) / (float)range;
        int realValue = (int)(scale * maxValue);
        return realValue;
    }
}
