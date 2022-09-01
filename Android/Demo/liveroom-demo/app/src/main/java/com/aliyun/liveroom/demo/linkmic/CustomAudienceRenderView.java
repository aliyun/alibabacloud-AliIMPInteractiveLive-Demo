package com.aliyun.liveroom.demo.linkmic;

import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.alibaba.dingpaas.room.RoomDetail;
import com.aliyun.liveroom.demo.R;
import com.aliyun.roompaas.base.callback.Callbacks;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.base.util.CollectionUtil;
import com.aliyun.roompaas.roombase.Const;
import com.aliyun.roompaas.uibase.util.ViewUtil;
import com.aliyun.standard.liveroom.lib.LiveContext;
import com.aliyun.standard.liveroom.lib.component.BaseComponent;
import com.aliyun.standard.liveroom.lib.component.ComponentHolder;
import com.aliyun.standard.liveroom.lib.component.IComponent;
import com.aliyun.standard.liveroom.lib.linkmic.AudienceService;
import com.aliyun.standard.liveroom.lib.linkmic.impl.SampleLinkMicEventHandler;
import com.aliyun.standard.liveroom.lib.linkmic.model.LinkMicUserModel;
import com.aliyun.standard.liveroom.lib.wrapper.LivePlayerServiceExtends;

import org.webrtc.sdk.SophonSurfaceView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 直播间连麦组件
 *
 * @author puke
 * @version 2022/1/10
 */
public class CustomAudienceRenderView extends RelativeLayout implements ComponentHolder {

    private final Component component = new Component();

    private final ViewGroup bigRenderContainer;
    private final IMicRenderContainer smallRenderContainer;
    private final Button apply;
    private final Button mic;
    private final Button camera;

    private final String myUserId;

    private boolean isApplying;

