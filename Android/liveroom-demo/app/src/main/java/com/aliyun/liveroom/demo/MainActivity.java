package com.aliyun.liveroom.demo;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.player.exposable.CanvasScale;
import com.aliyun.roompaas.roombase.Const;
import com.aliyun.standard.liveroom.lib.LivePrototype;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Mode currentMode = Mode.DEFAULT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Spinner selector = findViewById(R.id.mode_selector);
        selector.setOnItemSelectedListener(null);
        List<Mode> modes = new ArrayList<>(Arrays.asList(Mode.values()));
        selector.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                modes
        ));
        selector.setSelection(modes.indexOf(currentMode));
        selector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentMode = modes.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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
        param.supportLinkMic = currentMode == Mode.LINK_MIC;
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
        String liveId = "c7d451d8-efe4-49a6-9573-880689115c4d";
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
        param.supportLinkMic = currentMode == Mode.LINK_MIC;
        LivePrototype.getInstance().setup(this, param, new Callback<String>() {
            @Override
            public void onSuccess(String liveId) {

            }

            @Override
            public void onError(String errorMsg) {
                Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLiveStyle() {
        switch (currentMode) {
            case DEFAULT:
                LiveHooker.setDefaultStyle();
                break;
            case CUSTOM:
                LiveHooker.setCustomStyle();
                break;
            case LINK_MIC:
                LiveHooker.setLinkMicStyle();
                break;
        }
    }
}