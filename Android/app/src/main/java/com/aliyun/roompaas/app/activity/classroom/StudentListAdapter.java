package com.aliyun.roompaas.app.activity.classroom;

import android.content.Context;
import android.graphics.PixelFormat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.alivc.rtc.AliRtcEngine;
import com.aliyun.roompaas.app.R;
import com.aliyun.roompaas.biz.RoomChannel;
import com.aliyun.roompaas.rtc.RtcService;
import com.aliyun.roompaas.rtc.event.RtcStreamEvent;

import org.jetbrains.annotations.NotNull;
import org.webrtc.sdk.SophonSurfaceView;

import java.util.ArrayList;
import java.util.List;

public class StudentListAdapter extends RecyclerView.Adapter<StudentListAdapter.StudentListHolder> {

    private final Context context;
    private final List<RtcStreamEvent> dataList;
    private ItemClickListener itemClickListener;

    private final RtcService rtcService;

    public StudentListAdapter(RoomChannel roomChannel, Context context) {
        this.context = context;
        dataList = new ArrayList<>();
        this.rtcService = roomChannel.getPluginService(RtcService.class);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void addOrUpdateData(RtcStreamEvent rtcStreamEvent) {
        String targetUserId = rtcStreamEvent.userId;
        int position = -1;
        for (int i = 0; i < dataList.size(); i++) {
            String userId = dataList.get(i).userId;
            if (TextUtils.equals(userId, targetUserId)) {
                position = i;
                break;
            }
        }

        if (position < 0) {
            // 无, 添加
            dataList.add(rtcStreamEvent);
            notifyItemInserted(dataList.size() - 1);
        } else {
            // 有, 替换
            dataList.set(position, rtcStreamEvent);
            notifyItemChanged(position);
        }
    }

    public void updateData(int position, RtcStreamEvent rtcStreamEvent) {
        if (position < dataList.size()) {
            dataList.set(position, rtcStreamEvent);
            notifyItemChanged(position);
        }
    }

    public void removeData(int position) {
        if (position < 0 || position >= dataList.size()) {
            return;
        }
        detachedPreview(dataList.get(position));
        dataList.remove(position);
        notifyItemRemoved(position);
    }

    public void removeData(RtcStreamEvent rtcStreamEvent) {
        int position = dataList.indexOf(rtcStreamEvent);
        if (position != -1) {
            removeData(position);
        }
    }

    public void removeAll() {
        dataList.clear();
        notifyDataSetChanged();
    }

    @NotNull
    @Override
    public StudentListHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_rtc_small_stream, parent, false);
        return new StudentListHolder(view);
    }

    @Override
    public void onBindViewHolder(@NotNull StudentListHolder holder, int position) {
    }

    @Override
    public void onBindViewHolder(@NotNull StudentListHolder holder, int position, @NotNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
        ((StudentListHolder) holder).bindView(position, payloads);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    /**
     * 移除item的时候先把soponsurfaceview从父view上移除，防止切换大小屏的时候提示view的添加异常
     *
     * @param info 流信息
     */
    public void detachedPreview(RtcStreamEvent info) {
        if (info != null && info.isLocalStream) {
            SophonSurfaceView view = (SophonSurfaceView) info.aliVideoCanvas.view;
            if (view != null) {
                if (view.getParent() != null) {
                    ((ViewGroup) view.getParent()).removeAllViews();
                }
            }
        }
    }

    class StudentListHolder extends RecyclerView.ViewHolder {

        private final FrameLayout renderContainer;
        private final TextView nick;
        private final ImageView mute;

        private StudentListHolder(View view) {
            super(view);
            renderContainer = view.findViewById(R.id.item_render_container);
            nick = view.findViewById(R.id.item_nick);
            mute = view.findViewById(R.id.item_mute);
        }

        private void bindView(final int position, final List<Object> payloads) {
            // 取到当前小屏项的数据源
            RtcStreamEvent streamInfo = dataList.get(position);

            // 设置小屏点击事件
            itemView.setOnClickListener(v -> {
                renderContainer.removeAllViews();
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
                }
                return;
            }

            nick.setText(streamInfo.userName);
            mute.setImageResource(streamInfo.muteMic
                    ? R.drawable.alivc_biginteractiveclass_item_mute_mic
                    : R.drawable.alivc_biginteractiveclass_item_unmute_mic);

            final SophonSurfaceView sophonSurfaceView = (SophonSurfaceView) streamInfo.aliVideoCanvas.view;

            if (sophonSurfaceView == null) {
                SophonSurfaceView newSophonSurfaceView = new SophonSurfaceView(itemView.getContext());
                newSophonSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
                // true 在最顶层，会遮挡一切view
                newSophonSurfaceView.setZOrderOnTop(false);
                // true 如已绘制SurfaceView则在surfaceView上一层绘制。
                newSophonSurfaceView.setZOrderMediaOverlay(true);
                streamInfo.aliVideoCanvas.view = newSophonSurfaceView;
                // 设置渲染模式,一共有四种
                streamInfo.aliVideoCanvas.renderMode = AliRtcEngine.AliRtcRenderMode.AliRtcRenderModeFill;
                renderContainer.removeAllViews();
                renderContainer.addView(newSophonSurfaceView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                if (streamInfo.isLocalStream) {
                    // preview
                    startPreview(streamInfo);
                } else {
                    displayStream(streamInfo);
                }
            } else {
                nick.setText(TextUtils.isEmpty(streamInfo.userName) ? "" : streamInfo.userName);
                mute.setImageResource(streamInfo.muteMic ? R.drawable.alivc_biginteractiveclass_item_mute_mic : R.drawable.alivc_biginteractiveclass_item_unmute_mic);
                // 已经添加并开始预览就只切换展示的spoonsurfaceview
                if (sophonSurfaceView.getParent() != null) {
                    ((ViewGroup) sophonSurfaceView.getParent()).removeView(sophonSurfaceView);
                }
                // true 在最顶层，会遮挡一切view
                sophonSurfaceView.setZOrderOnTop(false);
                // true 如已绘制SurfaceView则在surfaceView上一层绘制。
                sophonSurfaceView.setZOrderMediaOverlay(true);
                renderContainer.removeAllViews();
                renderContainer.addView(sophonSurfaceView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                if (!streamInfo.isLocalStream) {
                    return;
                }
                // 点击关闭摄像头需要掉rtc sdk的停止预览展示黑背景
                if (streamInfo.muteLocalCamera) {
                    rtcService.getAliRtcManager().stopPreview();
                } else {
                    rtcService.getAliRtcManager().startPreview();
                }
            }
        }
    }

    /**
     * 展示远端流
     */
    private void displayStream(RtcStreamEvent alivcVideoStreamInfo) {
        AliRtcEngine.AliRtcVideoCanvas aliVideoCanvas = alivcVideoStreamInfo.aliVideoCanvas;
        if (aliVideoCanvas != null) {
            AliRtcEngine.AliRtcVideoTrack aliRtcVideoTrack =
                    alivcVideoStreamInfo.aliRtcVideoTrack == AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackBoth
                            ? AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackScreen
                            : alivcVideoStreamInfo.aliRtcVideoTrack;
            // 小屏渲染内容从远端读
            rtcService.getAliRtcManager().setRemoteViewConfig(aliVideoCanvas, alivcVideoStreamInfo.userId, aliRtcVideoTrack);
        }
    }

    /**
     * 展示本地流
     */
    private void startPreview(RtcStreamEvent alivcVideoStreamInfo) {
        AliRtcEngine.AliRtcVideoCanvas aliVideoCanvas = alivcVideoStreamInfo.aliVideoCanvas;
        if (aliVideoCanvas != null) {
            // 小屏渲染内容从本地摄像头读
            rtcService.getAliRtcManager().setLocalViewConfig(aliVideoCanvas, alivcVideoStreamInfo.aliRtcVideoTrack);
        }

        // 控制自己的摄像头是否打开
        if (alivcVideoStreamInfo.muteLocalCamera) {
            rtcService.getAliRtcManager().stopPreview();
        } else {
            rtcService.getAliRtcManager().startPreview();
        }
    }

    public interface ItemClickListener {
        void onItemClicked(int position, RtcStreamEvent streamInfo);
    }
}
