package com.aliyun.roompaas.app.activity.classroom.panel;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aliyun.roompaas.app.R;
import com.aliyun.roompaas.app.helper.RecyclerViewHelper;
import com.aliyun.roompaas.app.model.MessageModel;
import com.aliyun.roompaas.app.util.AppUtil;
import com.aliyun.roompaas.app.util.ClipboardUtil;
import com.aliyun.roompaas.base.util.CommonUtil;

import java.util.Collections;
import java.util.List;

/**
 * @author puke
 * @version 2021/5/24
 */
public class ChatView extends FrameLayout {

    private final RecyclerViewHelper<MessageModel> recyclerViewHelper;
    private final LinearLayoutManager layoutManager;
    private final RecyclerView recyclerView;

    public ChatView(@NonNull Context context) {
        super(context);
        inflate(context, R.layout.view_chat, this);
        recyclerView = findViewById(R.id.chat_recycler_view);
        layoutManager = new LinearLayoutManager(context);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerViewHelper = RecyclerViewHelper.of(
                recyclerView, R.layout.item_message, (holder, model, position, itemCount) -> {
                    TextView type = holder.getView(R.id.item_type);
                    TextView content = holder.getView(R.id.item_content);

                    type.setText(model.type);
                    content.setText(model.content);

                    int color = model.color;
                    type.setTextColor(color);
                    content.setTextColor(color);

                    View itemView = holder.itemView;
                    boolean isFirst = position == 0;
                    MarginLayoutParams layoutParams = (MarginLayoutParams) itemView.getLayoutParams();
                    layoutParams.topMargin = isFirst ? AppUtil.dp(6) : 0;
                    itemView.setLayoutParams(layoutParams);

                    content.setOnLongClickListener(v -> {
                        String text = model.content;
                        ClipboardUtil.copyText(text);
                        CommonUtil.showToast(context, "已复制: " + text);
                        return true;
                    });
                }
        );
    }

    @MainThread
    public void addSystemMessage(String content) {
        addMessage("系统", content);
    }

    @MainThread
    public void addMessage(String type, String content) {
        addMessage(Collections.singletonList(new MessageModel(type, content)));
    }

    @MainThread
    public void addMessage(List<MessageModel> addedData) {
        recyclerViewHelper.addData(addedData);

        // 已触底时, 随消息联动
        layoutManager.scrollToPositionWithOffset(
                recyclerViewHelper.getItemCount() - 1, Integer.MIN_VALUE);
        postDelayed(recyclerView::invalidate, 100);
    }
}
