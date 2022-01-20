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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 样板间连麦组件
 *
 * @author puke
 * @version 2022/1/10
 */
public class CustomLiveLinkMicView extends RelativeLayout implements ComponentHolder {

    private final Component component = new Component();

    private final ViewGroup renderContainer;
    private final IMicRenderContainer micRenderContainer;
    private final Button mic;
    private final Button camera;

    private final Map<String, View> userId2View = new HashMap<>();
    private final String myUserId;

    public CustomLiveLinkMicView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_live_linkmic_view, this);
        renderContainer = findViewById(R.id.render_container);
        micRenderContainer = findViewById(R.id.mic_render_container);
        micRenderContainer.setCallback(userId2View::get);
        mic = findViewById(R.id.mic);
        camera = findViewById(R.id.camera);
        myUserId = Const.getCurrentUserId();

        findViewById(R.id.apply).setOnClickListener(v ->
                component.audienceService.apply(new ToastCallback<>("申请连麦"))
        );
        findViewById(R.id.switch_camera).setOnClickListener(v ->
                component.audienceService.switchCamera()
        );
        mic.setOnClickListener(v -> {
            AudienceService audienceService = component.audienceService;
            if (audienceService.isOpenMic()) {
                audienceService.closeMic();
            } else {
                audienceService.openMic();
            }
            refreshButtonUI();
            LinkMicUserModel user = micRenderContainer.getUser(myUserId);
            if (user != null) {
                user.isMicOpen = audienceService.isOpenMic();
            }
            micRenderContainer.update(myUserId);
        });
        camera.setOnClickListener(v -> {
            AudienceService audienceService = component.audienceService;
            if (audienceService.isOpenCamera()) {
                component.audienceService.closeCamera();
            } else {
                component.audienceService.openCamera();
            }
            refreshButtonUI();
            LinkMicUserModel user = micRenderContainer.getUser(myUserId);
            if (user != null) {
                user.isCameraOpen = audienceService.isOpenCamera();
            }
            micRenderContainer.update(myUserId);
        });
        findViewById(R.id.leave).setOnClickListener(v -> component.audienceService.leave());
    }

    private void refreshButtonUI() {
        AudienceService audienceService = component.audienceService;
        if (audienceService == null) {
            return;
        }

        if (audienceService.isOpenCamera()) {
            camera.setText("关闭摄像头");
        } else {
            camera.setText("打开摄像头");
        }

        if (audienceService.isOpenMic()) {
            mic.setText("关闭麦克风");
        } else {
            mic.setText("打开麦克风");
        }
    }

    @Override
    public IComponent getComponent() {
        return component;
    }

    private class ToastCallback<T> implements Callback<T> {

        final String action;

        ToastCallback(String action) {
            this.action = action;
        }

        @Override
        public void onSuccess(T t) {
            component.showToast(String.format("%s成功", action));
        }

        @Override
        public void onError(String errorMsg) {
            component.showToast(String.format("%s失败, %s", action, errorMsg));
        }
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
                    audienceService.closeMic();
                    refreshButtonUI();
                    // 加入连麦之后
                    playerService.stopPlay();
                    // 移除旁路流
                    renderContainer.removeAllViews();
                    userId2View.put(Const.getCurrentUserId(), view);
                }


                @Override
                public void onLeftSuccess() {
                    micRenderContainer.removeAll();
                    playBypassLive();
                }

                @Override
                public void onUserJoined(boolean newJoined, List<LinkMicUserModel> users) {
                    if (audienceService.isJoined() && CollectionUtil.isNotEmpty(users)) {
                        micRenderContainer.add(users);
                    }
                }

                @Override
                public void onUserLeft(List<LinkMicUserModel> users) {
                    if (CollectionUtil.isEmpty(users)) {
                        return;
                    }

                    for (LinkMicUserModel user : users) {
                        String userId = user.userId;
                        micRenderContainer.remove(userId);
                    }
                }

                @Override
                public void onKicked(List<LinkMicUserModel> users) {
                    if (CollectionUtil.isEmpty(users)) {
                        return;
                    }

                    for (LinkMicUserModel userId : users) {
                        micRenderContainer.remove(userId.userId);
                    }
                }

                @Override
                public void onApplyResponse(boolean approve, String userId) {
                    showToast(String.format("老师%s了你的连麦申请", approve ? "同意" : "拒绝"));
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
                public void onCameraStreamAvailable(String userId, boolean isAnchor, View view) {
                    // 存下 userId=>渲染视图 的映射关系
                    userId2View.put(userId, view);
                    // 刷新该userId对应的ItemView
                    micRenderContainer.update(userId);
                }

                @Override
                public void onRemoteCameraStateChanged(String userId, boolean open) {
                    // 更新摄像头状态
                    LinkMicUserModel user = micRenderContainer.getUser(userId);
                    if (user != null) {
                        user.isCameraOpen = open;
                    }
                    micRenderContainer.update(userId);
                }

                @Override
                public void onRemoteMicStateChanged(String userId, boolean open) {
                    // 更新麦克风状态
                    LinkMicUserModel user = micRenderContainer.getUser(userId);
                    if (user != null) {
                        user.isMicOpen = open;
                    }
                    micRenderContainer.update(userId);
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
            playerService.tryPlay(new Callback<View>() {
                @Override
                public void onSuccess(View view) {
                    updateRoadRender(view);
                }

                @Override
                public void onError(String errorMsg) {
                }
            });
        }

        private void updateRoadRender(View toAdd) {
            ViewUtil.removeSelfSafely(ViewUtil.findFirstSurfaceViewAtLevel0(renderContainer));
            ViewUtil.addChildMatchParentSafely(true, renderContainer, 0, toAdd);
        }
    }
}
