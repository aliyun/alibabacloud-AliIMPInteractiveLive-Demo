package com.aliyun.roompaas.app.api;

import com.alibaba.dingpaas.base.DPSAuthToken;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.TypeReference;
import com.aliyun.roompaas.app.request.GetTokenRequest;
import com.aliyun.roompaas.app.response.Response;
import com.aliyun.roompaas.base.callback.Callback;

import java.lang.reflect.Type;

/**
 * @author puke
 * @version 2021/5/17
 */
public class GetTokenApi extends BaseAPI{
    private static final String TAG = GetTokenApi.class.getSimpleName();

    public static void getToken(final String uid, final Callback<DPSAuthToken> callback) {
        request("/api/login/getToken", new GetTokenRequest(uid), new Callback<String>() {
            @Override
            public void onSuccess(String data) {
                try {
                    Type type = new TypeReference<Response<DPSAuthToken>>() {
                    }.getType();
                    Response<DPSAuthToken> response = JSON.parseObject(data, type);
                    if (response.result == null) {
                        callback.onError("null");
                    } else {
                        callback.onSuccess(response.result);
                    }
                } catch (JSONException e) {
                    callback.onError(e.getMessage());
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String errorMsg) {
                callback.onError(errorMsg);
            }
        });
    }
}
