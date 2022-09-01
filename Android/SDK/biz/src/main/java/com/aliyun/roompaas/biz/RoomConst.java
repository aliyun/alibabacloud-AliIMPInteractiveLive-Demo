package com.aliyun.roompaas.biz;

/**
 * SDK内部常量统一配置
 *
 * @author puke
 * @version 2021/7/2
 */
public class RoomConst {
    public static final String PRE_INFO = "预发（内网环境）";
    public static final String ONLINE_INFO = "线上（AppServer不可变更）";

    public static final String LONG_LINK_URL_4_PRE = "tls://pre-tls.imp.aliyuncs.com:443";

    public static final String LONG_LINK_URL_4_ONLINE = "tls://tls.imp.aliyuncs.com:443";

    public static final Boolean USE_META_PATH = true;

    public static final String META_LONG_URL_4_PRE = "114.55.233.244:30301";

    public static final String META_LONG_URL_4_ONLINE = "114.55.233.244:30301";
}
