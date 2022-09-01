package com.aliyun.liveroom.demo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.live.exposable.AliLiveMediaStreamOptions;
import com.aliyun.roompaas.player.exposable.CanvasScale;
import com.aliyun.roompaas.roombase.Const;
import com.aliyun.roompaas.uibase.util.DialogUtil;
import com.aliyun.standard.liveroom.lib.LivePrototype;
import com.aliyun.standard.liveroom.lib.model.LiveRoomModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Mode currentMode = Mode.CUSTOM;
    private EditText liveIdInput;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        liveIdInput = findViewById(R.id.live_id);

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

        View config = findViewById(R.id.config);
        // 点击设置
        config.setOnClickListener(v -> AppConfig.updateConfig(this));
        // 长按清空
        config.setOnLongClickListener(v -> {
            AppConfig.clearConfig(this);
            return false;
        });
    }

    public void onStartLive(View view) {
        // 主播端: 开启直播
        readySetup(true);
    }

    public void onViewLive(View view) {
        // 观众端: 观看直播
        readySetup(false);
    }

    /**
     * 准备打开直播间页面
     *
     * @param isAnchor true:主播, false:观众
     */
    private void readySetup(boolean isAnchor) {
        // 设置直播间样式 (注: 实际业务开发时, 一般只有一种样式, 此时只需要全局设置一次即可)
        setLiveStyle(isAnchor);

        LivePrototype.OpenLiveParam param = new LivePrototype.OpenLiveParam();

        // 企业直播特殊设置
        if (currentMode == Mode.ENTERPRISE) {
            if (isAnchor) {
                DialogUtil.confirm(this, "企业直播不支持主播端哦", null);
                return;
            }
            param.supportPlayback = false;
            param.disableImmersive = true;
            param.statusBarColorStringWhenDisableImmersive = "#ffffff";
        }

        if (isAnchor) {
            // 主播端: 开启直播

            // 设置角色
            param.role = LivePrototype.Role.ANCHOR;

            // 设置直播Id (注: 这里可以传递直播Id, 如果传递就已主播身份进入该直播, 如果不传, SDK就新建并进入)
            param.liveId = null;

            // 设置推流填充模式
            param.liveShowMode = CanvasScale.Mode.ASPECT_FILL;

            // 设置推流全量参数 (注: 该参数会覆盖liveShowMode配置, 取代的是previewDisplayMode属性)
            AliLiveMediaStreamOptions pusherOptions = new AliLiveMediaStreamOptions();
            param.mediaPusherOptions = pusherOptions;
            pusherOptions.previewDisplayMode = AliLiveMediaStreamOptions.getPreviewDisplayMode(param.liveShowMode);

            // 设置直播间信息
            LiveRoomModel liveRoomModel = new LiveRoomModel();
            liveRoomModel.title = param.nick + "的直播";
            liveRoomModel.coverUrl = "https://gw.alicdn.com/imgextra/i3/O1CN01jmDcVV29uDaUHrI8g_!!6000000008127-0-tps-1024-681.jpg";
            // 设置业务自定义信息, 直播附属的一些属性, 可以在这里添加, 直播间内部获取可以参考:
            // com.aliyun.vpaas.standard.ecommerce.view.LiveInfoView.Component.onInit
            liveRoomModel.extension = new HashMap<String, String>() {{
                put("anchorAvatarURL", "https://gw.alicdn.com/imgextra/i4/O1CN01J9xh0a1QBeKUiazg6_!!6000000001938-2-tps-80-80.png");
                put("anchorIntroduction", "品牌总监，公司品牌宣传推广负责人");
                put("liveIntroduction", "这是一个直播简介");
            }};
            param.liveRoomModel = liveRoomModel;
        } else {
            // 观众端: 观看直播
            String liveId = liveIdInput.getText().toString().trim();
            if (TextUtils.isEmpty(liveId)) {
                String message = "请先填写liveId, 再重新运行";
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                return;
            }

            // 设置观众端直播填充方式
            param.liveShowMode = CanvasScale.Mode.ASPECT_FILL;

            // 设置角色
            param.role = LivePrototype.Role.AUDIENCE;

            // 设置直播Id
            param.liveId = liveId;
        }

        param.nick = "用户" + Const.getCurrentUserId();
        param.supportLinkMic = currentMode == Mode.LINK_MIC;
        LivePrototype.getInstance().setup(this, param, new Callback<String>() {
            @Override
            public void onSuccess(String liveId) {
                // 此处获取到liveId (liveId可以提供给观众端填写并观看)
                Log.i(TAG, "start live, liveId=" + liveId);
            }

            @Override
            public void onError(String errorMsg) {
                Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLiveStyle(boolean isAnchor) {
        switch (currentMode) {
            case DEFAULT:
                // 纯净样式 (不做任何定制的样式)
                LiveHooker.setDefaultStyle();
                break;
            case CUSTOM:
                // 自定义消息 (该样式仅做参考, 方便快速找到SDK中组件)
                LiveHooker.setCustomStyle();
                break;
            case LINK_MIC:
                // 连麦样式 (该样式跟其他样式差异化较大, 内部支持rtc协议)
                LiveHooker.setLinkMicStyle(isAnchor);
                break;
            case ECOMMERCE:
                // 电商直播样式 (对应 ui-ecommerce 模块, 完全开源, 可选择性复用)
                LiveHooker.setEcommerceStyle(isAnchor);
                break;
            case ENTERPRISE:
                // 企业直播样式 (对应 ui-enterprise 模块, 完全开源, 可选择性复用)
                LiveHooker.setEnterPriseStyle(isAnchor);
                break;
        }
    }
}