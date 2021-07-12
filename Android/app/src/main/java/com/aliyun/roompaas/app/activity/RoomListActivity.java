package com.aliyun.roompaas.app.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.alibaba.dingpaas.room.RoomBasicInfo;
import com.aliyun.roompaas.app.Const;
import com.aliyun.roompaas.app.R;
import com.aliyun.roompaas.app.activity.base.BaseActivity;
import com.aliyun.roompaas.app.api.DestroyRoomApi;
import com.aliyun.roompaas.app.helper.RoomHelper;
import com.aliyun.roompaas.app.helper.Router;
import com.aliyun.roompaas.app.request.DestroyRoomRequest;
import com.aliyun.roompaas.app.util.ClipboardUtil;
import com.aliyun.roompaas.app.util.DialogUtil;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.base.callback.Callbacks;
import com.aliyun.roompaas.base.util.CollectionUtil;
import com.aliyun.roompaas.base.util.ViewUtil;
import com.aliyun.roompaas.biz.RoomEngine;
import com.aliyun.roompaas.biz.exposable.model.RoomParam;

import java.util.ArrayList;
import java.util.List;

/**
 * @author puke
 * @version 2021/5/11
 */
public class RoomListActivity extends BaseActivity {

    private static final int PAGE_SIZE = 10;
    private SwipeRefreshLayout refreshLayout;
    private View startCreateRoom;

    private TabInfo roomType2TabInfo = new TabInfo();
    private Adapter adapter;
    private boolean isRequesting;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_room_list);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                int dataSize = CollectionUtil.size(getDataList());
                if ((lastVisibleItemPosition < 0 || dataSize == 0)) {
                    return;
                }

                if (lastVisibleItemPosition == dataSize - 1) {
                    // 最后一项可见
                    loadData(true);
                }
            }
        });
        adapter = new Adapter();
        recyclerView.setAdapter(adapter);

        notifyRadioChange();

        refreshLayout = findViewById(R.id.refresh_layout);
        refreshLayout.setOnRefreshListener(() -> loadData(false));

        startCreateRoom = findViewById(R.id.startCreateRoom);
        ViewUtil.bindClickActionWithClickCheck(startCreateRoom, this::startCreateRoom);

        TextView startCreateRoomButtonText = findViewById(R.id.startCreateRoomButtonText);
        ViewUtil.applyText(startCreateRoomButtonText, RoomHelper.isTypeBusiness()
                ? "开启\n直播" : "开启\n上课");
    }

    private void startCreateRoom(){
        Router.openEnterRoomInfoPage(context);
    }

    private void notifyRadioChange() {
        if (roomType2TabInfo != null && CollectionUtil.isNotEmpty(roomType2TabInfo.dataList)) {
            // 当前tab已有数据, 直接更新
            adapter.notifyDataSetChanged();
            return;
        }

        loadData(false);
    }

    private void loadData(boolean loadMore) {
        if (isRequesting) {
            return;
        }

        final TabInfo tabInfo = roomType2TabInfo;

        if (tabInfo != null && !tabInfo.hasMore && loadMore) {
            return;
        }

        int currentPage = tabInfo == null ? 0 : tabInfo.pageNum;
        final int targetPage = loadMore ? (currentPage + 1) : 1;

        RoomParam param = new RoomParam();
        param.pageNum = targetPage;
        param.pageSize = PAGE_SIZE;
        isRequesting = true;
        RoomEngine.getInstance().getRoomList(param, new Callbacks.Lambda<>(
                (success, data, errorMsg) -> {
                    isRequesting = false;
                    refreshLayout.setRefreshing(false);
                    if (!success) {
                        showToast(errorMsg);
                        return;
                    }

                    List<RoomBasicInfo> requestList = data == null ? null : data.list;
                    boolean hasMore = data != null && data.hasMore;
                    if (tabInfo == null) {
                        TabInfo newTabInfo = new TabInfo();
                        newTabInfo.pageNum = targetPage;
                        newTabInfo.dataList = requestList;
                        newTabInfo.hasMore = hasMore;
                        roomType2TabInfo= newTabInfo;
                        adapter.notifyDataSetChanged();
                    } else {
                        tabInfo.pageNum = targetPage;
                        tabInfo.hasMore = hasMore;
                        if (loadMore) {
                            // 加载更多
                            if (CollectionUtil.isNotEmpty(requestList)) {
                                if (tabInfo.dataList == null) {
                                    tabInfo.dataList = new ArrayList<>();
                                }
                                tabInfo.dataList.addAll(requestList);
                                adapter.notifyDataSetChanged();
                            }
                        } else {
                            // 刷新
                            tabInfo.dataList = requestList;
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
        ));
    }

    private List<RoomBasicInfo> getDataList() {
        return roomType2TabInfo == null ? null : roomType2TabInfo.dataList;
    }

    private class Adapter extends RecyclerView.Adapter<ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.item_room, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            List<RoomBasicInfo> dataList = getDataList();
            if (dataList == null) {
                return;
            }

            String currentUserId = Const.currentUserId;
            final RoomBasicInfo model = dataList.get(position);
            holder.title.setText(model.title);
            boolean isOwner = TextUtils.equals(currentUserId, model.ownerId);
            ViewUtil.setVisibilityIfNecessary(holder.isOwner, isOwner ? View.VISIBLE : View.GONE);
            holder.id.setText(model.roomId);

            holder.copy.setOnClickListener(v -> {
                ClipboardUtil.copyText(model.roomId);
                showToast("已复制: " + model.roomId);
            });

            // avoid frequent click
            ViewUtil.bindClickActionWithClickCheck(holder.itemView, () ->
                    Router.openRoomViaBizType(context, model.roomId, model.title, currentUserId));

            holder.itemView.setOnLongClickListener(v -> {
                if (!TextUtils.equals(model.ownerId, currentUserId)) {
                    // 不是房主, 不能删除
                    return true;
                }

                DialogUtil.confirm(context, "删除当前房间？", () -> {
                    String roomId = model.roomId;

                    DestroyRoomRequest request = new DestroyRoomRequest();
                    request.appId = Const.getAppId();
                    request.roomId = roomId;
                    request.userId = currentUserId;
                    DestroyRoomApi.destroyRoom(request, new Callback<Void>() {
                        @Override
                        public void onSuccess(Void data) {
                            int removeIndex = -1;
                            for (int i = 0; i < dataList.size(); i++) {
                                RoomBasicInfo current = dataList.get(i);
                                if (roomId.equals(current.roomId)) {
                                    removeIndex = i;
                                    break;
                                }
                            }
                            if (removeIndex >= 0) {
                                dataList.remove(removeIndex);
                                adapter.notifyItemRemoved(removeIndex);
                            }
                        }

                        @Override
                        public void onError(String errorMsg) {
                            showToast("销毁失败: " + errorMsg);
                        }
                    });
                });
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return CollectionUtil.size(getDataList());
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView title;
        final View isOwner;
        final TextView id;
        final View copy;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.item_title);
            isOwner = itemView.findViewById(R.id.item_is_owner);
            id = itemView.findViewById(R.id.item_id);
            copy = itemView.findViewById(R.id.item_copy);
        }
    }

    private static class TabInfo {
        int pageNum;
        List<RoomBasicInfo> dataList;
        boolean hasMore;
    }
}
