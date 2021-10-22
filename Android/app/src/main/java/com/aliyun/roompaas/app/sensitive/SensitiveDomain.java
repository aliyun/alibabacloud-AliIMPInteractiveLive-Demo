package com.aliyun.roompaas.app.sensitive;

/**
 * Created by KyleCe on 2021/7/6
 */
public class SensitiveDomain implements ISensitive {
    /**
     * APP_ID (来自于阿里云控制台)
     */
    public static final String APP_ID_FOR_BUSINESS = "*";
    public static final String APP_ID_FOR_CLASSROOM = "*";

    /**
     * APP_KEY (来自于阿里云控制台)
     */
    public static final String APP_KEY_FOR_BUSINESS = "*";
    public static final String APP_KEY_FOR_CLASSROOM = "*";

    /**
     * 用户服务端链接地址 (接入时替换为自己的服务地址)
     */
    public static final String APP_SERVER_URL = "*";

    /**
     * 验签公钥 (用户服务端按需选择)
     */
    public static final String SIGN_SECRET = "*";
}
