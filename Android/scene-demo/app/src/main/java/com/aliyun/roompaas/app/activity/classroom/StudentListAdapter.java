package com.aliyun.roompaas.app.activity.classroom;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alivc.rtc.AliRtcEngine;
import com.aliyun.roompaas.app.R;
import com.aliyun.roompaas.app.delegate.rtc.IDisplayVideo;
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
    private IDisplayVideo subStatusQuery;
    private String userId;

    public StudentListAdapter(RoomChannel roomChannel, Context context) {
        this.context = context;
        this.rtcService = roomChannel.getPluginService(RtcService.class);
        this.userId = roomChannel.getUserId();
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

    public void updateLocalMic(String uid, boolean closeMic) {
        for (int i = 0; i < focusingDataArray.length; i++) {
            RtcStreamEvent rtcStreamEvent = focusingDataArray[i];
            if (rtcStreamEvent == null) {
                continue;
            }
            if (rtcStreamEvent.userId.equals(uid)) {
                rtcStreamEvent.closeMic = closeMic;
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void updateLocalCamera(String uid, boolean closeCamera) {
        for (int i = 0; i < focusingDataArray.length; i++) {
            RtcStreamEvent rtcStreamEvent = focusingDataArray[i];
            if (rtcStreamEvent == null) {
                continue;
            }
            if (rtcStreamEvent.userId.equals(uid)) {
                rtcStreamEvent.closeCamera = closeCamera;
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
        if (info != null && info.aliVideoCanvas != null) {
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
            RtcStreamEvent event = focusingDataArray[position];
            ViewUtil.switchVisibilityIfNecessary(event != null, itemView);
            if (event == null) {
                return;
            }

            // 设置小屏点击事件
            ViewUtil.bindClickActionWithClickCheck(itemView, () -> {
                if (itemClickListener != null) {
                    itemClickListener.onItemClicked(position, event);
                }
            });

            String uid = event.userId;
            boolean notDisplayVideo = event.closeCamera || (subStatusQuery != null && !subStatusQuery.showDisplayVideo(uid));
            String nickName = Utils.firstNotEmpty(event.userName, "");
            updateNickMicNoVideo(nickName, notDisplayVideo, event);
            if (!payloads.isEmpty()) {
                // 这里可以刷新item的局部view，防止画面预览view出现黑屏抖动
                int type = (int) payloads.get(0);
                if (type == 1) {
                    updateNickMicNoVideo(nickName, notDisplayVideo, event);
                }
                return;
            }

            if (notDisplayVideo) {
                ViewUtil.removeSelfSafely(event.aliVideoCanvas.view);
                if (isSelf(uid)) {
                    previewProcess(event, true);
                }
            } else {
                if (event.aliVideoCanvas.view == null) {
                    AliRtcHelper.fillCanvasViewIfNecessary(event.aliVideoCanvas, itemView.getContext(), true);
                }
                configSurfaceViewAndAttachToContainer(renderContainer, event.aliVideoCanvas.view);
                if (isSelf(uid)) {
                    previewProcess(event, false);
                } else {
                    displayStream(event);
                }
            }
        }

        private void updateNickMicNoVideo(String nickName, boolean notDisplayVideo, RtcStreamEvent streamInfo) {
            nick.setText(nickName);
            ViewUtil.switchVisibilityIfNecessary(!TextUtils.isEmpty(nickName), nick);
            ViewUtil.switchVisibilityIfNecessary(notDisplayVideo, cameraLayout);
            updateCloseMicIcon(streamInfo.closeMic);
        }

        private void updateCloseMicIcon(boolean closeMic) {
            mute.setImageResource(closeMic ? R.drawable.alivc_biginteractiveclass_item_mute_mic : R.drawable.alivc_biginteractiveclass_item_unmute_mic);
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
    private void displayStream(RtcStreamEvent event) {
        AliRtcEngine.AliRtcVideoCanvas aliVideoCanvas = event.aliVideoCanvas;
        if (aliVideoCanvas != null && rtcService != null) {
            // 小屏渲染内容从远端读
            rtcService.setRemoteViewConfig(aliVideoCanvas, event.userId, AliRtcHelper.interceptTrack(event.aliRtcVideoTrack));
        }
    }

    /**
     * 展示本地流
     */
    private void previewProcess(RtcStreamEvent event, boolean notDisplayVideo) {
        if (rtcService == null) {
            return;
        }

        if (notDisplayVideo) {
            rtcService.stopPreview();
        } else {
            AliRtcEngine.AliRtcVideoCanvas canvas = event.aliVideoCanvas;
            if (canvas != null) {
                rtcService.setLocalViewConfig(canvas, event.aliRtcVideoTrack);
            }
            rtcService.startPreview();
        }
    }

    private boolean isSelf(String uid) {
        return TextUtils.equals(userId, uid);
    }

    public void setSubStatusQuery(IDisplayVideo subStatusQuery) {
        this.subStatusQuery = subStatusQuery;
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
