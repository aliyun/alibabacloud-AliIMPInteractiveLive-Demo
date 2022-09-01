package com.aliyun.liveroom.demo.custom;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.dingpaas.chat.GetTopicInfoRsp;
import com.alibaba.dingpaas.room.RoomDetail;
import com.alibaba.dingpaas.room.RoomInfo;
import com.alibaba.dingpaas.scenelive.SceneGetLiveDetailRsp;
import com.aliyun.liveroom.demo.R;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.biz.RoomEngine;
import com.aliyun.roompaas.biz.SampleRoomEventHandler;
import com.aliyun.roompaas.biz.exposable.RoomSceneLive;
import com.aliyun.roompaas.biz.exposable.event.RoomInOutEvent;
import com.aliyun.roompaas.biz.exposable.model.Result;
import com.aliyun.roompaas.chat.SampleChatEventHandler;
import com.aliyun.roompaas.chat.exposable.event.LikeEvent;
import com.aliyun.roompaas.uibase.util.AppUtil;
import com.aliyun.standard.liveroom.lib.Actions;
import com.aliyun.standard.liveroom.lib.LiveContext;
import com.aliyun.standard.liveroom.lib.component.BaseComponent;
import com.aliyun.standard.liveroom.lib.component.ComponentHolder;
import com.aliyun.standard.liveroom.lib.component.IComponent;
import com.bumptech.glide.Glide;

import java.util.Locale;

/**
 * 自定义直播信息组件 (位于直播间左上角, 包含主播头像、直播标题、观看人数和点赞数信息)
 *
 * @author puke
 * @version 2021/12/13
 */
public class CustomLiveInfoView extends FrameLayout implements ComponentHolder {

    private final Component component = new Component();
    private final TextView title;
    private final TextView viewCount;
    private final TextView likeCount;

    public CustomLiveInfoView(@NonNull final Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setMinimumHeight(AppUtil.dp(42));
        setBackgroundResource(R.drawable.ilr_bg_anchor_profile);
        inflate(context, R.layout.view_live_info, this);

        title = findViewById(R.id.view_title);
        viewCount = findViewById(R.id.view_view_count);
        likeCount = findViewById(R.id.view_like_count);

        // 加载网络图片
        ImageView avatar = findViewById(R.id.view_avatar);
        Glide.with(context).load("http://goo.gl/gEgYUd").into(avatar);

        // 设置长按后的组件提示
        LongClickHelper.attach(this);
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
        } else if (number >= 10000) {
            // 1w+ 格式化
            return String.format(Locale.getDefault(), "%.1fw", number / 10000f);
        } else {
            return String.valueOf(number);
        }
    }

    @Override
    public IComponent getComponent() {
        return component;
    }

    private class Component extends BaseComponent {

        @Override
        public void onInit(LiveContext liveContext) {
            super.onInit(liveContext);
            // 监听房间基本信息变化
            roomChannel.addEventHandler(new SampleRoomEventHandler() {
                @Override
                public void onEnterOrLeaveRoom(RoomInOutEvent event) {
                    setViewCount(event.pv);
                }
            });

            // 监听互动信息变化
            chatService.addEventHandler(new SampleChatEventHandler() {
                @Override
                public void onLikeReceived(LikeEvent event) {
                    setLikeCount(event.likeCount);
                }
            });
        }

        @Override
        public void onEnterRoomSuccess(RoomDetail roomDetail) {
            // 进入房间后, 填充房间基本信息
            RoomInfo roomInfo = roomDetail.roomInfo;
            if (roomInfo != null) {
                setViewCount(roomInfo.pv);
            }

            // 查询场景化直播详情
            String liveId = liveService.getInstanceId();
            if (!TextUtils.isEmpty(liveId)) {
                Result<RoomSceneLive> result = RoomEngine.getInstance().getRoomSceneLive();
                if (!result.success) {
                    showToast(result.errorMsg);
                    return;
                }

                result.value.getLiveDetail(liveId, new Callback<SceneGetLiveDetailRsp>() {
                    @Override
                    public void onSuccess(SceneGetLiveDetailRsp data) {
                        setTitle(data.anchorNick);
                    }

                    @Override
                    public void onError(String errorMsg) {

                    }
                });
            }
        }

        @Override
        public void onEvent(String action, Object... args) {
            // 监听进入直播间后, 查询到互动信息的回调, 设置初始的点赞数
            if (Actions.GET_CHAT_DETAIL_SUCCESS.equals(action)) {
                GetTopicInfoRsp chatDetail = chatService.getChatDetail();
                if (chatDetail != null) {
                    setLikeCount(chatDetail.likeCount);
                }
            }
        }
    }
}
