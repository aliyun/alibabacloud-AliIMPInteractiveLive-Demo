package com.aliyun.liveroom.demo;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.player.exposable.CanvasScale;
import com.aliyun.roompaas.roombase.Const;
import com.aliyun.standard.liveroom.lib.LivePrototype;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private boolean enableCustomStyle = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Switch enableCustom = findViewById(R.id.enable_custom_style);
        enableCustom.setChecked(enableCustomStyle);
        enableCustom.setOnCheckedChangeListener(
                (buttonView, isChecked) -> enableCustomStyle = isChecked);
    }

    // 设置直播间样式
    public void onStartLive(View view) {
        setLiveStyle();

        // 主播端: 开启直播
        String currentUserId = Const.getCurrentUserId();
        LivePrototype.OpenLiveParam param = new LivePrototype.OpenLiveParam();
        param.role = LivePrototype.Role.ANCHOR;
        param.nick = "用户" + currentUserId;
        param.liveShowMode = CanvasScale.Mode.ASPECT_FILL;
        LivePrototype.getInstance().setup(this, param, new Callback<String>() {
            @Override
            public void onSuccess(String liveId) {
                // 此处获取到liveId
                Log.i(TAG, "start live, liveId=" + liveId);
            }

            @Override
            public void onError(String errorMsg) {

            }
        });
    }

    // 观众端: 观看直播
    public void onViewLive(View view) {
        // 设置直播间样式
        setLiveStyle();

        // TODO: 此处替换主播开播时得到的liveId
        String liveId = "";
        if (TextUtils.isEmpty(liveId)) {
            String message = "请在代码处先填写liveId参数, 再重新运行";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = Const.getCurrentUserId();
        LivePrototype.OpenLiveParam param = new LivePrototype.OpenLiveParam();
        param.role = LivePrototype.Role.AUDIENCE;
        param.nick = "用户" + currentUserId;
        param.liveId = liveId;
        LivePrototype.getInstance().setup(this, param, null);
    }

    private void setLiveStyle() {
        // 设置直播间样式 (实际开发过程中, 样式确定后不需要每次设置; demo中涉及多种样式, 所以才会每次进入前都要设置)
        if (enableCustomStyle) {
            // 设置直播间自定义样式
            LiveHooker.setCustomStyle();
        } else {
            // 设置直播间默认样式
            LiveHooker.setDefaultStyle();
        }
    }
}