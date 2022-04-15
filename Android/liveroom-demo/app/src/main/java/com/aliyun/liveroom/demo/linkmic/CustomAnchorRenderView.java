package com.aliyun.liveroom.demo.linkmic;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.alibaba.dingpaas.room.RoomDetail;
import com.aliyun.liveroom.demo.R;
import com.aliyun.liveroom.demo.linkmic.rendercontainer.GridMicContainer;
import com.aliyun.roompaas.base.util.CollectionUtil;
import com.aliyun.roompaas.roombase.Const;
import com.aliyun.roompaas.uibase.util.DialogUtil;
import com.aliyun.roompaas.uibase.util.ViewUtil;
import com.aliyun.standard.liveroom.lib.LiveContext;
import com.aliyun.standard.liveroom.lib.component.BaseComponent;
import com.aliyun.standard.liveroom.lib.component.ComponentHolder;
import com.aliyun.standard.liveroom.lib.component.IComponent;
import com.aliyun.standard.liveroom.lib.linkmic.AnchorService;
import com.aliyun.standard.liveroom.lib.linkmic.enums.ContentMode;
import com.aliyun.standard.liveroom.lib.linkmic.impl.SampleLinkMicEventHandler;
import com.aliyun.standard.liveroom.lib.linkmic.model.LinkMicUserModel;

import java.util.HashMap;
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

    private final ViewGroup renderContainer;
    private final IMicRenderContainer micRenderContainer;
    private final Button invite;
    private final Button mic;
    private final Button camera;

    private final Map<String, View> userId2View = new HashMap<>();
    private final String myUserId;

    public CustomAnchorRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_live_linkmic_anchor_view, this);
        renderContainer = findViewById(R.id.render_container);
        GridMicContainer micRenderContainer = findViewById(R.id.mic_render_container);
        micRenderContainer.setCallback(userId2View::get);
        micRenderContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            int childCount = micRenderContainer.getChildCount();
            @Override
            public void onGlobalLayout() {
                int currentChildCount = micRenderContainer.getChildCount();
                if (this.childCount != currentChildCount) {
                    this.childCount = currentChildCount;
                    if (currentChildCount == 1) {
                        // 只有自己的画面时, 设置为Fill填充模式
                        component.anchorService.setPreviewContentMode(ContentMode.Fill);
                    } else {
                        // 有别人时, 设置为Crop填充模式
                        component.anchorService.setPreviewContentMode(ContentMode.Crop);
                    }
                }
            }
        });
        this.micRenderContainer = micRenderContainer;

        mic = findViewById(R.id.mic);
        camera = findViewById(R.id.camera);
        myUserId = Const.getCurrentUserId();

        invite = findViewById(R.id.invite);
        invite.setOnClickListener(v -> {
            component.showToast("待实现");
        });
        findViewById(R.id.switch_camera).setOnClickListener(v ->
                component.anchorService.switchCamera()
        );
        mic.setOnClickListener(v -> {
            AnchorService anchorService = component.anchorService;
            if (anchorService.isMicOpened()) {
                anchorService.closeMic();
            } else {
                if (anchorService.isSelfMicAllowed()) {
                    anchorService.openMic();
                } else {
                    component.showToast("主播开启了全员禁音, 无法打开麦克风");
                    return;
                }
            }
            refreshButtonUI();
            LinkMicUserModel user = micRenderContainer.getUser(myUserId);
            if (user != null) {
                user.isMicOpen = anchorService.isMicOpened();
            }
            micRenderContainer.update(myUserId);
        });
        camera.setOnClickListener(v -> {
            AnchorService anchorService = component.anchorService;
            if (anchorService.isCameraOpened()) {
                component.anchorService.closeCamera();
            } else {
                component.anchorService.openCamera();
            }
            refreshButtonUI();
            LinkMicUserModel user = micRenderContainer.getUser(myUserId);
            if (user != null) {
                user.isCameraOpen = anchorService.isCameraOpened();
            }
            micRenderContainer.update(myUserId);
        });
        findViewById(R.id.leave).setOnClickListener(v -> component.anchorService.leave());
    }

    private void refreshButtonUI() {
        AnchorService anchorService = component.anchorService;
        if (anchorService == null) {
            return;
        }

        if (anchorService.isJoined()) {
            invite.setVisibility(VISIBLE);
        } else {
            invite.setVisibility(GONE);
        }

        if (anchorService.isCameraOpened()) {
            camera.setText("关闭摄像头");
        } else {
            camera.setText("打开摄像头");
        }

        if (anchorService.isMicOpened()) {
            mic.setText("关闭麦克风");
        } else {
            mic.setText("打开麦克风");
        }
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
                    anchorService.closeMic();
                    refreshButtonUI();
                    renderContainer.removeAllViews();
                    userId2View.put(Const.getCurrentUserId(), view);
                }


                @Override
                public void onLeftSuccess() {
                    refreshButtonUI();

                    micRenderContainer.removeAll();
                }

                @Override
                public void onUserJoined(boolean newJoined, List<LinkMicUserModel> users) {
                    if (anchorService.isJoined() && CollectionUtil.isNotEmpty(users)) {
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
                public void onCameraStreamAvailable(String userId, boolean isAnchor, View view) {
                    // 存下 userId=>渲染视图 的映射关系
                    userId2View.put(userId, view);
                    // 刷新该userId对应的ItemView
                    micRenderContainer.update(userId);
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
                                    () -> anchorService.handleApply(applyUserId, true, null),
                                    () -> anchorService.handleApply(applyUserId, false, null)
                            );
                        }
                    }
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

                @Override
                public void onSelfMicAllowed(boolean allowed) {
                    showToast(String.format("主播%s了全员禁音", allowed ? "取消" : "开启"));
                }
            });
        }

        @Override
        public void onEnterRoomSuccess(RoomDetail roomDetail) {
            // 开始预览
            anchorService.setPreviewContentMode(ContentMode.Fill);
            View renderView = anchorService.openCamera();
            updateRenderContainer(renderView);
        }

        private void updateRenderContainer(View toAdd) {
            ViewUtil.removeSelfSafely(ViewUtil.findFirstSurfaceViewAtLevel0(renderContainer));
            ViewUtil.addChildMatchParentSafely(true, renderContainer, 0, toAdd);
        }

        @Override
        public void onActivityDestroy() {
            if (anchorService.isJoined()) {
                anchorService.leave();
            }
        }
    }
}
