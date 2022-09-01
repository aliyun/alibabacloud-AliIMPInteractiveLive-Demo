package com.aliyun.standard.liveroom.lib.component.view;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.alibaba.dingpaas.room.RoomDetail;
import com.aliyun.roompaas.base.base.Consumer;
import com.aliyun.roompaas.base.util.Utils;
import com.aliyun.roompaas.beauty_base.BeautyStrategy;
import com.aliyun.roompaas.live.exposable.AliLiveBeautyOptions;
import com.aliyun.roompaas.beauty_base.IBeautyOptUpdate;
import com.aliyun.roompaas.uibase.util.AppUtil;
import com.aliyun.roompaas.uibase.util.DialogUtil;
import com.aliyun.roompaas.uibase.util.ViewUtil;
import com.aliyun.standard.liveroom.lib.Actions;
import com.aliyun.standard.liveroom.lib.R;
import com.aliyun.standard.liveroom.lib.component.BaseComponent;
import com.aliyun.standard.liveroom.lib.component.ComponentHolder;
import com.aliyun.standard.liveroom.lib.component.IComponent;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

/**
 * 美颜视图
 *
 * @author puke
 * @version 2021/7/29
 */
public class LiveBeautyView extends FrameLayout implements ComponentHolder {

    private final Component component = new Component();
    private Dialog dialog;
    private Context context;
    private Reference<Consumer<Boolean>> enterRoomActionRef;

    public LiveBeautyView(@NonNull Context context) {
        this(context, null, 0);
    }

    public LiveBeautyView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LiveBeautyView(@NonNull final Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;

        final ViewGroup v = this;
        Consumer<Boolean> action = new Consumer<Boolean>() {
            @Override
            public void accept(final Boolean isOwner) {
                Runnable initAction = new Runnable() {
                    @Override
                    public void run() {
                        if (!isOwner || BeautyStrategy.INSTANCE.isBeautyInvalid()) {
                            return;
                        }

                        setSelected(true);
                        ViewGroup.LayoutParams lp = getLayoutParams();
                        if (lp != null) {
                            int size = AppUtil.getDimensionPixelOffset(R.dimen.ilr_beauty_icon_size);
                            lp.width = size;
                            lp.height = size;

                            if (lp instanceof MarginLayoutParams) {
                                MarginLayoutParams mlp = (MarginLayoutParams) lp;
                                mlp.leftMargin = AppUtil.getDimensionPixelOffset(R.dimen.ilr_feature_icon_left_margin);
                            }
                            setLayoutParams(lp);
                            requestLayout();
                            setVisibility(VISIBLE);
                        }

                        View.inflate(context, R.layout.ilr_view_live_beauty, v);
                        setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                onBeauty();
                            }
                        });
                    }
                };
                if (v.getParent() instanceof View && ((View) v.getParent()).getWidth() != 0) {
                    ViewUtil.addOnGlobalLayoutListener(v, initAction);
                } else {
                    initAction.run();
                }
            }
        };

        enterRoomActionRef = new WeakReference<>(action);
    }

    private void onBeauty() {
        ofDialog().show();
    }

    private Dialog ofDialog() {
        if (dialog == null) {
            dialog = DialogUtil.createDialogOfBottom(context, LayoutParams.WRAP_CONTENT,
                    R.layout.ilr_view_float_live_queen_beauty, true);
            BeautyStrategy.INSTANCE.setUp((ViewGroup) dialog.findViewById(R.id.beautyContainer), component);
        }

        return dialog;
    }

    @Override
    public IComponent getComponent() {
        return component;
    }

    private class Component extends BaseComponent implements IBeautyOptUpdate {

        @Override
        public void onEnterRoomSuccess(RoomDetail roomDetail) {
            Utils.accept(Utils.getRef(enterRoomActionRef), isOwner());
        }

        @Override
        public void onActivityDestroy() {
            if (dialog != null) {
                Utils.clear(BeautyStrategy.INSTANCE);
            }
        }

        @Override
        public void onEvent(String action, Object... args) {
            if (Actions.PREVIEW_SUCCESS.equals(action)) {
                setVisibility(View.VISIBLE);
            }
        }

        private void updateBeautyOptions(AliLiveBeautyOptions beautyOptions) {
            getPusherService().updateBeautyOptions(beautyOptions);
        }

        private void handleBeautyClick() {
            // 展示美颜
            postEvent(Actions.SHOW_BEAUTY_CLICKED);
        }

        @Override
        public void onUpdateBeautyOpt(AliLiveBeautyOptions beautyOptions) {
            updateBeautyOptions(beautyOptions);
        }
    }
}
