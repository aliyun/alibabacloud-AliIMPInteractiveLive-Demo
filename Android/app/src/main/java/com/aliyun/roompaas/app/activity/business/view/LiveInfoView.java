package com.aliyun.roompaas.app.activity.business.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.aliyun.roompaas.app.R;
import com.aliyun.roompaas.app.util.AppUtil;

import java.util.Locale;

/**
 * 直播信息视图, 包含主播头像、直播标题、观看人数和点赞数信息
 *
 * @author puke
 * @version 2021/6/30
 */
public class LiveInfoView extends FrameLayout {

    private final TextView title;
    private final TextView viewCount;
    private final TextView likeCount;

    public LiveInfoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setMinimumHeight(AppUtil.dp(42));
        setBackgroundResource(R.drawable.bg_anchor_profile);
        inflate(context, R.layout.view_live_info, this);

        title = findViewById(R.id.view_title);
        viewCount = findViewById(R.id.view_view_count);
        likeCount = findViewById(R.id.view_like_count);
    }

    /**
     * 设置标题
     *
     * @param text 标题信息
     */
    public void setTitle(String text) {
        this.title.setText(text);
    }

    /**
     * 设置观看人数
     *
     * @param count 观看人数
     */
    public void setViewCount(int count) {
        String value = formatNumber(count);
        viewCount.setText(String.format("%s观看", value));
    }

    /**
     * 设置点赞人数
     *
     * @param count 点赞人数
     */
    public void setLikeCount(int count) {
        String value = formatNumber(count);
        likeCount.setText(String.format("%s点赞", value));
    }

    private String formatNumber(int number) {
        if ((number < 0)) {
            // 兜底保护
            return String.valueOf(0);
        } else if (number > 10000) {
            // 1w+ 格式化
            return String.format(Locale.getDefault(), "%.1fw", number / 10000f);
        } else {
            return String.valueOf(number);
        }
    }
}
