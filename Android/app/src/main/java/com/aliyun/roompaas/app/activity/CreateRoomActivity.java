package com.aliyun.roompaas.app.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.alibaba.dingpaas.base.DPSAuthToken;
import com.aliyun.roompaas.app.Const;
import com.aliyun.roompaas.app.R;
import com.aliyun.roompaas.app.activity.base.BaseActivity;
import com.aliyun.roompaas.app.api.GetTokenApi;
import com.aliyun.roompaas.app.helper.DoubleTripleClick;
import com.aliyun.roompaas.app.helper.RoomHelper;
import com.aliyun.roompaas.app.helper.Router;
import com.aliyun.roompaas.app.helper.UserHelper;
import com.aliyun.roompaas.app.sensitive.AllSensitive;
import com.aliyun.roompaas.app.sp.SpHelper;
import com.aliyun.roompaas.app.sp.UserSp;
import com.aliyun.roompaas.app.util.DialogUtil;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.base.util.ViewUtil;
import com.aliyun.roompaas.biz.RoomEngine;
import com.aliyun.roompaas.biz.exposable.model.TokenInfo;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author puke
 * @version 2021/5/13
 */
public class CreateRoomActivity extends BaseActivity {

    private static final String TAG = CreateRoomActivity.class.getSimpleName();

    private EditText userIdInput;
    private RoomEngine roomEngine;
    private View loginButton;

    private UserSp userSp;
    private String userId;
    private AtomicBoolean loginRequestFlowFinish = new AtomicBoolean(false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadParamsFromCache();

        roomEngine = RoomEngine.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_room);

        userIdInput = findViewById(R.id.form_user_id);
        if (!TextUtils.isEmpty(userId)) {
            userIdInput.setText(userId);
        }
        loginButton = findViewById(R.id.loginButton);

        ViewUtil.bindClickActionWithClickCheck(loginButton, this::onLogin);

        AllSensitive.switchEnv(findViewById(R.id.env), this);
        DoubleTripleClick.addClickCallback(findViewById(R.id.roomTypeSwitch), new DoubleTripleClick.SimpleCallback() {
            @Override
            public void onTripleClick() {
                boolean isBusiness = RoomHelper.isTypeBusiness();
                String title = "房间类型环境";
                DialogUtil.doAction(context, title,
                        getSwitchRoomTypeAction(isBusiness, "直播", Const.BIZ_TYPE.BUSINESS),
                        getSwitchRoomTypeAction(!isBusiness, "课堂", Const.BIZ_TYPE.CLASSROOM));
            }
        });
    }

    @NotNull
    private DialogUtil.Action getSwitchRoomTypeAction(boolean isChecked, String itemText, String rooType) {
        return new DialogUtil.Action(itemText, () -> updateTypeSelected(rooType), isChecked);
    }

    private void updateTypeSelected(@Const.BIZ_TYPE String bizType) {
        AllSensitive.showRelaunchAppConfirmDialog(context, RoomHelper.isTypeSameWithCurrent(bizType)
                , () -> RoomHelper.updateTypeSelected(bizType));
    }

    private void loadParamsFromCache() {
        userSp = SpHelper.getInstance(UserSp.class);
        userId = UserHelper.parseUserId(userSp);
    }

    public void onLogin() {
        if (!roomEngine.isInit()) {
            showToast("当前未初始化成功, 请稍等");
            return;
        }

        if (roomEngine.isLogin()) {
            guideToListPage();
            return;
        }

        this.userId = userIdInput.getText().toString().trim();
        if (TextUtils.isEmpty(userId)) {
            showToast("输入有效id");
            return;
        }

        disableInput();

        loginRequestFlowFinish.set(false);
        GetTokenApi.getToken(userId, new Callback<DPSAuthToken>() {
            @Override
            public void onSuccess(DPSAuthToken token) {
                if (loginRequestFlowFinish.get()) {
                    return;
                }
                TokenInfo tokenInfo = new TokenInfo();
                tokenInfo.accessToken = token.accessToken;
                tokenInfo.refreshToken = token.refreshToken;
                roomEngine.auth(userId, tokenInfo, new Callback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        if (loginRequestFlowFinish.get()) {
                            return;
                        }
                        loginSuccessProcess();
                        enableInput();
                        loginRequestFlowFinish.set(true);
                    }

                    @Override
                    public void onError(String errorMsg) {
                        if (loginRequestFlowFinish.get()) {
                            return;
                        }
                        showToast("登录失败: " + errorMsg);
                        enableInput();
                        loginRequestFlowFinish.set(true);
                    }
                });
            }

            @Override
            public void onError(String errorMsg) {
                if (loginRequestFlowFinish.get()) {
                    return;
                }
                showToast("获取Token失败: " + errorMsg);
                enableInput();
                loginRequestFlowFinish.set(true);
            }
        });
    }

    private void disableInput() {
        ViewUtil.disableView(userIdInput, loginButton);
    }

    private void enableInput() {
        ViewUtil.enableView(userIdInput, loginButton);
    }

    private void loginSuccessProcess() {
        Const.currentUserId = userId;
        userSp.setUserId(userId);
        showToast("登录成功");
        guideToListPage();
    }

    private void guideToListPage() {
        Router.openRoomListPage(context);
    }
}
