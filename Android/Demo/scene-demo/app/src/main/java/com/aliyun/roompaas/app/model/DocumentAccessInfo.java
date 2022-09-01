package com.aliyun.roompaas.app.model;

import java.io.Serializable;

/**
 * Created by KyleCe on 2021/5/26
 */
public class DocumentAccessInfo implements Serializable {
    public static final int PERMISSION_NONE = 0;
    public static final int PERMISSION_READ_ONLY = 1;
    public static final int PERMISSION_READ_AND_WRITE = 2;

    public String accessToken;
    public String collabHost;
    public int permission = PERMISSION_NONE;
    public String docKey;

    public String wsDomain = "";
    public String userId = "";
}
