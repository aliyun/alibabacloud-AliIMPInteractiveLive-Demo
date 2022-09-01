package com.aliyun.roompaas.app.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.TypeReference;
import com.aliyun.roompaas.app.request.CreateRoomRequest;
import com.aliyun.roompaas.app.response.CreateRoomResponse;
import com.aliyun.roompaas.app.response.Response;
import com.aliyun.roompaas.base.exposable.Callback;

import java.lang.reflect.Type;

/**
 * @author puke
 * @version 2021/5/14
 */
public class CreateRoomApi extends BaseAPI{
    private static final String TAG = CreateRoomApi.class.getSimpleName();
    /**
     * 创建房间
     *
     * @param request 请求参数
     */
    public static void createRoom(final CreateRoomRequest request, Callback<Response<CreateRoomResponse>> callback) {
        request("/api/login/createRoom", request, new Callback<String>() {
            @Override
            public void onSuccess(String data) {
                try {
                    Type type = new TypeReference<Response<CreateRoomResponse>>() {
                    }.getType();
                    Response<CreateRoomResponse> response = JSON.parseObject(data, type);
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
