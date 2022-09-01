package com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.view;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;


import com.aliyun.roompaas.beauty_pro.R;
import com.aliyun.roompaas.beauty_pro.beauty.ViewPanel.menu.model.TabItemInfo;
import com.aliyun.roompaas.beauty_pro.utils.ResoureUtils;

public class BeautyMenuSeekPanel extends RelativeLayout {

    private SimpleSeekBar mProgressBar;
    private TextView mProgressValueText;
    private TabItemInfo mCurItemInfo;
    private OnProgressChangedListener mOnProgressListener;

    private float mProgressFontSize;

    public BeautyMenuSeekPanel(@NonNull Context context) {
        super(context);
    }

    public BeautyMenuSeekPanel(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BeautyMenuSeekPanel(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        initView(this.getContext());

        mProgressFontSize = getContext().getResources().getDimension(R.dimen.alivc_common_4);
    }

    private void initView(Context context) {
        mProgressValueText = findViewById(R.id.beauty_seekpanel_value);

        mProgressBar = findViewById(R.id.beauty_seekpanel_seekbar);
        mProgressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean b) {
                updateValuePosition();

                if (mOnProgressListener != null && mCurItemInfo != null) {
                    mOnProgressListener.onProgressChanged(mCurItemInfo, seekBar.getProgress());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                updateValuePosition();
                mProgressValueText.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mProgressValueText.setVisibility(View.GONE);
            }
        });
    }

    private void updateValuePosition() {
        int progress = mProgressBar.getProgress();
        mProgressValueText.setText(String.valueOf(progress));
        float fontSize = progress > 0 ? mProgressFontSize : mProgressFontSize - 1;
        mProgressValueText.setTextSize(fontSize);

        Rect bounds = mProgressBar.getProgressDrawable().getBounds();
        int margin = ((RelativeLayout.LayoutParams)mProgressBar.getLayoutParams()).leftMargin;
        int progressOffset = mProgressBar.getThumbOffset();
        float measureText = mProgressValueText.getWidth();
        float tmpWidth = ResoureUtils.px2dip(getContext(), measureText)/2;
        int min = mProgressBar.getProgessMin();
        float factor = (mProgressBar.getProgress() - min)*1.0f/(mProgressBar.getMax() - min);
        int xOffset = (int) (bounds.width() * factor + margin + progressOffset - tmpWidth);
        LayoutParams params = (RelativeLayout.LayoutParams)mProgressValueText.getLayoutParams();
        params.leftMargin = xOffset;
        mProgressValueText.setLayoutParams(params);
    }

    public void updateProgressViewData(TabItemInfo itemInfo, int defaultValue) {
        mCurItemInfo = itemInfo;

        mProgressBar.setMax(itemInfo.progressMax);
        mProgressBar.setProgessMin(itemInfo.progressMin);
        mProgressBar.setProgress(defaultValue);
    }

    public void setOnProgressChangeListener(OnProgressChangedListener listener) {
        mOnProgressListener = listener;
    }

    public interface OnProgressChangedListener {
        void onProgressChanged(TabItemInfo tabItemInfo, int value);
    }
}
