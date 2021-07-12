package com.aliyun.roompaas.app.activity.business.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.roompaas.app.R;
import com.aliyun.roompaas.app.helper.ActivityFloatHelper;
import com.aliyun.roompaas.app.model.BusinessUserModel;
import com.aliyun.roompaas.app.util.StringUtil;
import com.aliyun.roompaas.base.util.CollectionUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author puke
 * @version 2021/6/30
 */
public class LiveAudienceView extends LinearLayout {

    private final ActivityFloatHelper floatHelper;

    private Callback callback;

    private RecyclerView recyclerView;
    private Adapter adapter;

    public LiveAudienceView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setBackgroundResource(R.drawable.bg_live_audience);
        inflate(context, R.layout.view_live_audience, this);

        if (!(context instanceof Activity)) {
            throw new RuntimeException("The context of current view must be activity type.");
        }

        floatHelper = new ActivityFloatHelper((Activity) context, R.layout.view_float_audience);
        setOnClickListener(v -> {
            View layer = floatHelper.show();
            if (layer == null) {
                return;
            }

            if (recyclerView == null) {
                recyclerView = layer.findViewById(R.id.view_recycler_view);
                recyclerView.setLayoutManager(new LinearLayoutManager(
                        context, RecyclerView.VERTICAL, false));
                initAdapterByLazy();
                recyclerView.setAdapter(adapter);
            }
            notifyDataChanged();
        });
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
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
        if (model != null && StringUtil.isNotEmpty(model.id)) {
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
        if (StringUtil.isNotEmpty(userId) && CollectionUtil.isNotEmpty(dataList)) {
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

    private class Adapter extends RecyclerView.Adapter<ViewHolder> {
        private List<BusinessUserModel> dataList = new ArrayList<>();

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.item_user, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            final BusinessUserModel model = dataList.get(position);
            holder.nick.setText(model.nick);
            holder.id.setText(model.id);

            holder.itemView.setOnClickListener(v -> {
                if (floatHelper != null) {
                    floatHelper.hide();
                }
                if (callback != null) {
                    callback.onUserSelected(model);
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

    public interface Callback {
        void onUserSelected(BusinessUserModel model);
    }
}
