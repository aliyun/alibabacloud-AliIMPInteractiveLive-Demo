package com.aliyun.roompaas.app;

import com.aliyun.roompaas.app.sp.EnvSp;
import com.aliyun.roompaas.app.sp.SpHelper;
import com.aliyun.roompaas.base.util.CommonUtil;
import com.aliyun.roompaas.biz.enums.EnvType;

/**
 * @author puke
 * @version 2021/5/13
 */
public abstract class Const {

    /**
     * APP_ID (来自于阿里云控制台)
     */
    public static final String APP_ID = "l****u";

    /**
     * APP_KEY (来自于阿里云控制台)
     */
    public static final String APP_KEY = "2cf8c17************022eb36";

    /**
     * 环境 (内部开发调试使用, 外部统一用{@link EnvType#ONLINE})
     */
    public static final EnvType ENV_TYPE = EnvType.valueOf(SpHelper.getInstance(EnvSp.class).getEnv());

    /**
     * sdk请求的长连接地址 (来自于阿里云控制台)
     */
    public static final String LONG_LINK_URL = ENV_TYPE == EnvType.PRE
            ? "tls-cloud-pre.****.com:443"
            : "tls-cloud.****.com:443";

    /**
     * 设备Id (注: SDK初始化和getLoginToken时保持一致)
     */
    public static final String DEVICE_ID = CommonUtil.getDeviceId();

    /**
     * 用户服务端链接地址 (接入时替换为自己的服务地址)
     */
    public static final String APP_SERVER_URL = "http://****:8080";

    /**
     * 验签公钥 (用户服务端按需选择)
     */
    public static final String SIGN_SECRET = "h****2";

    /**
     * 当前登录用户Id (仅Demo使用, 接入时替换为App内部的userId)
     */
    public static String currentUserId;
}