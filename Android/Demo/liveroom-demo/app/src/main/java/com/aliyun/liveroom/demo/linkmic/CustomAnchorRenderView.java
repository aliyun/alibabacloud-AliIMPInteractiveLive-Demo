package com.aliyun.liveroom.demo.linkmic;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.alibaba.dingpaas.room.RoomDetail;
import com.aliyun.liveroom.demo.R;
import com.aliyun.roompaas.base.util.CollectionUtil;
import com.aliyun.roompaas.roombase.Const;
import com.aliyun.roompaas.uibase.util.DialogUtil;
import com.aliyun.roompaas.uibase.util.ViewUtil;
import com.aliyun.standard.liveroom.lib.LiveContext;
import com.aliyun.standard.liveroom.lib.component.BaseComponent;
import com.aliyun.standard.liveroom.lib.component.ComponentHolder;
import com.aliyun.standard.liveroom.lib.component.IComponent;
import com.aliyun.standard.liveroom.lib.linkmic.impl.SampleLinkMicEventHandler;
import com.aliyun.standard.liveroom.lib.linkmic.model.LinkMicUserModel;
import com.aliyun.standard.liveroom.lib.wrapper.LinkMicPusherService;

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
public class CustomAnchorRenderView extends RelativeLayout implements ComponentHolder {

    private final Component component = new Component();

    private final ViewGroup bigRenderContainer;
    private final IMicRenderContainer smallRenderContainer;
    private final Button mic;
    private final Button camera;

    private final String myUserId;

    public CustomAnchorRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_live_linkmic_anchor_view, this);
        bigRenderContainer = findViewById(R.id.big_render_container);
        smallRenderContainer = findViewById(R.id.small_render_container);

        mic = findViewById(R.id.mic);
        camera = findViewById(R.id.camera);
        myUserId = Const.getCurrentUserId();

        findViewById(R.id.switch_camera).setOnClickListener(v ->
                component.pusherService.switchCamera()
        );
        mic.setOnClickListener(v -> {
            LinkMicPusherService pusherService = component.pusherService;
            if (pusherService.isMicOpened()) {
                pusherService.closeMic();
            } else {
                if (pusherService.isSelfMicAllowed()) {
                    pusherService.openMic();
                } else {
                    component.showToast("主播开启了全员禁音, 无法打开麦克风");
                    return;
                }
            }
            refreshButtonUI();
            smallRenderContainer.update(myUserId, false);
        });
        camera.setOnClickListener(v -> {
            LinkMicPusherService pusherService = component.pusherService;
            if (pusherService.isCameraOpened()) {
                component.pusherService.closeCamera();
                component.updateRenderContainer(null);
            } else {
                View view = component.pusherService.openCamera();
                component.updateRenderContainer(view);
            }
            refreshButtonUI();
        });
        refreshButtonUI();
    }

    private void refreshButtonUI() {
        LinkMicPusherService pusherService = component.pusherService;
        if (pusherService == null) {
            return;
        }

        if (pusherService.isCameraOpened()) {
            camera.setText("关闭摄像头");
        } else {
            camera.setText("打开摄像头");
        }

        if (pusherService.isMicOpened()) {
            mic.setText("关闭麦克风");
        } else {
            mic.setText("打开麦克风");
        }
    }

    private LinkMicUserModel getUser(String userId) {
        Map<String, LinkMicUserModel> joinedUsers = component.pusherService.getJoinedUsers();
        return joinedUsers.get(userId);
    }
    
    @Override
    public IComponent getComponent() {
        return component;
    }

    private class Component extends BaseComponent {

        private LinkMicPusherService pusherService;

        @Override
        public void onInit(LiveContext liveContext) {
            super.onInit(liveContext);
            pusherService = liveService.getLinkMicPusherService();

            pusherService.addEventHandler(new SampleLinkMicEventHandler() {
                @Override
                public void onJoinedSuccess(View view) {
                    pusherService.closeMic();
                    refreshButtonUI();
                }


                @Override
                public void onLeftSuccess() {
                    refreshButtonUI();
                    smallRenderContainer.removeAll();
                }

                @Override
                public void onUserJoined(List<LinkMicUserModel> users) {
                    if (CollectionUtil.isEmpty(users) && !pusherService.isJoined()) {
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
                        String userId = user.userId;
                        smallRenderContainer.remove(userId);
                    }
                }

                @Override
                public void onKicked(List<LinkMicUserModel> users) {
                    if (CollectionUtil.isEmpty(users)) {
                        return;
                    }

                    for (LinkMicUserModel userId : users) {
                        smallRenderContainer.remove(userId.userId);
                    }
                }

                @Override
                public void onApplied(boolean newApplied, List<LinkMicUserModel> users) {
                    // 收到观众的连麦申请
                    if (newApplied) {
                        LinkMicUserModel user = CollectionUtil.getFirst(users);
                        if (user != null) {
                            String applyUserId = user.userId;
                            String message = String.format("%s申请加入连麦, 是否同意?", applyUserId);
                            DialogUtil.confirm(activity, message,
                                    () -> pusherService.handleApply(applyUserId, true, null),
                                    () -> pusherService.handleApply(applyUserId, false, null)
                            );
                        }
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
            // 开始预览
            pusherService.setPreviewMode(getOpenLiveParam().liveShowMode);
            View renderView = pusherService.openCamera();
            updateRenderContainer(renderView);
        }

        private void updateRenderContainer(View toAdd) {
            if (toAdd != null && toAdd.getParent() == bigRenderContainer) {
                return;
            }

            if (toAdd instanceof SophonSurfaceView) {
                ((SophonSurfaceView) toAdd).setZOrderMediaOverlay(false);
            }
            ViewUtil.removeSelfSafely(ViewUtil.findFirstSurfaceViewAtLevel0(bigRenderContainer));
            ViewUtil.addChildMatchParentSafely(true, bigRenderContainer, 0, toAdd);
        }
    }
}
