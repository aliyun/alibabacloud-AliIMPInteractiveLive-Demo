package com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.aliyun.roompaas.beauty_pro.R;


public class BeautyScrollMenuSubPanel extends FrameLayout {

    private Context mContext;
    private TextView mSubPanelTitle;
    private SimpleHorizontalScrollView  mSubPanelItemsScrollView;
    private OnSubPanelClickListener mOnSubPanelClickListener;

    public BeautyScrollMenuSubPanel(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public BeautyScrollMenuSubPanel(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public BeautyScrollMenuSubPanel(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(final Context context) {
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.beauty_menu_panel_layout_subpanel, this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.BOTTOM;
        setLayoutParams(params);

        mSubPanelTitle = findViewById(R.id.beauty_subpanel_bar_title);
        mSubPanelItemsScrollView = findViewById(R.id.beauty_subpanel_items_scrollview);
        View imageView = findViewById(R.id.beauty_subpanel_bar_back);
        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // 返回上一级
                if (mOnSubPanelClickListener != null) {
                    mOnSubPanelClickListener.onBackClick();
                }
            }
        });
    }

    public void setSubPanelTitle(String title) {
        mSubPanelTitle.setText(title);
    }

    public void setAdapter(Adapter adapter) {
        mSubPanelItemsScrollView.setAdapter(adapter);
    }

    public Adapter getAdapter() {
        return mSubPanelItemsScrollView.getAdapter();
    }

    public void setOnSubPanelClickListener(OnSubPanelClickListener clickListener) {
        mOnSubPanelClickListener = clickListener;
    }

    public interface OnSubPanelClickListener {
//        public void onSubPanleItemClick();
        public void onBackClick();
    }
}
