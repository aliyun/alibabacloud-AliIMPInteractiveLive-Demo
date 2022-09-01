package com.aliyun.roompaas.biz;

/**
 * @author puke
 * @version 2021/4/28
 */
public interface TokenHandler {

    void doNext(String accessToken, String refreshToken);
}
