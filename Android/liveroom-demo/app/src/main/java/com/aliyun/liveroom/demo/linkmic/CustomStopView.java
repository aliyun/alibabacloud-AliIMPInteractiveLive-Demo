package com.aliyun.liveroom.demo.linkmic;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;

import com.aliyun.liveroom.demo.R;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.uibase.util.DialogUtil;
import com.aliyun.standard.liveroom.lib.LiveContext;
import com.aliyun.standard.liveroom.lib.component.BaseComponent;
import com.aliyun.standard.liveroom.lib.component.ComponentHolder;
import com.aliyun.standard.liveroom.lib.component.IComponent;
import com.aliyun.standard.liveroom.lib.linkmic.AnchorService;
import com.aliyun.standard.liveroom.lib.linkmic.impl.SampleLinkMicEventHandler;

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
        setVisibility(GONE);

        setOnClickListener(v -> component.showUpStopLiveConfirmDialog(true));
    }

    @Override
    public IComponent getComponent() {
        return component;
    }

    private class Component extends BaseComponent {

        private AnchorService anchorService;

        @Override
        public void onInit(LiveContext liveContext) {
            super.onInit(liveContext);
            anchorService = roomChannel.getLinkMicService().getAnchorService();

            anchorService.addEventHandler(new SampleLinkMicEventHandler() {
                @Override
                public void onJoinedSuccess(View view) {
//                    setVisibility(View.VISIBLE);
                }
            });
        }

        @Override
        public boolean interceptBackKey() {
            boolean isOwner = roomChannel != null && roomChannel.isOwner();
            boolean shouldShowConfirm = isOwner && anchorService.isJoined();
            if (shouldShowConfirm) {
                showUpStopLiveConfirmDialog(true);
                return true;
            }
            return false;
        }

        private void showUpStopLiveConfirmDialog(final boolean needFinish) {
            stopConfirmDialog(() -> onLiveExitProcess(needFinish));
        }

        private void stopConfirmDialog(@NonNull Runnable action) {
            DialogUtil.showCustomDialog(getContext(), "还有观众正在路上，确定要结束直播吗？", action, null);
        }

        private void onLiveExitProcess(boolean needFinish) {
            anchorService.stopLive(new Callback<Void>() {
                @Override
                public void onSuccess(Void data) {
                }

                @Override
                public void onError(String errorMsg) {
                    showToast("结束直播失败: " + errorMsg);
                }
            });

            if (needFinish) {
                activity.finish();
            }
        }
    }
}
