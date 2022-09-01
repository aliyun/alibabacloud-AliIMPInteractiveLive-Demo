package com.aliyun.roompaas.biz.exposable.model;

import java.io.Serializable;

/**
 * @author puke
 * @version 2021/5/12
 */
public class TokenInfo implements Serializable {

    public String accessToken;

    public String refreshToken;
}
