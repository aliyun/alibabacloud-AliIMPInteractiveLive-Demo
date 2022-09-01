package com.aliyun.roompaas.roombase;

/**
 * Created by KyleCe on 2021/10/27
 */
public class Const {

    // 页面跳转相关配置
    public static final String PARAM_KEY_ROOM_ID = getSdkKey("room_id");
    public static final String PARAM_KEY_ROOM_TITLE = getSdkKey("room_title");
    public static final String PARAM_KEY_NICK = getSdkKey("nick");
    public static final String PARAM_KEY_USER_EXTENSION = getSdkKey("user_extension");
    public static final String PARAM_KEY_DISABLE_IMMERSIVE = getSdkKey("disable_immersive");
    public static final String PARAM_KEY_STATUS_BAR_COLOR_STRING_WHEN_DISABLE_IMMERSIVE = getSdkKey("status_bar_color_string_when_disable_immersive");
    public static final String PARAM_KEY_PERMISSION_IGNORE_STRICT_CHECK = getSdkKey("permission_ignore_strict_check");
    public static final String PARAM_KEY_EXTRA_SERIALIZABLE = getSdkKey("extra_serializable_prefix_with_class_simple_name_");

    protected static BizInitParam sParamViaUserConfig;

    public static <P extends BizInitParam> void injectConfig(P param) {
        sParamViaUserConfig = param;
    }

    public static void injectNewUser(String userId) {
        if (sParamViaUserConfig != null) {
            sParamViaUserConfig.userId = userId;
        }
    }

    public static String getAppId() {
        return sParamViaUserConfig != null ? sParamViaUserConfig.appId : "";
    }

    public static String getAppKey() {
        return sParamViaUserConfig != null ? sParamViaUserConfig.appKey : "";
    }

    public static String getAppServer() {
        return sParamViaUserConfig != null ? sParamViaUserConfig.serverHost : "";
    }

    public static String getAppSecret() {
        return sParamViaUserConfig != null ? sParamViaUserConfig.serverSecret : "";
    }

    /**
     * 当前登录用户Id (仅Demo使用, 接入时替换为App内部的userId)
     */
    public static String getCurrentUserId() {
        return sParamViaUserConfig != null ? sParamViaUserConfig.userId : "";
    }

    protected static String getSdkKey(String key) {
        return "imp_sdk_key_" + key;
    }
}
