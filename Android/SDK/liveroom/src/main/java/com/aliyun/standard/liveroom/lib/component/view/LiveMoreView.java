package com.aliyun.standard.liveroom.lib.component.view;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.alibaba.dingpaas.room.RoomDetail;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.uibase.util.DialogUtil;
import com.aliyun.standard.liveroom.lib.LiveContext;
import com.aliyun.standard.liveroom.lib.R;
import com.aliyun.standard.liveroom.lib.component.BaseComponent;
import com.aliyun.standard.liveroom.lib.component.ComponentHolder;
import com.aliyun.standard.liveroom.lib.component.IComponent;

/**
 * @author puke
 * @version 2021/7/29
 */
public class LiveMoreView extends FrameLayout implements ComponentHolder {

    private final Component component = new Component();
    private final Dialog dialog;

    private boolean isMute = false;
    private boolean isMutePlay = false;
    private boolean isMirror = false;
    private boolean isPlaying = false;
    private boolean isBanAll = false;

    public LiveMoreView(@NonNull Context context) {
        this(context, null, 0);
    }

    public LiveMoreView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LiveMoreView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.ilr_view_live_more, this);

        dialog = DialogUtil.createDialogOfBottom(context, LayoutParams.WRAP_CONTENT,
                R.layout.ilr_view_float_live_more, true);
        setMoreToolbarListener();

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onMore();
            }
        });
    }

    private void onMore() {
        if (!component.isOwner()) {
            dialog.findViewById(R.id.live_tool_switch).setVisibility(View.GONE);
            dialog.findViewById(R.id.live_tool_mirror).setVisibility(View.GONE);
            dialog.findViewById(R.id.live_tool_ban_all).setVisibility(View.GONE);
        }
        dialog.show();
    }

    private void changeBandAllUI(View view) {
        ((TextView) view.findViewById(R.id.live_tool_band_txt)).setText(isBanAll ? "取消禁言" : "全员禁言");
        view.findViewById(R.id.live_tool_band_select).setVisibility(isBanAll ? View.VISIBLE : View.GONE);
    }

    private void setMoreToolbarListener() {
        dialog.findViewById(R.id.live_tool_mute).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (component.isOwner()) {
                    onMuteLive(view);
                } else {
                    onMutePlay(view);
                }
            }
        });
        dialog.findViewById(R.id.live_tool_pause).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (component.isOwner()) {
                    onPauseLive(view);
                } else {
                    onPausePlay(view);
                }
            }
        });
        dialog.findViewById(R.id.live_tool_switch).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onSwitch(v);
            }
        });
        dialog.findViewById(R.id.live_tool_mirror).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onMirrorLive(v);
            }
        });
        dialog.findViewById(R.id.live_tool_ban_all).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                component.handleBanAll(v);
            }
        });
    }

    public void onMuteLive(View view) {
        isMute = !isMute;
        component.getPusherService().setMutePush(isMute);
        ((TextView) view.findViewById(R.id.live_tool_mute_txt)).setText(isMute ? "取消静音" : "静音");
        view.findViewById(R.id.live_tool_mute_select).setVisibility(isMute ? View.VISIBLE : View.GONE);
    }

    public void onMutePlay(View view) {
        isMutePlay = !isMutePlay;
        component.getPlayerService().setMutePlay(isMutePlay);
        ((TextView) view.findViewById(R.id.live_tool_mute_txt)).setText(isMute ? "取消静音" : "静音");
        view.findViewById(R.id.live_tool_mute_select).setVisibility(isMutePlay ? View.VISIBLE : View.GONE);
    }

    public void onPauseLive(View view) {
        if (component.isPushing()) {
            component.getPusherService().pauseLive();
        } else {
            component.getPusherService().resumeLive();
        }
        ((TextView) view.findViewById(R.id.live_tool_pause_txt)).setText(component.isPushing() ? "结束暂停" : "暂停");
        view.findViewById(R.id.live_tool_pause_select).setVisibility(component.isPushing() ? View.VISIBLE : View.GONE);
        component.setPushing(!component.isPushing());
    }

    public void onPausePlay(View view) {
        if (isPlaying) {
            component.getPlayerService().pausePlay();
        } else {
            component.getPlayerService().resumePlay();
        }
        ((TextView) view.findViewById(R.id.live_tool_pause_txt)).setText(isPlaying ? "结束暂停" : "暂停");
        view.findViewById(R.id.live_tool_pause_select).setVisibility(isPlaying ? View.VISIBLE : View.GONE);
        isPlaying = !isPlaying;
    }

    public void onMirrorLive(View view) {
        isMirror = !isMirror;
        component.getPusherService().setPreviewMirror(isMirror);
        component.getPusherService().setPushMirror(isMirror);
        ((TextView) view.findViewById(R.id.live_tool_mirror_txt)).setText(isMirror ? "镜像开" : "开启镜像");
        view.findViewById(R.id.live_tool_mirror_select).setVisibility(isMirror ? View.VISIBLE : View.GONE);
    }

    public void onSwitch(View view) {
        component.getPusherService().switchCamera();
    }

    @Override
    public IComponent getComponent() {
        return component;
    }

    private class Component extends BaseComponent {

        @Override
        public void onInit(LiveContext liveContext) {
            super.onInit(liveContext);
        }

        @Override
        public void onEnterRoomSuccess(RoomDetail roomDetail) {
            // 只有主播端, 且不是回放状态的情况, 才展示更多视图
            boolean showMore = isOwner() && !needPlayback();
            setVisibility(showMore ? VISIBLE : GONE);
        }

        private void handleBanAll(final View view) {
            isBanAll = !isBanAll;
            if (isBanAll) {
                chatService.banAllComment(new Callback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        changeBandAllUI(view);
                    }

                    @Override
                    public void onError(String errorMsg) {
                        isBanAll = false;
                        changeBandAllUI(view);
                    }
                });
            } else {
                chatService.cancelBanAllComment(new Callback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        changeBandAllUI(view);
                    }

                    @Override
                    public void onError(String errorMsg) {
                        isBanAll = true;
                        changeBandAllUI(view);
                    }
                });
            }
        }

        private boolean isPushing() {
            return liveContext.isPushing();
        }

        private void setPushing(boolean isPushing) {
            liveContext.setPushing(isPushing);
        }
    }
}
