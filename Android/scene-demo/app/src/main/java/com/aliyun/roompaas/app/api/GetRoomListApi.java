package com.aliyun.roompaas.app.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.TypeReference;
import com.aliyun.roompaas.app.request.RoomListRequest;
import com.aliyun.roompaas.app.request.RoomListResponse;
import com.aliyun.roompaas.app.response.Response;
import com.aliyun.roompaas.base.exposable.Callback;

import java.lang.reflect.Type;

/**
 * @author puke
 * @version 2021/10/29
 */
public class GetRoomListApi extends BaseAPI {

    private static final String TAG = GetRoomListApi.class.getSimpleName();

    public static void getRoomList(final RoomListRequest request, final Callback<RoomListResponse> callback) {
        request("/api/login/getRoomList", request, new Callback<String>() {
            @Override
            public void onSuccess(String data) {
                try {
                    Type type = new TypeReference<Response<RoomListResponse>>() {
                    }.getType();
                    Response<RoomListResponse> response = JSON.parseObject(data, type);
                    takeResultWithNullCheck(callback, response.result);
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
