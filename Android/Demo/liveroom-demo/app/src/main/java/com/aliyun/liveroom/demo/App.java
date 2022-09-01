package com.aliyun.liveroom.demo;

import android.app.Application;

import com.aliyun.roompaas.base.util.CommonUtil;
import com.aliyun.standard.liveroom.lib.LivePrototype;

import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * @author puke
 * @version 2021/7/20
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化&认证
        LivePrototype.InitParam param = AppConfig.getConfig();
        if (param == null) {
            // 对于首次使用的用户, 直接更改以下配置参数
            param = new LivePrototype.InitParam();
            param.appId = BuildConfig.INNER_BUSINESS_APP_ID;
            param.appKey = BuildConfig.INNER_BUSINESS_APP_KEY;
            param.serverHost = BuildConfig.INNER_BUSINESS_APP_SERVER_HOST;
            param.serverSecret = BuildConfig.INNER_BUSINESS_APP_SERVER_SECRET;
        }

        // TODO: 此处userId取设备md5, 实际接入时要替换为业务userId
        param.userId = getMD5(CommonUtil.getDeviceId()).substring(0, 6);
        LivePrototype.getInstance().init(this, param);
    }

    /**
     * 对字符串md5加密
     */
    private static String getMD5(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            return new BigInteger(1, md.digest()).toString(16);
        } catch (Exception e) {
            throw new RuntimeException("MD5加密出现错误");
        }
    }
}
