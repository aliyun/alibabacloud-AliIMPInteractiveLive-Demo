package com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.view;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.HorizontalScrollView;

public class SimpleHorizontalScrollView extends HorizontalScrollView {

    private DataSetObserver mObserver;
    private Adapter mAdapter;

    public SimpleHorizontalScrollView(Context context) {
        super(context);
        initialize();
    }

    public SimpleHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public SimpleHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    public Adapter getAdapter() {
        return mAdapter;
    }

    public void setAdapter(Adapter adapter) {
        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mObserver);
        }
        mAdapter = adapter;
        mAdapter.registerDataSetObserver(mObserver);
        updateAdapter();
    }

    private void updateAdapter() {
        ViewGroup group = (ViewGroup) getChildAt(0);
        group.removeAllViews();
        for (int i = 0; i < mAdapter.getCount(); i++) {
            View view = mAdapter.getView(i, null, group);
            group.addView(view);
        }
    }

    void initialize() {
        mObserver = new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                updateAdapter();
            }

            @Override
            public void onInvalidated() {
                super.onInvalidated();
                ((ViewGroup) getChildAt(0)).removeAllViews();
            }
        };
    }

    public void setClicked(int position) {
        ((ViewGroup) getChildAt(0)).getChildAt(position).performClick();
    }
}