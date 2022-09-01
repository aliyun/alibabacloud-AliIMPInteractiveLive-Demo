package com.aliyun.standard.liveroom.lib.component.view;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.dingpaas.room.RoomDetail;
import com.alibaba.dingpaas.room.RoomInfo;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.biz.SampleRoomEventHandler;
import com.aliyun.roompaas.uibase.util.AppUtil;
import com.aliyun.roompaas.uibase.util.DialogUtil;
import com.aliyun.standard.liveroom.lib.Actions;
import com.aliyun.standard.liveroom.lib.LiveContext;
import com.aliyun.standard.liveroom.lib.R;
import com.aliyun.standard.liveroom.lib.component.BaseComponent;
import com.aliyun.standard.liveroom.lib.component.ComponentHolder;
import com.aliyun.standard.liveroom.lib.component.IComponent;


/**
 * 直播公告视图, 封装直播视图的展开收起状态
 *
 * @author puke
 * @version 2021/6/30
 */
public class LiveNoticeView extends LinearLayout implements ComponentHolder {

    private static final String PLACE_HOLDER = "向观众介绍你的直播间吧～";

    private final Component component = new Component();
    private final View expand;
    private final View occupy;
    private final View edit;
    private final TextView notice;

    private boolean isExpand = false;
    private String content;

    public LiveNoticeView(@NonNull final Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        setMinimumHeight(AppUtil.dp(20f));
        setBackgroundResource(R.drawable.ilr_bg_live_notice);
        inflate(context, R.layout.ilr_view_live_notice, this);

        expand = findViewById(R.id.view_expand);
        occupy = findViewById(R.id.view_occupy);
        edit = findViewById(R.id.view_edit);
        notice = findViewById(R.id.view_notice);

        setNotice(null);
        refreshView();

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isExpand = !isExpand;
                refreshView();
            }
        });

        edit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!component.isOwner()) {
                    return;
                }

                String currentNotice = getNotice();
                DialogUtil.input(context, "更改房间公告", currentNotice, new DialogUtil.InputCallback() {
                    @Override
                    public void onInput(String value) {
                        component.updateNotice(value);
                    }
                });
            }
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
            occupy.setVisibility(VISIBLE);
            edit.setVisibility(component.isOwner() ? VISIBLE : GONE);
            notice.setVisibility(VISIBLE);
        } else {
            // 收起
            expand.setVisibility(VISIBLE);
            occupy.setVisibility(GONE);
            edit.setVisibility(GONE);
            notice.setVisibility(GONE);
        }

        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (layoutParams != null) {
            layoutParams.width = isExpand ? AppUtil.dp(166) : ViewGroup.LayoutParams.WRAP_CONTENT;
            setLayoutParams(layoutParams);
        }
    }

    public String getNotice() {
        return content;
    }

    @Override
    public IComponent getComponent() {
        return component;
    }

    private class Component extends BaseComponent {
        @Override
        public void onInit(LiveContext liveContext) {
            super.onInit(liveContext);
            // 监听房间事件
            roomChannel.addEventHandler(new SampleRoomEventHandler() {
                @Override
                public void onRoomNoticeChanged(String notice) {
                    setNotice(notice);
                }
            });
        }

        @Override
        public void onEnterRoomSuccess(RoomDetail roomDetail) {
            refreshView();
            RoomInfo roomInfo = roomDetail.roomInfo;
            if (roomInfo != null) {
                setNotice(roomInfo.notice);
            }
        }

        private void updateNotice(String notice) {
            roomChannel.updateNotice(notice, new Callback<Void>() {
                @Override
                public void onSuccess(Void data) {

                }

                @Override
                public void onError(String errorMsg) {
                    component.showToast("修改公告失败: " + errorMsg);
                }
            });
        }

        @Override
        public void onEvent(String action, Object... args) {
            if (Actions.EMPTY_SPACE_CLICK.equals(action)) {
                // 点击空白区域时, 收起展开的公告面板
                setExpand(false);
            }
        }
    }
}
