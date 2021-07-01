package com.aliyun.roompaas.app.model;

import java.io.Serializable;

/**
 * Created by KyleCe on 2021/5/26
 */
public class DocumentAccessInfo implements Serializable {
    public String accessToken;
    public String collabHost;
    public int permission;
    public String docKey;
    public UserInfo userInfo;
}
