package com.aliyun.roompaas.app.activity.classroom;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alivc.rtc.AliRtcEngine;
import com.aliyun.roompaas.app.R;
import com.aliyun.roompaas.app.delegate.rtc.StudentRtcDelegate;
import com.aliyun.roompaas.app.helper.AliRtcHelper;
import com.aliyun.roompaas.base.IDestroyable;
import com.aliyun.roompaas.base.util.Utils;
import com.aliyun.roompaas.biz.exposable.RoomChannel;
import com.aliyun.roompaas.rtc.exposable.RtcService;
import com.aliyun.roompaas.rtc.exposable.event.RtcStreamEvent;
import com.aliyun.roompaas.uibase.util.ViewUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class StudentListAdapter extends RecyclerView.Adapter<StudentListAdapter.StudentListHolder>
        implements IDestroyable {

    private final Context context;
    @NonNull
    private RtcStreamEvent[] focusingDataArray = asEmptyArray();
    @Nullable
    private ItemClickListener itemClickListener;

    private RtcService rtcService;

    public StudentListAdapter(RoomChannel roomChannel, Context context) {
        this.context = context;
        this.rtcService = roomChannel.getPluginService(RtcService.class);
    }

    public void setItemClickListener(@Nullable ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void refreshData(int index, RtcStreamEvent event) {
        focusingDataArray[index] = event;
        notifyItemChanged(index);
    }

    public void refreshData(RtcStreamEvent[] array) {
        safelyUpdate(array);
        notifyDataSetChanged();
    }

    private void safelyUpdate(RtcStreamEvent[] array) {
        this.focusingDataArray = array != null ? array : asEmptyArray();
    }

    private RtcStreamEvent[] asEmptyArray() {
        return new RtcStreamEvent[StudentRtcDelegate.DEFAULT_SUPPORTING_STUDENT_VIEW_COUNT];
    }

    public void updateLocalMic(String uid, boolean muteLocalMic) {
        for (int i = 0; i < focusingDataArray.length; i++) {
            RtcStreamEvent rtcStreamEvent = focusingDataArray[i];
            if (rtcStreamEvent == null) {
                continue;
            }
            if (rtcStreamEvent.userId.equals(uid)) {
                rtcStreamEvent.muteMic = muteLocalMic;
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void updateLocalCamera(String uid, boolean muteLocalCamera) {
        for (int i = 0; i < focusingDataArray.length; i++) {
            RtcStreamEvent rtcStreamEvent = focusingDataArray[i];
            if (rtcStreamEvent == null) {
                continue;
            }
            if (rtcStreamEvent.userId.equals(uid)) {
                rtcStreamEvent.muteCamera = muteLocalCamera;
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void removeAll() {
        for (RtcStreamEvent e : focusingDataArray) {
            detachedPreview(e);
        }
        Arrays.fill(focusingDataArray, null);
        notifyDataSetChanged();
    }

    @Override
    public StudentListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_rtc_small_stream, parent, false);
        return new StudentListHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentListHolder holder, int position) {
    }

    @Override
    public void onBindViewHolder(@NonNull StudentListHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
        ((StudentListHolder) holder).bindView(position, payloads);
    }

    @Override
    public int getItemCount() {
        return focusingDataArray.length;
    }

    /**
     * 移除item的时候先把soponsurfaceview从父view上移除，防止切换大小屏的时候提示view的添加异常
     *
     * @param info 流信息
     */
    public void detachedPreview(@Nullable RtcStreamEvent info) {
        if (info != null && info.isLocalStream && info.aliVideoCanvas != null) {
            ViewUtil.removeSelfSafely(info.aliVideoCanvas.view);
        }
    }

    class StudentListHolder extends RecyclerView.ViewHolder {

        private final FrameLayout renderContainer;
        private final View container;
        private final TextView nick;
        private final ImageView mute;
        private final LinearLayout cameraLayout;

        private StudentListHolder(View view) {
            super(view);
            renderContainer = view.findViewById(R.id.item_render_container);
            container = view.findViewById(R.id.container);
            nick = view.findViewById(R.id.item_nick);
            mute = view.findViewById(R.id.item_mute);
            cameraLayout = view.findViewById(R.id.rtc_camera_close);
        }

        private void bindView(final int position, final List<Object> payloads) {
            // 取到当前小屏项的数据源
            RtcStreamEvent streamInfo = focusingDataArray[position];
            ViewUtil.switchVisibilityIfNecessary(streamInfo != null, itemView);
            if (streamInfo == null) {
                return;
            }

            // 设置小屏点击事件
            ViewUtil.bindClickActionWithClickCheck(itemView, () -> {
                if (itemClickListener != null) {
                    itemClickListener.onItemClicked(position, streamInfo);
                }
            });

            if (!payloads.isEmpty()) {
                // 这里可以刷新item的局部view，防止画面预览view出现黑屏抖动
                int type = (int) payloads.get(0);
                if (type == 1) {
                    nick.setText(streamInfo.userName);
                    mute.setImageResource(streamInfo.muteMic
                            ? R.drawable.alivc_biginteractiveclass_item_mute_mic
                            : R.drawable.alivc_biginteractiveclass_item_unmute_mic);
                    cameraLayout.setVisibility(streamInfo.muteCamera ? View.VISIBLE : View.GONE);
                }
                return;
            }

            nick.setText(Utils.firstNotEmpty(streamInfo.userName, ""));
            mute.setImageResource(streamInfo.muteMic
                    ? R.drawable.alivc_biginteractiveclass_item_mute_mic
                    : R.drawable.alivc_biginteractiveclass_item_unmute_mic);
            cameraLayout.setVisibility(streamInfo.muteCamera ? View.VISIBLE : View.GONE);

            final View canvasView = streamInfo.aliVideoCanvas.view;

            if (canvasView == null) {
                AliRtcHelper.fillCanvasViewIfNecessary(streamInfo.aliVideoCanvas, itemView.getContext(), true);
                configSurfaceViewAndAttachToContainer(renderContainer, streamInfo.aliVideoCanvas.view);
                if (streamInfo.isLocalStream) {
                    // preview
                    startPreview(streamInfo);
                } else {
                    displayStream(streamInfo);
                }
            } else {
                configSurfaceViewAndAttachToContainer(renderContainer, canvasView);
                if (!streamInfo.isLocalStream || rtcService == null) {
                    return;
                }
                // 点击关闭摄像头需要掉rtc sdk的停止预览展示黑背景
                if (streamInfo.muteLocalCamera) {
                    rtcService.stopPreview();
                } else {
                    rtcService.startPreview();
                }
            }
        }

        private void configSurfaceViewAndAttachToContainer(@NonNull ViewGroup holdOnlyOneChildVG, @NonNull View v) {
            if (holdOnlyOneChildVG.getChildCount() > 0 && !Objects.equals(holdOnlyOneChildVG, v.getParent())) {
                holdOnlyOneChildVG.removeAllViews();
            }
            ViewUtil.addChildMatchParentSafely(holdOnlyOneChildVG, v);
        }
    }

    /**
     * 展示远端流
     */
    private void displayStream(RtcStreamEvent streamEvent) {
        AliRtcEngine.AliRtcVideoCanvas aliVideoCanvas = streamEvent.aliVideoCanvas;
        if (aliVideoCanvas != null && rtcService != null) {
            // 小屏渲染内容从远端读
            rtcService.setRemoteViewConfig(aliVideoCanvas, streamEvent.userId,
                    AliRtcHelper.interceptTrack(streamEvent.aliRtcVideoTrack));
        }
    }

    /**
     * 展示本地流
     */
    private void startPreview(RtcStreamEvent alivcVideoStreamInfo) {
        if (rtcService == null) {
            return;
        }

        AliRtcEngine.AliRtcVideoCanvas aliVideoCanvas = alivcVideoStreamInfo.aliVideoCanvas;
        if (aliVideoCanvas != null) {
            // 小屏渲染内容从本地摄像头读
            rtcService.setLocalViewConfig(aliVideoCanvas, alivcVideoStreamInfo.aliRtcVideoTrack);
        }

        // 控制自己的摄像头是否打开
        if (alivcVideoStreamInfo.muteLocalCamera) {
            rtcService.stopPreview();
        } else {
            rtcService.startPreview();
        }
    }

    @Override
    public void destroy() {
        Arrays.fill(focusingDataArray, null);
        itemClickListener = null;
        rtcService = null;
    }

    public interface ItemClickListener {
        void onItemClicked(int position, RtcStreamEvent streamInfo);
    }
}
