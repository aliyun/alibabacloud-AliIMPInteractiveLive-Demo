package com.aliyun.liveroom.demo.linkmic.rendercontainer;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;

import com.aliyun.liveroom.demo.R;
import com.aliyun.liveroom.demo.linkmic.IMicRenderContainer;
import com.aliyun.roompaas.base.util.CollectionUtil;
import com.aliyun.roompaas.uibase.util.AppUtil;
import com.aliyun.roompaas.uibase.util.ViewUtil;
import com.aliyun.standard.liveroom.lib.linkmic.model.LinkMicUserModel;

import org.webrtc.sdk.SophonSurfaceView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 表格布局的连麦视图容器
 *
 * @author puke
 * @version 2022/1/19
 */
public class GridMicContainer extends RecyclerView implements IMicRenderContainer {

    private static final String PAYLOAD_IGNORE_RENDER = "ignoreRender";

    // 最多展示的连麦画面数量
    private static final int MAX_COUNT = 25;
    private static final int SPAN_COUNT = 3;
    private final List<LinkMicUserModel> users = new ArrayList<>();
    private final Adapter adapter;

    public GridMicContainer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        GridLayoutManager layoutManager = new GridLayoutManager(context, SPAN_COUNT);
        setLayoutManager(layoutManager);
        adapter = new Adapter();
        setAdapter(adapter);
    }

    @Override
    public void add(List<LinkMicUserModel> addedUsers) {
        for (LinkMicUserModel user : addedUsers) {
            add(user);
        }
    }

    private void add(LinkMicUserModel user) {
        int index = getIndex(user.userId);
        if ((index < 0)) {
            // 新增的用户不在列表中, 直接添加
            users.add(user);
//            adapter.notifyItemInserted(users.size() - 1);
        } else {
            // 新增的用户已在列表中, 只做更新
            users.remove(index);
            users.add(index, user);
//            adapter.notifyItemChanged(index);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void remove(String userId) {
        int removeIndex = getIndex(userId);
        remove(removeIndex);
    }

    private void remove(int index) {
        if (!isValidIndex(index)) {
            return;
        }

        users.remove(index);
        // adapter.notifyItemRemoved(index);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void removeAll() {
        users.clear();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void update(String userId, boolean refreshRenderView) {
        int index = getIndex(userId);
        if (isValidIndex(index)) {
            if (refreshRenderView) {
                adapter.notifyItemChanged(index);
            } else {
                adapter.notifyItemChanged(index, PAYLOAD_IGNORE_RENDER);
            }
        }
    }

    private int getIndex(String userId) {
        for (int i = 0; i < users.size(); i++) {
            LinkMicUserModel user = users.get(i);
            if (TextUtils.equals(userId, user.userId)) {
                return i;
            }
        }
        return -1;
    }

    private boolean isValidIndex(int index) {
        return index >= 0 && index < users.size();
    }

    private class Adapter extends RecyclerView.Adapter<ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View itemView = inflater.inflate(R.layout.item_render_view, parent, false);
            itemView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    AppUtil.getScreenWidth() / SPAN_COUNT
            ));
            Random random = new Random();
            itemView.setBackgroundColor(Color.rgb(
                    random.nextInt(255),
                    random.nextInt(255),
                    random.nextInt(255)
            ));
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull GridMicContainer.ViewHolder holder, int position, @NonNull List<Object> payloads) {
            if (CollectionUtil.isEmpty(payloads)) {
                onBindViewHolder(holder, position);
            } else {
                LinkMicUserModel user = users.get(position);
                if (payloads.contains(PAYLOAD_IGNORE_RENDER)) {
                    holder.userLabel.setText(user.userId);
                    holder.mic.setText(String.format("麦克风: %s", user.isMicOpen ? "开" : "关"));
                }
            }
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            LinkMicUserModel user = users.get(position);
            ViewGroup container = holder.container;
            holder.userLabel.setText(user.nickname);
            holder.mic.setText(String.format("麦克风: %s", user.isMicOpen ? "开" : "关"));

            View renderView = user.cameraView;

            if (renderView == null || !user.isCameraOpen) {
                // 没画面或没开摄像头, 不展示 (没有直接remove, 为了降低的add和remove的性能消耗)
                container.setVisibility(GONE);
                return;
            }

            // 以下case都需要展示出来
            container.setVisibility(VISIBLE);

            ViewParent parent = renderView.getParent();
            if (parent instanceof ViewGroup) {
                if (parent == container) {
                    // 重复刷新时, 不做remove+add处理, 防止闪屏
                    return;
                }
                ((ViewGroup) parent).removeView(renderView);
            }

            // 设置图层盖在上方
            if (renderView instanceof SophonSurfaceView) {
                ((SophonSurfaceView) renderView).setZOrderMediaOverlay(true);
            }
            container.removeAllViews();
            ViewUtil.addChildMatchParentSafely(container, renderView);
        }

        @Override
        public int getItemCount() {
            return Math.min(users.size(), MAX_COUNT);
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        final ViewGroup container;
        final TextView userLabel;
        final TextView mic;

        ViewHolder(View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.item_render_container);
            userLabel = itemView.findViewById(R.id.item_user);
            mic = itemView.findViewById(R.id.item_mic);
        }
    }
}
