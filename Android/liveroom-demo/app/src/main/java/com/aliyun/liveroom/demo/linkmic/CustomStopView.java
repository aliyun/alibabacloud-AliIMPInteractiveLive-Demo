package com.aliyun.liveroom.demo.linkmic;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.alibaba.dingpaas.room.RoomDetail;
import com.aliyun.liveroom.demo.R;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.uibase.util.DialogUtil;
import com.aliyun.standard.liveroom.lib.component.BaseComponent;
import com.aliyun.standard.liveroom.lib.component.ComponentHolder;
import com.aliyun.standard.liveroom.lib.component.IComponent;
import com.aliyun.standard.liveroom.lib.linkmic.AnchorService;
import com.aliyun.standard.liveroom.lib.linkmic.CommonService;
import com.aliyun.standard.liveroom.lib.linkmic.LinkMicService;

/**
 * @author puke
 * @version 2022/4/14
 */
public class CustomStopView extends AppCompatImageView implements ComponentHolder {

    private final Component component = new Component();

    public CustomStopView(@NonNull Context context) {
        this(context, null, 0);
    }

    public CustomStopView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomStopView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setImageResource(R.drawable.ilr_icon_close);
        setVisibility(VISIBLE);
        setOnClickListener(v -> component.readyClose());
    }

    @Override
    public IComponent getComponent() {
        return component;
    }

    private class Component extends BaseComponent {

        private CommonService commonService;

        @Override
        public void onEnterRoomSuccess(RoomDetail roomDetail) {
            super.onEnterRoomSuccess(roomDetail);
            LinkMicService linkMicService = roomChannel.getLinkMicService();
            if (isOwner()) {
                commonService = linkMicService.getAnchorService();
            } else {
                commonService = linkMicService.getAudienceService();
            }
        }

        @Override
        public boolean interceptBackKey() {
            readyClose();
            return true;
        }

        private void readyClose() {
            boolean isOwner = roomChannel != null && roomChannel.isOwner();
            if (isOwner && commonService.isJoined()) {
                // 对于已经上麦的主播, 关闭页面时, 需要弹窗二次确认
                DialogUtil.showCustomDialog(
                        getContext(), "还有观众正在路上，确定要结束直播吗？",
                        () -> doClose(true), null
                );
            } else {
                // 对于观众和未上麦的主播, 无需弹窗
                doClose(isOwner);
            }
        }

        private void doClose(boolean isAnchor) {
            if (isAnchor) {
                // 主播离开时, 执行结束直播处理
                ((AnchorService) commonService).stopLive(new Callback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                    }

                    @Override
                    public void onError(String errorMsg) {
                        showToast("结束直播失败: " + errorMsg);
                    }
                });
            }
            activity.finish();
        }
    }
}
