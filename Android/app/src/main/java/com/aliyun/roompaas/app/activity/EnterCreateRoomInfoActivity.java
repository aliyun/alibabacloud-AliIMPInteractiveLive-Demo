package com.aliyun.roompaas.app.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;

import com.aliyun.roompaas.app.Const;
import com.aliyun.roompaas.app.R;
import com.aliyun.roompaas.app.activity.base.BaseActivity;
import com.aliyun.roompaas.app.api.CreateRoomApi;
import com.aliyun.roompaas.app.helper.RoomHelper;
import com.aliyun.roompaas.app.helper.Router;
import com.aliyun.roompaas.app.request.CreateRoomRequest;
import com.aliyun.roompaas.app.response.CreateRoomResponse;
import com.aliyun.roompaas.app.response.Response;
import com.aliyun.roompaas.app.sp.SpHelper;
import com.aliyun.roompaas.app.sp.UserSp;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.base.util.ViewUtil;
import com.aliyun.roompaas.biz.RoomEngine;

public class EnterCreateRoomInfoActivity extends BaseActivity {

    private static final String TAG = EnterCreateRoomInfoActivity.class.getSimpleName();

    private EditText roomNameInput;
    private EditText userNickInput;
    private RoomEngine roomEngine;

    private UserSp userSp;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        userSp = SpHelper.getInstance(UserSp.class);
        userId = userSp.getUserId();
        roomEngine = RoomEngine.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_create_room_info);

        roomNameInput = findViewById(R.id.form_room_name);
        userNickInput = findViewById(R.id.form_user_nick);

        boolean isBusiness = RoomHelper.isTypeBusiness();
        roomNameInput.setText(String.format("%s的" + (isBusiness ? "直播间" : "课堂"), userId));
        userNickInput.setText(userId);

        ViewUtil.bindClickActionWithClickCheck(findViewById(R.id.icon_back), this::finish);

        TextView submit = findViewById(R.id.form_submit);
        ViewUtil.bindClickActionWithClickCheck(submit, this::onCreateRoom);
        ViewUtil.applyText(submit, isBusiness ? "即刻开播" : "进入课堂");
    }

    // 创建房间
    @SuppressLint("NonConstantResourceId")
    public void onCreateRoom() {
        if (!roomEngine.isLogin()) {
            showToast("当前未登录");
            return;
        }

        String roomName = roomNameInput.getText().toString();
        String userNick = userNickInput.getText().toString();

        if (TextUtils.isEmpty(roomName)) {
            showToast("名称不能为空");
            return;
        }

        if (TextUtils.isEmpty(userNick)) {
            showToast("用户昵称不能为空");
            return;
        }

        final CreateRoomRequest request = new CreateRoomRequest();
        request.appId = Const.getAppId();
        request.roomOwnerId = userId;
        request.title = roomName;
        request.notice = "向观众介绍你的直播间吧～（长按修改）";
        request.templateId = "default";
        CreateRoomApi.createRoom(request, new Callback<Response<CreateRoomResponse>>() {
            @Override
            public void onSuccess(Response<CreateRoomResponse> response) {
                CreateRoomResponse result = response.result;
                if (!response.responseSuccess || result == null) {
                    showToast(response.message);
                    return;
                }

                Router.openRoomViaBizType(context, result.roomId, roomName, userId);
                finish();
            }

            @Override
            public void onError(String errorMsg) {
                showToast("创建失败：" + errorMsg);
            }
        });
    }
}
