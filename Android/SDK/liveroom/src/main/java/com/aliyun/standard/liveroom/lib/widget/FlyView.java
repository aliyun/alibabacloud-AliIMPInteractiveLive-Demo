package com.aliyun.standard.liveroom.lib.widget;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.TextView;

import com.aliyun.roompaas.uibase.helper.RecyclerViewHelper;
import com.aliyun.standard.liveroom.lib.LimitSizeRecyclerView;
import com.aliyun.standard.liveroom.lib.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author puke
 * @version 2021/9/27
 */
public class FlyView extends LimitSizeRecyclerView {

    private static final String TAG = FlyView.class.getSimpleName();

    // 划入时长
    private static final int FLY_IN_DURATION = 700;
    // 划出时长
    private static final int FLY_OUT_DURATION = 300;
    // 无新消息时, 当前消息停留时长
    private static final int STAY_DURATION = 2000;
    // 消息队列最大长度
    private static final int MAX_QUEUE_SIZE = 10;
    // 两条消息之间的间隔
    private static final int SHOW_MESSAGE_INTERVAL = FLY_IN_DURATION + FLY_IN_DURATION;

    private final List<FlyItem> queue = new ArrayList<>();
    private final RecyclerViewHelper<FlyItem> recyclerViewHelper;
    private final Runnable disappearTask = new Runnable() {
        @Override
        public void run() {
            if (recyclerViewHelper.getItemCount() > 0) {
                recyclerViewHelper.removeData(0);
            }
        }
    };
    private final Runnable showTask = new Runnable() {
        @Override
        public void run() {
            if (!isRunning || queue.isEmpty()) {
                isRunning = false;
                removeCallbacks(disappearTask);
                postDelayed(disappearTask, STAY_DURATION);
                return;
            }

            FlyItem item = queue.remove(0);
            disappearTask.run();
            recyclerViewHelper.addData(Collections.singletonList(item));
            removeCallbacks(this);
            postDelayed(this, SHOW_MESSAGE_INTERVAL);
        }
    };

    private boolean isRunning;

    public FlyView(@NonNull Context context) {
        this(context, null, 0);
    }

    public FlyView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlyView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setMaxHeight(getResources().getDimensionPixelOffset(R.dimen.live_message_fly_height));
        recyclerViewHelper = RecyclerViewHelper.of(this, R.layout.ilr_view_fly_item,
                new RecyclerViewHelper.HolderRenderer<FlyItem>() {
                    @Override
                    public void render(RecyclerViewHelper.ViewHolder holder, FlyItem model, int position, int itemCount) {
                        TextView itemView = holder.getView(R.id.item_content);
                        itemView.setText(model.content);
                        itemView.setBackgroundResource(model.bgDrawable);
                    }
                });
        RecyclerView recyclerView = recyclerViewHelper.getRecyclerView();
        CustomItemAnimator animator = new CustomItemAnimator();
        animator.setAddDuration(FLY_IN_DURATION);
        animator.setRemoveDuration(FLY_OUT_DURATION);
        recyclerView.setItemAnimator(animator);
    }

    public void addItem(@NonNull FlyItem item) {
        if (queue.size() > MAX_QUEUE_SIZE) {
            return;
        }

        queue.add(item);
        if (!isRunning) {
            isRunning = true;
            showTask.run();
        }
    }

    public static class FlyItem implements Serializable {
        public CharSequence content;
        @DrawableRes
        public int bgDrawable = R.drawable.ilr_bg_fly_normal;
    }
}
