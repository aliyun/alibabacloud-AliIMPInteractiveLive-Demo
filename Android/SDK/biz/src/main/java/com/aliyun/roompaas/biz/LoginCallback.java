package com.aliyun.roompaas.biz;

/**
 * @author puke
 * @version 2021/4/28
 */
public interface LoginCallback {

    void onGetToken(TokenHandler handler);

    void onLoginSuccess();

    void onLoginError(String errorMsg);
}
