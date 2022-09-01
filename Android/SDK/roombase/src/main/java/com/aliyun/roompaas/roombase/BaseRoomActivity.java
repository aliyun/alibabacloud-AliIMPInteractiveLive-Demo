package com.aliyun.roompaas.roombase;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Pair;

import com.alibaba.dingpaas.room.RoomDetail;
import com.aliyun.roompaas.base.callback.Callbacks;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.base.log.Logger;
import com.aliyun.roompaas.biz.RoomEngine;
import com.aliyun.roompaas.biz.exposable.RoomChannel;
import com.aliyun.roompaas.biz.exposable.model.Result;
import com.aliyun.roompaas.uibase.activity.BaseActivity;
import com.aliyun.roompaas.uibase.util.ColorUtil;
import com.aliyun.roompaas.uibase.util.DialogUtil;
import com.aliyun.roompaas.uibase.util.ExStatusBarUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 房间Activity, 封装房间相关的通用处理逻辑
 *
 * @author puke
 * @version 2021/5/27
 */
public abstract class BaseRoomActivity extends BaseActivity {

    private static final String TAG = BaseRoomActivity.class.getSimpleName();
    private String bizType;

    protected String roomId;
    protected String roomTitle;
    protected String nick;
    protected Map<String, String> userExtension;
    protected boolean disableImmersive;
    protected String statusBarColorStringWhenDisableImmersive;
    protected boolean permissionIgnoreStrictCheck;

    protected RoomChannel roomChannel;

    // 解析参数
    @CallSuper
    protected void parseParams(Intent intent) {
        roomId = intent.getStringExtra(Const.PARAM_KEY_ROOM_ID);
        roomTitle = intent.getStringExtra(Const.PARAM_KEY_ROOM_TITLE);
        nick = intent.getStringExtra(Const.PARAM_KEY_NICK);
        disableImmersive = intent.getBooleanExtra(Const.PARAM_KEY_DISABLE_IMMERSIVE, false);
        statusBarColorStringWhenDisableImmersive = intent.getStringExtra(Const.PARAM_KEY_STATUS_BAR_COLOR_STRING_WHEN_DISABLE_IMMERSIVE);
        permissionIgnoreStrictCheck = intent.getBooleanExtra(Const.PARAM_KEY_PERMISSION_IGNORE_STRICT_CHECK, false);

        //noinspection unchecked
        userExtension = (Map<String, String>) intent.getSerializableExtra(Const.PARAM_KEY_USER_EXTENSION);
        if (userExtension == null) {
            userExtension = new HashMap<>();
        }
        userExtension.put(Const.getSdkKey("BizType"), bizType);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // 解析参数
        parseParams(getIntent());

        // 参数有效性校验
        if (TextUtils.isEmpty(roomId)) {
            showToast("房间Id为空");
            return;
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean shouldDisableImmersive() {
        return disableImmersive;
    }

    @Override
    protected Runnable actionWhenDisableImmersive() {
        final Activity act = this;
        return new Runnable() {
            @Override
            public void run() {
                Integer color = ColorUtil.parseColor(statusBarColorStringWhenDisableImmersive);
                if (color == null) {
                    return;
                }

                ExStatusBarUtils.setStatusBarColor(act, color, color == Color.BLACK);
            }
        };
    }

    @Override
    protected boolean permissionIgnoreStrictCheck() {
        return permissionIgnoreStrictCheck;
    }

    @Override
    protected Runnable asPermissionGrantedAction() {
        return new Runnable() {
            @Override
            public void run() {
                RoomEngine roomEngine = RoomEngine.getInstance();
                if (roomEngine.isLogin()) {
                    // 已登录
                    Logger.i(TAG, "already login");
                    init();
                } else {
                    // 未登录
                    Logger.w(TAG, "login state invalid");
                    roomEngine.auth(Const.getCurrentUserId(), new Callback<Void>() {
                        @Override
                        public void onSuccess(Void data) {
                            Logger.i(TAG, "relogin success");
                            init();
                        }

                        @Override
                        public void onError(String errorMsg) {
                            Logger.e(TAG, "relogin error: " + errorMsg);
                            finish();
                        }
                    });
                }
            }
        };
    }

    @Override
    protected Runnable asPermissionGuidanceAction() {
        return new Runnable() {
            @Override
            public void run() {
                DialogUtil.showCustomDialog(BaseRoomActivity.this,
                        "未开启拍摄权限，请在设置中允许使用拍摄和录音权限",
                        new Pair<CharSequence, Runnable>("设置权限", new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.setData(Uri.fromParts("package", context.getPackageName(), null));
                                if (intent.resolveActivity(context.getPackageManager()) != null) {
                                    context.startActivity(intent);
                                }
                            }
                        }),
                        new Pair<CharSequence, Runnable>("取消", new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }));
            }
        };
    }

    @CallSuper
    protected void init() {
        // 获取RoomChannel
        Result<RoomChannel> result = RoomEngine.getInstance().getRoomChannel(roomId);
        if (!result.success) {
            Logger.e(TAG, "getRoomChannel error: " + result.errorMsg);
            showToast(result.errorMsg);
            return;
        }

        roomChannel = result.value;
        // 进房间
        roomChannel.enterRoom(nick, userExtension, new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                if (isActivityValid()) {
                    RoomDetail roomDetail = roomChannel.getRoomDetail();
                    onEnterRoomSuccess(roomDetail);
                }
            }

            @Override
            public void onError(String errorMsg) {
                if (isActivityValid()) {
                    onEnterRoomError(errorMsg);
                }
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
    protected abstract void onEnterRoomError(String errorMsg);

    @Override
    public void finish() {
        super.finish();
        if (roomChannel != null) {
            roomChannel.leaveRoom(new Callbacks.Log<Void>(TAG, "leave room"));
        }
    }

    protected void setBizType(String bizType) {
        this.bizType = bizType;
    }
}
