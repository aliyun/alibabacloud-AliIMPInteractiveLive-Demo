package com.aliyun.standard.liveroom.lib;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * @author puke
 * @version 2021/5/13
 */
public class LimitSizeRecyclerView extends RecyclerView {

    private int maxWidth;
    private int maxHeight;

    public LimitSizeRecyclerView(@NonNull Context context) {
        super(context);
    }

    public LimitSizeRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LimitSizeRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        if (maxWidth > 0) {
            int width = MeasureSpec.getSize(widthSpec);
            if (width > maxWidth) {
                widthSpec = MeasureSpec.makeMeasureSpec(
                        maxWidth, MeasureSpec.getMode(widthSpec));
            }
        }
        if (maxHeight > 0) {
            int height = MeasureSpec.getSize(heightSpec);
            if (height > maxHeight) {
                heightSpec = MeasureSpec.makeMeasureSpec(
                        maxHeight, MeasureSpec.getMode(heightSpec));
            }
        }
        super.onMeasure(widthSpec, heightSpec);
    }
}
