package com.aliyun.roompaas.roombase.api;

import android.support.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.roompaas.base.callback.UICallback;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.base.util.CommonUtil;
import com.aliyun.roompaas.base.util.SignUtil;
import com.aliyun.roompaas.base.util.ThreadUtil;
import com.aliyun.roompaas.base.util.Utils;
import com.aliyun.roompaas.roombase.Const;
import com.aliyun.roompaas.roombase.request.BaseReq;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by KyleCe on 2021/5/26
 */
public class BaseAPI {

    public static void request(String api, @Nullable final BaseReq request, @Nullable Callback<String> callback) {
        final String apiURL = Const.getAppServer() + api;
        final UICallback<String> uiCallback = new UICallback<>(callback);
        ThreadUtil.runOnSubThread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(apiURL);
                    Map<String, String> headers = generateHeaders();
                    Map<String, String> params = generateParams(request);
                    String sing = SignUtil.verifySign(Const.getAppSecret(), "POST", apiURL, params, headers);

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.addRequestProperty("a-signature", sing);
                    for (Map.Entry<String, String> entry : headers.entrySet()) {
                        connection.addRequestProperty(entry.getKey(), entry.getValue());
                    }

                    OutputStream os = connection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(os, StandardCharsets.UTF_8));
                    writer.write(getPostDataString(params));

                    writer.flush();
                    writer.close();
                    os.close();

                    connection.setConnectTimeout(5000);
                    connection.connect();
                    InputStream inputStream = connection.getInputStream();
                    InputStreamReader reader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(reader);
                    String temp;
                    StringBuilder stringBuffer = new StringBuilder();
                    while ((temp = bufferedReader.readLine()) != null) {
                        stringBuffer.append(temp);
                    }
                    final String jsonStr = stringBuffer.toString();
                    try {
                        JSONObject jsonObject = JSON.parseObject(jsonStr);
                        boolean responseSuccess = jsonObject.getBooleanValue("responseSuccess");
                        if (responseSuccess) {
                            uiCallback.onSuccess(jsonStr);
                        } else {
                            String errorMsg = jsonObject.getString("errorMsg");
                            String message = jsonObject.getString("message");
                            uiCallback.onError(Utils.acceptFirstNotEmpty(errorMsg, message));
                        }
                    } catch (JSONException e) {
                        uiCallback.onError(e.getMessage());
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    uiCallback.onError(e.getMessage());
                }
            }
        });
    }

    protected static Map<String, String> generateHeaders() {
        String stamp = SignUtil.getUTCTimeStampFormatString();
        String nonce = SignUtil.generateNonce();

        Map<String, String> headers = new HashMap<>();
        headers.put("a-app-id", "imp-room"); // Call Doc Server: RTC, Callback: WB
        headers.put("a-signature-method", "HMAC-SHA1");
        headers.put("a-signature-version", "1.0");
        headers.put("a-timestamp", stamp);
        headers.put("a-signature-nonce", nonce);
        return headers;
    }

    protected static Map<String, String> generateParams(final BaseReq request) {
        Map<String, String> params = new HashMap<>();
        params.put("appId", Const.getAppId());
        params.put("appKey", Const.getAppKey());
        params.put("deviceId", CommonUtil.getDeviceId());
        if (request != null) {
            request.appendParams(params);
        }

        HashSet<Map.Entry<String, String>> entries = new HashSet<>(params.entrySet());
        for (Map.Entry<String, String> entry : entries) {
            if (entry.getValue() == null) {
                params.remove(entry.getKey());
            }
        }
        return params;
    }

    public static String getPostDataString(Map<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    public static <T> void takeResultWithNullCheck(@Nullable Callback<T> callback, @Nullable T result) {
        if (result == null) {
            Utils.callError(callback, "null");
        } else {
            Utils.callSuccess(callback, result);
        }
    }
}
