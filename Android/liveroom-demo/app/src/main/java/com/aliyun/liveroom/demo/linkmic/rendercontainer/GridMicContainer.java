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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * 表格布局的连麦视图容器
 *
 * @author puke
 * @version 2022/1/19
 */
public class GridMicContainer extends RecyclerView implements IMicRenderContainer {

    // 最多展示的连麦画面数量
    private static final int MAX_COUNT = 10;
    // 主播占位符
    private static final LinkMicUserModel OCCUPY_ANCHOR = new LinkMicUserModel();

    private final List<LinkMicUserModel> users = new ArrayList<>();
    private final Adapter adapter;

    // 只有一个视图时, 展示大图
    private boolean showLargeWhenSingle = true;
    private Callback callback;

    public GridMicContainer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        final int spanCount = 3;
        GridLayoutManager layoutManager = new GridLayoutManager(context, spanCount);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (needShowLarge()) {
                    return spanCount;
                }

                // 首项是主播占两格, 其他占一格
                if (position == 0) {
                    return 2;
                }
                return 1;
            }
        });
        setLayoutManager(layoutManager);
        adapter = new Adapter();
        setAdapter(adapter);
    }

    @Override
    public void add(List<LinkMicUserModel> addedUsers) {
        // 排序, 主播排前面
        Collections.sort(addedUsers, (o1, o2) -> {
            if (o1.isAnchor ^ o2.isAnchor) {
                return o1.isAnchor ? -1 : 1;
            }
            return 0;
        });

        LinkMicUserModel first = CollectionUtil.getFirst(addedUsers);
        if (first != null && first.isAnchor) {
            // 新加用户中包含主播
            int anchorIndex = users.indexOf(OCCUPY_ANCHOR);
            if (anchorIndex >= 0) {
                // 当前列表包含占位, 将占位替换为主播数据
                addedUsers.remove(first);
                users.remove(anchorIndex);
                users.add(anchorIndex, first);
                adapter.notifyItemChanged(anchorIndex);
            } else {
                // 当前列表不含占位, 不需要额外处理
            }
        } else {
            // 新加用户中不含主播
            if (users.isEmpty()) {
                // 首次添加, 手动塞一个占位到第一项代表主播
                addedUsers.add(0, OCCUPY_ANCHOR);
            }
        }

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

        LinkMicUserModel removed = users.get(index);
        if (removed.isAnchor) {
            // 是主播, 不能删除, 要做占位替换
            users.remove(index);
            users.add(index, OCCUPY_ANCHOR);
//            adapter.notifyItemChanged(index);
        } else {
            // 不是主播, 直接删除
            users.remove(index);
//            adapter.notifyItemRemoved(index);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void removeAll() {
        users.clear();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void update(String userId) {
        int index = getIndex(userId);
        if (isValidIndex(index)) {
            adapter.notifyItemChanged(index);
        }
    }

    @Override
    public LinkMicUserModel getUser(String userId) {
        int index = getIndex(userId);
        if (!isValidIndex(index)) {
            return null;
        }
        return users.get(index);
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

    @Nullable
    private View getRenderView(String userId) {
        return callback == null ? null : callback.getView(userId);
    }

    @Override
    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    private boolean needShowLarge() {
        return adapter.getItemCount() == 1 && showLargeWhenSingle;
    }

    private class Adapter extends RecyclerView.Adapter<ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View itemView = inflater.inflate(R.layout.item_render_view, parent, false);
            itemView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    AppUtil.getDeviceWidth() / 3
            ));
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            boolean needShowLarge = needShowLarge();
            // 设置itemView大小
            ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = needShowLarge ? ViewGroup.LayoutParams.MATCH_PARENT : AppUtil.getDeviceWidth() / 3;
            holder.itemView.setLayoutParams(layoutParams);

            LinkMicUserModel user = users.get(position);

            Random random = new Random();
            holder.itemView.setBackgroundColor(Color.rgb(
                    random.nextInt(255),
                    random.nextInt(255),
                    random.nextInt(255)
            ));

            ViewGroup container = holder.container;
            if (user == OCCUPY_ANCHOR) {
                // 主播数据还没来时, 第一项的展示样式
                holder.userLabel.setText(null);
                holder.mic.setText(null);
                container.removeAllViews();
                container.setVisibility(GONE);
                return;
            }

            if (needShowLarge) {
                // 显示大图时, 不展示边界信息
                holder.userLabel.setText(null);
                holder.mic.setText(null);
            } else {
                holder.userLabel.setText(user.userId);
                holder.mic.setText(String.format("麦克风: %s", user.isMicOpen ? "开" : "关"));
            }

            View renderView = getRenderView(user.userId);

            if (renderView == null || !user.isCameraOpen) {
//            if (renderView == null) {
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
