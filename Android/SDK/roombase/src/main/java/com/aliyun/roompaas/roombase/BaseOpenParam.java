package com.aliyun.roompaas.roombase;

import java.util.HashMap;

/**
 * Created by KyleCe on 2021/11/2
 */
public class BaseOpenParam {
    public static final int DEFAULT_SEND_COMMENT_MAX_LEN = 50;

    public String nick;
    public HashMap<String, String> userExtension;
    public int sendCommentMaxLength = DEFAULT_SEND_COMMENT_MAX_LEN;

    public boolean disableImmersive;
    public String statusBarColorStringWhenDisableImmersive;
    public boolean permissionIgnoreStrictCheck;
}