    public CustomAudienceRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_live_linkmic_audience_view, this);
        bigRenderContainer = findViewById(R.id.big_render_container);
        smallRenderContainer = findViewById(R.id.small_render_container);
        mic = findViewById(R.id.mic);
        camera = findViewById(R.id.camera);
        myUserId = Const.getCurrentUserId();

        apply = findViewById(R.id.apply);
        apply.setOnClickListener(v -> {
            AudienceService audienceService = component.audienceService;
            if (isApplying) {
                audienceService.cancelApply(new Callbacks.Toast<>(context, "取消申请连麦"));
            } else {
                audienceService.apply(new Callbacks.Toast<>(context, "申请连麦"));
            }
            isApplying = !isApplying;
            refreshButtonUI();
        });
        findViewById(R.id.switch_camera).setOnClickListener(v ->
                component.audienceService.switchCamera()
        );
        mic.setOnClickListener(v -> {
            AudienceService audienceService = component.audienceService;
            if (audienceService.isMicOpened()) {
                audienceService.closeMic();
            } else {
                if (audienceService.isSelfMicAllowed()) {
                    audienceService.openMic();
                } else {
                    component.showToast("主播开启了全员禁音, 无法打开麦克风");
                    return;
                }
            }
            refreshButtonUI();
            smallRenderContainer.update(myUserId, false);
        });
        camera.setOnClickListener(v -> {
            AudienceService audienceService = component.audienceService;
            if (audienceService.isCameraOpened()) {
                component.audienceService.closeCamera();
            } else {
                component.audienceService.openCamera();
            }
            refreshButtonUI();
            smallRenderContainer.update(myUserId, true);
        });
        findViewById(R.id.leave).setOnClickListener(v -> component.audienceService.leave());
    }

    private void refreshButtonUI() {
        AudienceService audienceService = component.audienceService;
        if (audienceService == null) {
            return;
        }

        if (audienceService.isJoined()) {
            apply.setVisibility(GONE);
        } else {
            apply.setVisibility(VISIBLE);
            apply.setText(isApplying ? "取消申请连麦" : "申请连麦");
        }

        if (audienceService.isCameraOpened()) {
            camera.setText("关闭摄像头");
        } else {
            camera.setText("打开摄像头");
        }

        if (audienceService.isMicOpened()) {
            mic.setText("关闭麦克风");
        } else {
            mic.setText("打开麦克风");
        }
    }

    private LinkMicUserModel getUser(String userId) {
        Map<String, LinkMicUserModel> joinedUsers = component.audienceService.getJoinedUsers();
        return joinedUsers.get(userId);
    }

    @Override
    public IComponent getComponent() {
        return component;
    }

    private class Component extends BaseComponent {

        private AudienceService audienceService;
        private LivePlayerServiceExtends playerService;

        @Override
        public void onInit(LiveContext liveContext) {
            super.onInit(liveContext);
            audienceService = roomChannel.getLinkMicService().getAudienceService();
            playerService = liveService.getPlayerService();

            audienceService.addEventHandler(new SampleLinkMicEventHandler() {
                @Override
                public void onJoinedSuccess(View view) {
                    isApplying = false;
                    refreshButtonUI();
                    // 加入连麦之后
                    playerService.stopPlay();
                    // 移除旁路流
                    bigRenderContainer.removeAllViews();
                }


                @Override
                public void onLeftSuccess() {
                    isApplying = false;
                    refreshButtonUI();

                    smallRenderContainer.removeAll();
                    playBypassLive();
                }

                @Override
                public void onUserJoined(List<LinkMicUserModel> users) {
                    if (CollectionUtil.isEmpty(users) && !audienceService.isJoined()) {
                        return;
                    }

                    List<LinkMicUserModel> audiences = new ArrayList<>();
                    for (LinkMicUserModel user : users) {
                        if (user.isAnchor) {
                            // 主播
                            updateRenderContainer(user.cameraView);
                        } else {
                            // 观众
                            audiences.add(user);
                        }
                    }

                    if (CollectionUtil.isNotEmpty(audiences)) {
                        // 更新观众视图
                        smallRenderContainer.add(audiences);
                    }
                }

                @Override
                public void onUserLeft(List<LinkMicUserModel> users) {
                    if (CollectionUtil.isEmpty(users)) {
                        return;
                    }

                    for (LinkMicUserModel user : users) {
                        if (user.isAnchor) {
                            updateRenderContainer(null);
                        } else {
                            smallRenderContainer.remove(user.userId);
                        }
                    }
                }

                @Override
                public void onKicked(List<LinkMicUserModel> users) {
                    if (CollectionUtil.isEmpty(users)) {
                        return;
                    }

                    for (LinkMicUserModel user : users) {
                        if (user.isAnchor) {
                            updateRenderContainer(null);
                        } else {
                            smallRenderContainer.remove(user.userId);
                        }
                    }
                }

                @Override
                public void onApplyResponse(boolean approve, String userId) {
                    // 观众收到主播对观众申请连麦的处理结果 (同意或拒绝)
                    boolean isSelf = TextUtils.equals(userId, Const.getCurrentUserId());
                    showToast(String.format("主播%s了%s的连麦申请",
                            approve ? "同意" : "拒绝",
                            isSelf ? "您" : userId
                    ));

                    // 主播同意我的申请时, 进行处理 (!!!注意比较, uid可能是别人)
                    if (isSelf) {
                        if (approve) {
                            // 这里需要手动调用该方法才会执行连麦操作
                            audienceService.handleApplyResponse(true);
                        } else {
                            // 主播拒绝后, 申请连麦状态重置
                            isApplying = false;
                            refreshButtonUI();
                        }
                    }
                }

                @Override
                public void onInvited(LinkMicUserModel inviter, List<LinkMicUserModel> invitedUsers) {
                    if (CollectionUtil.isEmpty(invitedUsers)) {
                        return;
                    }

                    boolean selfIsInvited = false;
                    List<String> invitedOtherIds = new ArrayList<>();
                    for (LinkMicUserModel invitedUser : invitedUsers) {
                        String invitedUserId = invitedUser.userId;
                        boolean isSelf = TextUtils.equals(invitedUserId, myUserId);
                        if (isSelf) {
                            // 自己被邀请
                            selfIsInvited = true;
                        } else {
                            // 别人被邀请
                            invitedOtherIds.add(invitedUserId);
                        }
                    }

                    // 别人被邀请, 弹toast提示
                    if (CollectionUtil.isNotEmpty(invitedOtherIds)) {
                        final String invitedInfo;
                        if (invitedOtherIds.size() == 1) {
                            invitedInfo = invitedOtherIds.get(0);
                        } else {
                            invitedInfo = TextUtils.join(", ", invitedOtherIds);
                        }
                        showToast(String.format("%s对%s发出连麦邀请", inviter.userId, invitedInfo));
                    }

                    // 自己被邀请, 弹确认框
                    if (selfIsInvited) {
                        new AlertDialog.Builder(getContext())
                                .setTitle(String.format("%s邀请您加入连麦，是否同意？", inviter.userId))
                                .setPositiveButton("同意", (dialog, which) -> {
                                    audienceService.handleInvite(true);
                                })
                                .setNegativeButton("拒绝", (dialog, which) -> {
                                    audienceService.handleInvite(false);
                                })
                                .setCancelable(false)
                                .show();
                    }
                }

                @Override
                public void onRemoteCameraStateChanged(String userId, boolean open) {
                    // 更新摄像头状态
                    LinkMicUserModel user = getUser(userId);
                    if (user.isAnchor) {
                        updateRenderContainer(user.cameraView);
                    } else {
                        smallRenderContainer.update(userId, true);
                    }
                }

                @Override
                public void onRemoteMicStateChanged(String userId, boolean open) {
                    // 更新麦克风状态
                    smallRenderContainer.update(userId, false);
                }

                @Override
                public void onSelfMicAllowed(boolean allowed) {
                    showToast(String.format("主播%s了全员禁音", allowed ? "取消" : "开启"));
                }
            });
        }

        @Override
        public void onEnterRoomSuccess(RoomDetail roomDetail) {
            super.onEnterRoomSuccess(roomDetail);
            playBypassLive();
        }

        private void playBypassLive() {
            // 刚进房间时, 尝试拉取主播视频流
            playerService.setViewContentMode(getOpenLiveParam().liveShowMode);
            playerService.tryPlay(new Callback<View>() {
                @Override
                public void onSuccess(View view) {
                    updateRenderContainer(view);
                }

                @Override
                public void onError(String errorMsg) {
                }
            });
        }

        private void updateRenderContainer(View toAdd) {
            if (toAdd != null && toAdd.getParent() == bigRenderContainer) {
                return;
            }

            if (toAdd instanceof SophonSurfaceView ) {
                ((SophonSurfaceView) toAdd).setZOrderMediaOverlay(false);
            }
            ViewUtil.removeSelfSafely(ViewUtil.findFirstSurfaceViewAtLevel0(bigRenderContainer));
            ViewUtil.addChildMatchParentSafely(true, bigRenderContainer, 0, toAdd);
        }

        @Override
        public void onActivityDestroy() {
            if (audienceService.isJoined()) {
                audienceService.leave();
            }
        }
    }
}
