package com.aliyun.roompaas.app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.roompaas.app.R;
import com.aliyun.roompaas.app.model.BusinessUserModel;
import com.aliyun.roompaas.app.util.StringUtil;
import com.aliyun.roompaas.base.util.CollectionUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户列表视图
 *
 * @author puke
 * @version 2021/5/21
 */
public class UserListView extends LinearLayout {

    private final Button toggle;
    private final LimitSizeRecyclerView recyclerView;
    private final Adapter adapter;
    private final List<BusinessUserModel> dataList;

    private boolean isExpand = false;
    private Callback callback;

    public interface Callback {
        void onUserSelected(BusinessUserModel model);
    }

    public UserListView(@NonNull Context context) {
        this(context, null, 0);
    }

    public UserListView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UserListView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);

        inflate(context, R.layout.view_user_list, this);

        toggle = findViewById(R.id.view_toggle);
        recyclerView = findViewById(R.id.view_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(
                context, RecyclerView.VERTICAL, false));
        dataList = new ArrayList<>();
        adapter = new Adapter();
        recyclerView.setAdapter(adapter);

        toggle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                onToggle();
            }
        });
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void setMaxHeight(int maxHeight) {
        recyclerView.setMaxHeight(maxHeight);
    }

    public void setData(List<BusinessUserModel> addedList) {
        dataList.clear();
        if (CollectionUtil.isNotEmpty(addedList)) {
            dataList.addAll(addedList);
        }
        adapter.notifyDataSetChanged();
    }

    public void addData(BusinessUserModel model) {
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
        if (StringUtil.isNotEmpty(userId)) {
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

    // 展开 or 收起
    private void onToggle() {
        isExpand = !isExpand;
        recyclerView.setVisibility(isExpand ? VISIBLE : GONE);
        toggle.setText(isExpand ? "收起" : "展开");
    }

    private class Adapter extends RecyclerView.Adapter<ViewHolder> {

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
            holder.nick.setTextColor(model.color);

            holder.nick.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (callback != null) {
                        callback.onUserSelected(model);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return CollectionUtil.size(dataList);
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView nick;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            nick = itemView.findViewById(R.id.item_text);
        }
    }
}
