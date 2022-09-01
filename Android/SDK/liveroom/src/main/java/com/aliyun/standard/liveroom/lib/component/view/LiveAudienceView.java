package com.aliyun.standard.liveroom.lib.component.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.dingpaas.room.RoomDetail;
import com.alibaba.dingpaas.room.RoomUserModel;
import com.aliyun.roompaas.base.IDestroyable;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.base.model.PageModel;
import com.aliyun.roompaas.base.util.CollectionUtil;
import com.aliyun.roompaas.base.util.Utils;
import com.aliyun.roompaas.biz.SampleRoomEventHandler;
import com.aliyun.roompaas.biz.exposable.event.KickUserEvent;
import com.aliyun.roompaas.biz.exposable.event.RoomInOutEvent;
import com.aliyun.roompaas.biz.exposable.model.UserParam;
import com.aliyun.roompaas.uibase.util.AppUtil;
import com.aliyun.roompaas.uibase.util.DialogUtil;
import com.aliyun.standard.liveroom.lib.BusinessUserModel;
import com.aliyun.standard.liveroom.lib.LiveContext;
import com.aliyun.standard.liveroom.lib.R;
import com.aliyun.standard.liveroom.lib.component.BaseComponent;
import com.aliyun.standard.liveroom.lib.component.ComponentHolder;
import com.aliyun.standard.liveroom.lib.component.IComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * 直播观众管理视图 (左上角小icon)
 *
 * @author puke
 * @version 2021/6/30
 */
public class LiveAudienceView extends LinearLayout implements ComponentHolder, IDestroyable {

    private final Component component = new Component(this);
    private Dialog dialog;

    private RecyclerView recyclerView;
    private Adapter adapter;

    public LiveAudienceView(@NonNull final Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);
        setMinimumHeight(AppUtil.dp(20));
        setGravity(Gravity.CENTER_VERTICAL);
        setBackgroundResource(R.drawable.ilr_bg_live_audience);

        int verticalMargin = AppUtil.dimensionPixelOffset(R.dimen.info_bubble_vertical_margin);
        setPadding(getPaddingLeft(), verticalMargin, getPaddingRight(), verticalMargin);

        inflate(context, R.layout.ilr_view_live_audience, this);

        if (!(context instanceof Activity)) {
            throw new RuntimeException("The context of current view must be activity type.");
        }

