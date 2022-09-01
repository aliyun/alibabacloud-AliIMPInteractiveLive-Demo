package com.aliyun.roompaas.base.callback;

import com.aliyun.roompaas.base.exposable.Callback;

/**
 * @author puke
 * @version 2021/5/27
 */
public class LambdaCallback<T> implements Callback<T> {

    private final Callback<T> callback;

    public interface Callback<T> {
        void onCall(boolean success, T data, String errorMsg);
    }

    public LambdaCallback(Callback<T> callback) {
        this.callback = callback;
    }

    @Override
    public void onSuccess(T data) {
        if (callback != null) {
            callback.onCall(true, data, null);
        }
    }

    @Override
    public void onError(String errorMsg) {
        if (callback != null) {
            callback.onCall(false, null, errorMsg);
        }
    }
}
