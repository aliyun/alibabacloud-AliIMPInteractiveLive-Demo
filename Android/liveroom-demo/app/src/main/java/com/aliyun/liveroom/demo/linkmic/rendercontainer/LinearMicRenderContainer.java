package com.aliyun.liveroom.demo.linkmic.rendercontainer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aliyun.liveroom.demo.R;
import com.aliyun.liveroom.demo.linkmic.IMicRenderContainer;
import com.aliyun.roompaas.uibase.util.ViewUtil;
import com.aliyun.standard.liveroom.lib.linkmic.model.LinkMicUserModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 线性布局的连麦视图容器
 *
 * @author puke
 * @version 2022/1/14
 */
public class LinearMicRenderContainer extends LinearLayout implements IMicRenderContainer {

    private final Map<String, View> userId2ItemView = new HashMap<>();

    public LinearMicRenderContainer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void add(List<LinkMicUserModel> users) {
        for (LinkMicUserModel user : users) {
            add(user);
        }
    }

    @Override
    public LinkMicUserModel getUser(String userId) {
        ItemView itemView = (ItemView) userId2ItemView.get(userId);
        return itemView == null ? null : itemView.user;
    }

    private void add(LinkMicUserModel user) {
        ItemView itemView = (ItemView) userId2ItemView.get(user.userId);
        if (itemView == null) {
            View view = new ItemView(getContext(), user);
            addView(view);
            userId2ItemView.put(user.userId, view);
            update(getChildCount() - 1, true);
        } else {
            update(indexOfChild(itemView), true);
        }
    }

    @Override
    public void remove(String userId) {
        int index = getIndex(userId);
        if (isInValidIndex(index)) {
            return;
        }

        userId2ItemView.remove(userId);
        removeViewAt(index);
    }

    @Override
    public void removeAll() {
        removeAllViews();
        userId2ItemView.clear();
    }

    @Override
    public void update(String userId, boolean refreshRenderView) {
        int index = getIndex(userId);
        update(index, refreshRenderView);
    }

    private void update(int index, boolean refreshRenderView) {
        if (isInValidIndex(index)) {
            return;
        }

        ItemView itemView = (ItemView) getChildAt(index);
        LinkMicUserModel user = itemView.user;

        View renderView = user.cameraView;

        itemView.userLabel.setText(user.nickname);
        itemView.mic.setText(String.format("麦克风: %s", user.isMicOpen ? "开" : "关"));

        if (refreshRenderView) {
            if (renderView == null || !user.isCameraOpen) {
                itemView.container.removeAllViews();
            } else {
                ViewUtil.addChildMatchParentSafely(itemView.container, renderView);
            }
        }
    }

    private int getIndex(String userId) {
        ItemView itemView = (ItemView) userId2ItemView.get(userId);
        return itemView == null ? -1 : indexOfChild(itemView);
    }

    private boolean isInValidIndex(int index) {
        return index < 0 || index >= getChildCount();
    }

    @SuppressLint("ViewConstructor")
    public static class ItemView extends FrameLayout {

        private final LinkMicUserModel user;
        private final ViewGroup container;
        private final TextView userLabel;
        private final TextView mic;

        public ItemView(@NonNull Context context, LinkMicUserModel user) {
            super(context);
            this.user = user;
            View.inflate(getContext(), R.layout.item_render_view, this);
            container = findViewById(R.id.item_render_container);
            userLabel = findViewById(R.id.item_user);
            mic = findViewById(R.id.item_mic);
        }
    }

}
