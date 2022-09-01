package com.aliyun.roompaas.app.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.TypeReference;
import com.aliyun.roompaas.app.request.DestroyRoomRequest;
import com.aliyun.roompaas.app.response.CreateRoomResponse;
import com.aliyun.roompaas.app.response.Response;
import com.aliyun.roompaas.base.exposable.Callback;

import java.lang.reflect.Type;

/**
 * @author puke
 * @version 2021/5/14
 */
public class DestroyRoomApi extends BaseAPI {

    private static final String TAG = DestroyRoomApi.class.getSimpleName();

    /**
     * 创建房间
     *
     * @param request 请求参数
     */
    public static void destroyRoom(final DestroyRoomRequest request, Callback<Void> callback) {
        request("/api/login/destroyRoom", request, new Callback<String>() {
            @Override
            public void onSuccess(String data) {
                try {
                    Type type = new TypeReference<Response<CreateRoomResponse>>() {
                    }.getType();
                    Response<Void> response = JSON.parseObject(data, type);
                    if (response.responseSuccess) {
                        callback.onSuccess(null);
                    } else {
                        callback.onError(response.message);
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
