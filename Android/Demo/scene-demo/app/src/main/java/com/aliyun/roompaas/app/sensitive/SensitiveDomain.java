package com.aliyun.roompaas.app.sensitive;

import com.aliyun.roompaas.app.BuildConfig;

/**
 * Created by KyleCe on 2021/7/6
 */
public class SensitiveDomain implements ISensitive {
    /**
     * APP_ID (来自于阿里云控制台)
     */
    public static final String APP_ID_FOR_BUSINESS = BuildConfig.EXTERNAL_BUSINESS_APP_ID;
    public static final String APP_ID_FOR_CLASSROOM = BuildConfig.EXTERNAL_CLASSROOM_APP_ID;

    /**
     * APP_KEY (来自于阿里云控制台)
     */
    public static final String APP_KEY_FOR_BUSINESS = BuildConfig.EXTERNAL_BUSINESS_APP_KEY;
    public static final String APP_KEY_FOR_CLASSROOM = BuildConfig.EXTERNAL_CLASSROOM_APP_KEY;

    /**
     * 用户服务端链接地址 (接入时替换为自己的服务地址)
     */
    public static final String SERVER_HOST_BUSINESS = BuildConfig.EXTERNAL_BUSINESS_APP_SERVER_HOST;
    public static final String SERVER_HOST_CLASSROOM = BuildConfig.EXTERNAL_CLASSROOM_APP_SERVER_HOST;

    /**
     * 验签公钥 (用户服务端按需选择)
     */
    public static final String SERVER_SECRET_BUSINESS = BuildConfig.EXTERNAL_BUSINESS_APP_SERVER_SECRET;
    public static final String SERVER_SECRET_CLASSROOM = BuildConfig.EXTERNAL_CLASSROOM_APP_SERVER_SECRET;
}
