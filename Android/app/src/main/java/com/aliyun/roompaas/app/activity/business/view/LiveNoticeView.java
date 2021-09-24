package com.aliyun.roompaas.app.activity.business.view;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aliyun.roompaas.app.R;
import com.aliyun.roompaas.app.util.AppUtil;

/**
 * 直播公告视图, 封装直播视图的展开收起状态
 *
 * @author puke
 * @version 2021/6/30
 */
public class LiveNoticeView extends LinearLayout {

    private static final String PLACE_HOLDER = "向观众介绍你的直播间吧～";

    private final View expand;
    private final TextView notice;

    private boolean isExpand = false;
    private String content;

    public LiveNoticeView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        setMinimumHeight(AppUtil.dp(17.5f));
        setBackgroundResource(R.drawable.bg_live_notice);
        inflate(context, R.layout.view_live_notice, this);

        expand = findViewById(R.id.view_expand);
        notice = findViewById(R.id.view_notice);

        setNotice(null);
        refreshView();

        setOnClickListener(v -> {
            isExpand = !isExpand;
            refreshView();
        });
    }

    /**
     * 设置公告内容
     *
     * @param content 公告内容
     */
    public void setNotice(String content) {
        this.content = content;
        if (TextUtils.isEmpty(content)) {
            notice.setTextColor(Color.parseColor("#E7E7E7"));
            notice.setText(PLACE_HOLDER);
        } else {
            notice.setTextColor(Color.WHITE);
            notice.setText(content);
        }
    }

    /**
     * @return 是否是展开状态
     */
    public boolean isExpand() {
        return isExpand;
    }

    public void setExpand(boolean expand) {
        if (isExpand != expand) {
            isExpand = expand;
            refreshView();
        }
    }

    private void refreshView() {
        if (isExpand) {
            // 展开
            expand.setVisibility(GONE);
            notice.setVisibility(VISIBLE);
        } else {
            // 收起
            expand.setVisibility(VISIBLE);
            notice.setVisibility(GONE);
        }
    }

    public String getNotice() {
        return content;
    }
}
