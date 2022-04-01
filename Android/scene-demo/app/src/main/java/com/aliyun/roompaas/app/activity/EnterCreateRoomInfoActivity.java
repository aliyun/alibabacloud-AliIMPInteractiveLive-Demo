package com.aliyun.roompaas.app.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.dingpaas.sceneclass.CreateClassRsp;
import com.alibaba.dingpaas.scenelive.SceneCreateLiveReq;
import com.alibaba.dingpaas.scenelive.SceneCreateLiveRsp;
import com.aliyun.roompaas.app.Const;
import com.aliyun.roompaas.app.R;
import com.aliyun.roompaas.app.activity.base.BaseActivity;
import com.aliyun.roompaas.app.helper.RoomHelper;
import com.aliyun.roompaas.app.helper.Router;
import com.aliyun.roompaas.app.sp.SpHelper;
import com.aliyun.roompaas.app.sp.UserSp;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.base.log.Logger;
import com.aliyun.roompaas.base.util.CommonUtil;
import com.aliyun.roompaas.biz.RoomEngine;
import com.aliyun.roompaas.biz.exposable.RoomSceneClass;
import com.aliyun.roompaas.biz.exposable.RoomSceneLive;
import com.aliyun.roompaas.biz.exposable.model.Result;
import com.aliyun.roompaas.uibase.util.ViewUtil;

public class EnterCreateRoomInfoActivity extends BaseActivity {

    private static final String TAG = EnterCreateRoomInfoActivity.class.getSimpleName();

    private EditText roomNameInput;
    private EditText userNickInput;
    private RoomEngine roomEngine;

    private UserSp userSp;
    private String userId;

    private boolean isBusiness;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        userSp = SpHelper.getInstance(UserSp.class);
        userId = userSp.getUserId();
        roomEngine = RoomEngine.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_create_room_info);

        roomNameInput = findViewById(R.id.form_room_name);
        userNickInput = findViewById(R.id.form_user_nick);

        isBusiness = RoomHelper.isTypeBusiness();
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

        String currentUserId = Const.currentUserId;
        String roomTile = String.format("用户%s的房间", currentUserId);
        if(isBusiness){
            SceneCreateLiveReq req = new SceneCreateLiveReq();
            req.anchorId = currentUserId;
            req.notice = "向观众介绍你的直播间吧～（长按修改）";
            req.anchorNick = userId;
            req.title = roomTile;

            Result<RoomSceneLive> result = roomEngine.getRoomSceneLive();
            if (result.value == null) {
                CommonUtil.showToast(this, "创建失败："+result.errorMsg);
                return;
            }
            result.value.createLive(req, new Callback<SceneCreateLiveRsp>() {
                @Override
                public void onSuccess(SceneCreateLiveRsp response) {
                    Router.openRoomViaBizType(context, response.roomId, roomName, userId);
                    finish();
                }

                @Override
                public void onError(String errorMsg) {
                    showToast("创建失败：" + errorMsg);
                }
            });
        } else {
            Result<RoomSceneClass> result = roomEngine.getRoomSceneClass();
            if (result.value == null) {
                CommonUtil.showToast(this, "创建失败："+result.errorMsg);
                return;
            }

            boolean inRoomNoFormat = !TextUtils.isEmpty(roomName) && TextUtils.isDigitsOnly(roomName);
            String title = inRoomNoFormat ? roomTile : roomName;
            result.value.createClass(title, userNick, new Callback<CreateClassRsp>() {
                @Override
                public void onSuccess(CreateClassRsp response) {
                    Logger.i(TAG, "createClass onSuccess: " + response.classId);
                    Router.openRoomViaBizType(context, response.roomId, roomName, userId);
                }

                @Override
                public void onError(String errorMsg) {
                    showToast("创建失败：" + errorMsg);
                }
            });
        }
    }
}
