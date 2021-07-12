package com.aliyun.roompaas.app.activity.base;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alibaba.dingpaas.room.RoomDetail;
import com.aliyun.roompaas.app.helper.PermissionHelper;
import com.aliyun.roompaas.app.util.DialogUtil;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.base.callback.Callbacks;
import com.aliyun.roompaas.biz.exposable.RoomChannel;
import com.aliyun.roompaas.biz.RoomEngine;
import com.aliyun.roompaas.chat.exposable.ChatService;
import com.aliyun.roompaas.live.exposable.LivePlayerService;
import com.aliyun.roompaas.live.exposable.LivePusherService;
import com.aliyun.roompaas.live.exposable.LiveService;

/**
 * 房间Activity, 封装房间相关的通用处理逻辑
 *
 * @author puke
 * @version 2021/5/27
 */
public abstract class BaseRoomActivity extends BaseActivity {

    private static final String TAG = BaseRoomActivity.class.getSimpleName();

    // 页面跳转相关配置
    private static final String PARAM_KEY_ROOM_ID = "room_id";
    private static final String PARAM_KEY_ROOM_TITLE = "room_title";
    private static final String PARAM_KEY_NICK = "nick";

    // 权限相关配置
    private static final int REQUEST_PERMISSION_CAMERA_OK = 1;

    private static final String[] DEFAULT_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    protected String roomId;
    protected String roomTitle;
    protected String nick;
    protected PermissionHelper permissionHelper;

    protected RoomChannel roomChannel;
    protected ChatService chatService;
    protected LiveService liveService;
    protected LivePlayerService livePlayerService;
    protected LivePusherService livePusherService;

    // 解析参数
    private void parseParams() {
        Intent intent = getIntent();
        roomId = intent.getStringExtra(PARAM_KEY_ROOM_ID);
        roomTitle = intent.getStringExtra(PARAM_KEY_ROOM_TITLE);
        nick = intent.getStringExtra(PARAM_KEY_NICK);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // 解析参数
        parseParams();

        // 参数有效性校验
        if (TextUtils.isEmpty(roomId)) {
            showToast("房间Id为空");
            return;
        }

        super.onCreate(savedInstanceState);

        // 权限校验
        checkPermission();
    }

    private void checkPermission() {
        permissionHelper = new PermissionHelper(
                this, REQUEST_PERMISSION_CAMERA_OK, DEFAULT_PERMISSIONS);
        permissionHelper.setGrantedCallback(this::init);
        permissionHelper.setRejectedCallback(
                () -> DialogUtil.tips(this, "权限不足, 即将退出页面", this::finish)
        );
        permissionHelper.checkPermission();
    }

    @CallSuper
    protected void init() {
        // 获取RoomChannel
        roomChannel = RoomEngine.getInstance().getRoomChannel(roomId);
        // 获取插件服务
        chatService = roomChannel.getPluginService(ChatService.class);
        liveService = roomChannel.getPluginService(LiveService.class);
        livePlayerService = liveService.getPlayerService();
        livePusherService = liveService.getPusherService();
        // 进房间
        roomChannel.enterRoom(nick, new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                RoomDetail roomDetail = roomChannel.getRoomDetail();
                onEnterRoomSuccess(roomDetail);
            }

            @Override
            public void onError(String errorMsg) {
                onEnterRoomError(errorMsg);
            }
        });
    }

    /**
     * 进房间成功的回调
     *
     * @param roomDetail 房间信息
     */
    protected abstract void onEnterRoomSuccess(RoomDetail roomDetail);

    /**
     * 进房间失败的回调
     *
     * @param errorMsg 错误信息
     */
    protected void onEnterRoomError(String errorMsg) {
        // 默认实现: 弹窗 + 退出
        DialogUtil.tips(context, "进入房间失败: " + errorMsg, BaseRoomActivity.this::finish);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionHelper.handleRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void finish() {
        super.finish();
        if (roomChannel != null) {
            roomChannel.leaveRoom(new Callbacks.Log<>(TAG, "leave room"));
        }
    }

    /**
     * 子类复用的页面打开逻辑
     */
    public static void open(Context context, Intent intent, String roomId, String roomTitle, String nick) {
        intent.putExtra(PARAM_KEY_ROOM_ID, roomId);
        intent.putExtra(PARAM_KEY_ROOM_TITLE, roomTitle);
        intent.putExtra(PARAM_KEY_NICK, nick);
        context.startActivity(intent);
    }
}
