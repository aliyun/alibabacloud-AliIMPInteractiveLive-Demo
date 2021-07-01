package com.aliyun.roompaas.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.alibaba.dingpaas.base.DPSAuthToken;
import com.aliyun.roompaas.app.Const;
import com.aliyun.roompaas.app.R;
import com.aliyun.roompaas.app.activity.base.BaseActivity;
import com.aliyun.roompaas.app.activity.classroom.ClassroomActivity;
import com.aliyun.roompaas.app.api.CreateRoomApi;
import com.aliyun.roompaas.app.api.GetTokenApi;
import com.aliyun.roompaas.app.enums.RoomType;
import com.aliyun.roompaas.app.request.CreateRoomRequest;
import com.aliyun.roompaas.app.response.CreateRoomResponse;
import com.aliyun.roompaas.app.response.Response;
import com.aliyun.roompaas.app.sp.EnvSp;
import com.aliyun.roompaas.app.sp.SpHelper;
import com.aliyun.roompaas.app.sp.UserSp;
import com.aliyun.roompaas.app.util.DialogUtil;
import com.aliyun.roompaas.base.callback.Callback;
import com.aliyun.roompaas.base.util.ThreadUtil;
import com.aliyun.roompaas.biz.RoomEngine;
import com.aliyun.roompaas.biz.enums.EnvType;
import com.aliyun.roompaas.biz.model.TokenInfo;

/**
 * @author puke
 * @version 2021/5/13
 */
public class CreateRoomActivity extends BaseActivity {

    private static final String TAG = CreateRoomActivity.class.getSimpleName();

    private EditText userIdInput;
    private EditText roomNameInput;
    private EditText userNickInput;
    private Spinner roomType;
    private RoomEngine roomEngine;

    private UserSp userSp;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        userSp = SpHelper.getInstance(UserSp.class);
        userId = userSp.getUserId();
        roomEngine = RoomEngine.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_room);

        userIdInput = findViewById(R.id.form_user_id);
        if (!TextUtils.isEmpty(userId)) {
            userIdInput.setText(userId);
        }
        roomNameInput = findViewById(R.id.form_room_name);
        userNickInput = findViewById(R.id.form_user_nick);
        roomType = findViewById(R.id.form_room_type);

        findViewById(R.id.form_submit).setOnLongClickListener(v -> {
            roomNameInput.setText(String.format("%s的房间", userId));
            userNickInput.setText(userId);
            return true;
        });

        Button env = findViewById(R.id.env);
        final EnvType currentEnv = Const.ENV_TYPE;
        final boolean isPre = currentEnv == EnvType.PRE;
        env.setText(String.format("环境: %s", isPre ? "预发" : "线上"));
        env.setOnClickListener(v -> {
            String title = "切换环境";
            int checkedItem = isPre ? 0 : 1;
            final EnvType[] envTypes = {EnvType.PRE, EnvType.ONLINE};
            String[] choices = new String[envTypes.length];
            for (int i = 0; i < envTypes.length; i++) {
                choices[i] = envTypes[i].getValue();
            }
            DialogUtil.singleChoice(context, title, choices, checkedItem, index -> {
                final EnvType selected = envTypes[index];
                if (selected == currentEnv) {
                    // 仍然是当前环境, 不做处理
                    return;
                }

                String message = "切换需要重启App，是否确认";
                DialogUtil.confirm(context, message, () -> {
                    SpHelper.getInstance(EnvSp.class).setEnv(selected.name());

                    ThreadUtil.postDelay(200, () -> {
                        final Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
                        if (intent != null) {
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                        // 杀掉以前进程
                        android.os.Process.killProcess(android.os.Process.myPid());
                    });
                });
            });
        });

        RoomType[] roomTypes = RoomType.values();
        String[] dataList = new String[roomTypes.length];
        for (int i = 0; i < roomTypes.length; i++) {
            dataList[i] = roomTypes[i].getDesc();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, dataList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roomType.setAdapter(adapter);
    }


    // 创建房间
    public void onCreateRoom(View view) {
        if (!roomEngine.isLogin()) {
            showToast("当前未登录");
            return;
        }

        String roomName = roomNameInput.getText().toString();
        String userNick = userNickInput.getText().toString();
        RoomType roomType = RoomType.values()[this.roomType.getSelectedItemPosition()];

        if (TextUtils.isEmpty(roomName)) {
            showToast("房间名称不能为空");
            return;
        }

        if (TextUtils.isEmpty(userNick)) {
            showToast("用户昵称不能为空");
            return;
        }

        final CreateRoomRequest request = new CreateRoomRequest();
        request.domain = Const.APP_ID;
        request.ownerId = userId;
        request.title = roomName;
        request.notice = "默认公告";
        request.templateId = "default";
        request.bizType = roomType.name().toLowerCase();
        CreateRoomApi.createRoom(request, new Callback<Response<CreateRoomResponse>>() {
            @Override
            public void onSuccess(Response<CreateRoomResponse> response) {
                CreateRoomResponse result = response.result;
                if (!response.responseSuccess || result == null) {
                    showToast(response.message);
                    return;
                }

                switch (roomType) {
                    case BUSINESS:
                        // 去电商房间
                        BusinessActivity.open(context, result.roomId, roomName, userId);
                        break;
                    case CLASSROOM:
                        // 去课堂房间
                        ClassroomActivity.open(context, result.roomId, roomName, userId);
                        break;
                }
            }

            @Override
            public void onError(String errorMsg) {
                showToast("创建失败：" + errorMsg);
            }
        });
    }

    // 去房间列表页
    public void onToRoomListPage(View view) {
        if (!roomEngine.isLogin()) {
            showToast("当前未登录");
            return;
        }

        RoomListActivity.open(context, null);
    }

    // 登录
    public void onLogin(final View view) {
        if (!roomEngine.isInit()) {
            showToast("当前未初始化成功, 请稍等");
            return;
        }

        if (roomEngine.isLogin()) {
            showToast("当前已登录");
            return;
        }

        this.userId = userIdInput.getText().toString().trim();

        GetTokenApi.getToken(userId, new Callback<DPSAuthToken>() {
            @Override
            public void onSuccess(DPSAuthToken token) {
                TokenInfo tokenInfo = new TokenInfo();
                tokenInfo.accessToken = token.accessToken;
                tokenInfo.refreshToken = token.refreshToken;
                roomEngine.login(userId, tokenInfo, new Callback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        Const.currentUserId = userId;
                        userSp.setUserId(userId);
                        userIdInput.setEnabled(false);
                        view.setEnabled(false);
                        showToast("登录成功");
                    }

                    @Override
                    public void onError(String errorMsg) {
                        showToast("登录失败: " + errorMsg);
                    }
                });
            }

            @Override
            public void onError(String errorMsg) {
                showToast("获取Token失败: " + errorMsg);
            }
        });
    }
}
