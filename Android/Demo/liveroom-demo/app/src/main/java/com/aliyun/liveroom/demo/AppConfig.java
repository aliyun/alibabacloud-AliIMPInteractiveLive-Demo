package com.aliyun.liveroom.demo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.roompaas.base.util.ThreadUtil;
import com.aliyun.roompaas.uibase.helper.SpHelper;
import com.aliyun.standard.liveroom.lib.LivePrototype;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * App配置类, 用来支持不改代码更改配置信息
 *
 * @author puke
 * @version 2022/3/2
 */
public class AppConfig {

    private static final String SP_NAME = "app_config_of_sdk";
    private static final String KEY_CONFIG = "config";

    /**
     * @param context 用户手动设置的配置  (为了便于开发测试时更换配置参数, 生产环境不需要)
     */
    public static void updateConfig(Context context) {
        String configStr = SpHelper.get(SP_NAME, KEY_CONFIG, null);
        JSONObject config = JSON.parseObject(configStr);

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        List<String> keys = Arrays.asList("appId", "appKey", "serverHost", "serverSecret");

        Map<String, EditText> key2Input = new HashMap<>();
        for (String key : keys) {
            EditText editText = new EditText(context);
            editText.setHint("请输入" + key);
            layout.addView(editText);
            key2Input.put(key, editText);

            if (config != null) {
                editText.setText(config.getString(key));
            }
        }

        new AlertDialog.Builder(context)
                .setTitle("修改默认配置")
                .setView(layout)
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", (dialog, which) -> {
                    JSONObject json = new JSONObject();
                    for (String key : keys) {
                        String value = key2Input.get(key).getText().toString();
                        json.put(key, value);
                    }
                    SpHelper.set(SP_NAME, KEY_CONFIG, json.toJSONString());
                    relaunchApp(context);
                })
                .show();
    }

    /**
     * @return 读取用户设置的配置
     */
    @Nullable
    public static LivePrototype.InitParam getConfig() {
        String configStr = SpHelper.get(SP_NAME, KEY_CONFIG, null);
        return JSON.parseObject(configStr, LivePrototype.InitParam.class);
    }

    /**
     * 清空用户设置的配置
     */
    public static void clearConfig(Context context) {
        new AlertDialog.Builder(context)
                .setTitle("是否清空当前配置")
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", (dialog, which) -> {
                    SpHelper.set(SP_NAME, KEY_CONFIG, null);
                    relaunchApp(context);
                })
                .show();
    }

    private static void relaunchApp(Context context) {
        ThreadUtil.postDelay(200, () -> {
            final Intent intent = context.getPackageManager()
                    .getLaunchIntentForPackage(context.getPackageName());
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
            }
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        });
    }
}
