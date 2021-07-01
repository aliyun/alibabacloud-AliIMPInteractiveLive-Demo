package com.aliyun.roompaas.app.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.TypeReference;
import com.aliyun.roompaas.app.response.OpenWhiteBoardResponse;
import com.aliyun.roompaas.app.response.Response;
import com.aliyun.roompaas.base.callback.Callback;

import java.lang.reflect.Type;

/**
 * Created by KyleCe on 2021/5/26
 */
public class OpenWhiteBoardAPI extends BaseAPI {

    //最后一步拿着dockey调用appserver的打开白板接口：
    //http://30.40.195.22:8080/whiteboard/open?docKey=1wvqrjWmyR0pqako&userId=123456
    private static final String API_FORMAT = "/whiteboard/open?docKey=%s&userId=%s";

    public static void openWhiteBoard(String docKey, String userId, final Callback<Response<OpenWhiteBoardResponse>> callback) {
        String api = String.format(API_FORMAT, docKey, userId);
        request(api, null, new Callback<String>() {
            @Override
            public void onSuccess(String data) {
                try {
                    Type type = new TypeReference<Response<OpenWhiteBoardResponse>>() {
                    }.getType();
                    Response<OpenWhiteBoardResponse> response = JSON.parseObject(data, type);
                    callback.onSuccess(response);
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
