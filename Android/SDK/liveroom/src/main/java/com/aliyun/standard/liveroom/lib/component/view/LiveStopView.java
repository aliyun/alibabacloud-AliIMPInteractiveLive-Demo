package com.aliyun.standard.liveroom.lib.component.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;

import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.uibase.util.DialogUtil;
import com.aliyun.standard.liveroom.lib.LiveContext;
import com.aliyun.standard.liveroom.lib.R;
import com.aliyun.standard.liveroom.lib.component.BaseComponent;
import com.aliyun.standard.liveroom.lib.component.ComponentHolder;
import com.aliyun.standard.liveroom.lib.component.IComponent;

/**
 * @author puke
 * @version 2021/7/29
 */
public class LiveStopView extends AppCompatImageView implements ComponentHolder {

    private static final int ORDER_STOP = 100;
    private static final String TAG = LiveStopView.class.getSimpleName();

    private final Component component = new Component();

    public LiveStopView(@NonNull Context context) {
        this(context, null, 0);
    }

    public LiveStopView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LiveStopView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setImageResource(R.drawable.ilr_icon_close);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                component.readyStop();
            }
        });
    }

    protected String getStopTips() {
        return "还有观众正在路上，确定要结束直播吗？";
    }

    @Override
    public IComponent getComponent() {
        return component;
    }

    private class Component extends BaseComponent {
        @Override
        public void onInit(LiveContext liveContext) {
            super.onInit(liveContext);
        }

        @Override
        public boolean interceptBackKey() {
            readyStop();
            // 拦截所有back操作
            return true;
        }

        private void readyStop() {
            // 是主播身份, 且正在推流, 则需要弹窗
            boolean stopLive = isOwner() && liveContext.isPushing();
            if (stopLive) {
                DialogUtil.showCustomDialog(getContext(), getStopTips(), new Runnable() {
                    @Override
                    public void run() {
                        doStop(true);
                    }
                }, null);
            } else {
                doStop(false);
            }
        }

        private void doStop(boolean stopLive) {
            if (stopLive) {
                getPusherService().stopLive(new Callback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        liveContext.setPushing(false);
                    }

                    @Override
                    public void onError(String errorMsg) {
                        showToast("结束直播失败: " + errorMsg);
                    }
                });
            }
            activity.finish();
        }

        @Override
        public int getOrder() {
            // 后置回调的优先级, 保证最后处理 interceptBackKey 事件
            return ORDER_STOP;
        }
    }
}