        dialog = DialogUtil.createDialogOfBottom(context, FrameLayout.LayoutParams.WRAP_CONTENT,
                R.layout.ilr_view_float_audience, false);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog == null) {
                    return;
                }
                dialog.show();

                if (recyclerView == null) {
                    recyclerView = dialog.findViewById(R.id.view_recycler_view);
                    recyclerView.setLayoutManager(new LinearLayoutManager(
                            context, RecyclerView.VERTICAL, false));
                    initAdapterByLazy();
                    recyclerView.setAdapter(adapter);
                }
                notifyDataChanged();
            }
        });
    }

    public void setData(List<BusinessUserModel> addedList) {
        initAdapterByLazy();
        adapter.setDataList(addedList);
        notifyDataChanged();
    }

    private void initAdapterByLazy() {
        if (adapter == null) {
            adapter = new Adapter();
        }
    }

    private void notifyDataChanged() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    public void addData(BusinessUserModel model) {
        initAdapterByLazy();
        List<BusinessUserModel> dataList = adapter.getDataList();
        if (model != null && !TextUtils.isEmpty(model.id)) {
            // 防重入 (实际开发时, 该业务逻辑建议放到Activity中处理)
            for (BusinessUserModel userModel : dataList) {
                if (model.id.equals(userModel.id)) {
                    return;
                }
            }

            dataList.add(model);
            adapter.notifyItemInserted(dataList.size() - 1);
        }
    }

    public void removeData(String userId) {
        initAdapterByLazy();
        List<BusinessUserModel> dataList = adapter.getDataList();
        if (!TextUtils.isEmpty(userId) && CollectionUtil.isNotEmpty(dataList)) {
            int index = -1;
            for (int i = 0; i < dataList.size(); i++) {
                BusinessUserModel model = dataList.get(i);
                if (userId.equals(model.id)) {
                    index = i;
                    break;
                }
            }
            if (index >= 0) {
                dataList.remove(index);
                adapter.notifyItemRemoved(index);
            }
        }
    }

    @Override
    public IComponent getComponent() {
        return component;
    }

    @Override
    public void destroy() {
        DialogUtil.dismiss(dialog);
        dialog = null;
    }

    private class Adapter extends RecyclerView.Adapter<ViewHolder> {
        private List<BusinessUserModel> dataList = new ArrayList<>();

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.ilr_item_user, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            final BusinessUserModel model = dataList.get(position);
            holder.nick.setText(model.nick);
            holder.id.setText(model.id);

            holder.itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogUtil.dismiss(dialog);

                    component.handleUserManageLogic(model);
                }
            });
        }

        @Override
        public int getItemCount() {
            return CollectionUtil.size(dataList);
        }

        public void setDataList(List<BusinessUserModel> dataList) {
            this.dataList = dataList;
        }

        public List<BusinessUserModel> getDataList() {
            return dataList;
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView nick;
        TextView id;
        ImageView avatar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            nick = itemView.findViewById(R.id.item_text);
            id = itemView.findViewById(R.id.item_id);
            avatar = itemView.findViewById(R.id.avatar);
        }
    }

    private class Component extends BaseComponent {
        private IDestroyable iDestroyable;

        public Component(IDestroyable iDestroyable) {
            this.iDestroyable = iDestroyable;
        }

        @Override
        public void onActivityDestroy() {
            Utils.destroy(iDestroyable);
        }

        @Override
        public void onInit(LiveContext liveContext) {
            super.onInit(liveContext);

            // 监听房间事件
            roomChannel.addEventHandler(new SampleRoomEventHandler() {
                @Override
                public void onEnterOrLeaveRoom(RoomInOutEvent event) {
                    if (event.enter) {
                        BusinessUserModel model = new BusinessUserModel();
                        model.id = event.userId;
                        model.nick = event.nick;
                        addData(model);
                    } else {
                        removeData(event.userId);
                    }
                }

                @Override
                public void onRoomUserKicked(KickUserEvent event) {
                    if (!TextUtils.equals(roomChannel.getUserId(), event.kickUser)) {
                        // 其他人, 移除列表, 面板删除
                        removeData(event.kickUser);
                    }
                }
            });
        }

        @Override
        public void onEnterRoomSuccess(RoomDetail roomDetail) {
            super.onEnterRoomSuccess(roomDetail);

            // 加载在线列表
            loadUser();
        }

        private void loadUser() {
            // Demo只拉取100条, 业务按需改造
            UserParam userParam = new UserParam();
            userParam.pageNum = 1;
            userParam.pageSize = 100;
            roomChannel.listUser(userParam, new com.aliyun.roompaas.base.exposable.Callback<PageModel<RoomUserModel>>() {
                @Override
                public void onSuccess(PageModel<RoomUserModel> pageModel) {
                    final List<BusinessUserModel> users = new ArrayList<>();
                    if (CollectionUtil.isNotEmpty(pageModel.list)) {
                        for (RoomUserModel roomUserModel : pageModel.list) {
                            if (roomChannel.isOwner(roomUserModel.openId)) {
                                // 过滤掉主播
                                continue;
                            }

                            BusinessUserModel userModel = new BusinessUserModel();
                            userModel.id = roomUserModel.openId;
                            userModel.nick = roomUserModel.nick;
                            users.add(userModel);
                        }
                        setData(users);
                    }
                }

                @Override
                public void onError(String errorMsg) {
                    showToast("获取在线列表失败: " + errorMsg);
                }
            });
        }

        /**
         * 处理用户管理逻辑
         *
         * @param model 目标用户
         */
        private void handleUserManageLogic(final BusinessUserModel model) {
            if (roomChannel == null) {
                return;
            }

            if (!isOwner()) {
                showToast("您当前无操作权限哦");
                return;
            }

            if (roomChannel.isOwner(model.id)) {
                showToast("不能对主播进行操作哦");
                return;
            }

            DialogUtil.doAction(activity, "用户管理",
                    new DialogUtil.Action("禁言", new Runnable() {
                        @Override
                        public void run() {
                            int muteSeconds = 5 * 60;
                            chatService.banComment(model.id, muteSeconds, new Callback<Void>() {
                                @Override
                                public void onSuccess(Void data) {
                                    showToast(String.format("已对%s禁言", model.nick));
                                }

                                @Override
                                public void onError(String errorMsg) {
                                    showToast("禁言失败: " + errorMsg);
                                }
                            });
                        }
                    }),
                    new DialogUtil.Action("取消禁言", new Runnable() {
                        @Override
                        public void run() {
                            chatService.cancelBanComment(model.id, new Callback<Void>() {
                                @Override
                                public void onSuccess(Void data) {
                                    showToast(String.format("已对%s取消禁言", model.nick));
                                }

                                @Override
                                public void onError(String errorMsg) {
                                    showToast("取消禁言失败: " + errorMsg);
                                }
                            });
                        }
                    }),
                    new DialogUtil.Action("移出房间", new Runnable() {
                        @Override
                        public void run() {
                            roomChannel.kickUser(model.id, new Callback<Void>() {
                                @Override
                                public void onSuccess(Void data) {
                                    showToast(String.format("已将%s移除房间", model.nick));
                                }

                                @Override
                                public void onError(String errorMsg) {
                                    showToast("移除失败: " + errorMsg);
                                }
                            });
                        }
                    })
            );
        }
    }
}
