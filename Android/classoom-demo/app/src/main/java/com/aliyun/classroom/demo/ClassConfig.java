package com.aliyun.classroom.demo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;

import com.aliyun.roompaas.classroom.lib.core.ClassInitParam;

import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * Created by KyleCe on 2021/12/16
 */
public class ClassConfig {
    private static final String MOCK_APP_ID = "";// FIXME: by KyleCe on 2021/12/16 填写AppId
    private static final String MOCK_APP_KEY = "";// FIXME: by KyleCe on 2021/12/16 填写AppKey
    private static final String MOCK_APP_SERVER = "";// FIXME: by KyleCe on 2021/12/16 填写AppServer
    private static final String MOCK_SERVER_SECRET = "";// FIXME: by KyleCe on 2021/12/16 填写ServerSecret

    public static ClassInitParam asClassInitParam(String userId) {
        ClassInitParam param = new ClassInitParam();
        param.appId = MOCK_APP_ID;
        param.appKey = MOCK_APP_KEY;
        param.userId = userId;
        param.serverHost = MOCK_APP_SERVER;
        param.serverSecret = MOCK_SERVER_SECRET;
        return param;
    }

    public static String acquireUserId(Context context) {
        return getMD5(acquireMacAddress(context)).substring(0, 6);
    }

    private static String getMD5(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            return new BigInteger(1, md.digest()).toString(16);
        } catch (Exception e) {
            throw new RuntimeException("MD5加密出现错误");
        }
    }

    @SuppressLint("HardwareIds")
    public static String acquireMacAddress(@NonNull Context context) {
        try {
            WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = manager.getConnectionInfo();
            return info.getMacAddress();
        } catch (Throwable ignore) {
            return "";
        }
    }
}
