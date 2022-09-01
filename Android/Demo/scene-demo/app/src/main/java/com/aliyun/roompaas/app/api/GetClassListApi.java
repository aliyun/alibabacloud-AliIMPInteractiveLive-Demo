package com.aliyun.roompaas.app.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.aliyun.roompaas.app.Const;
import com.aliyun.roompaas.app.model.ClassListResponse;
import com.aliyun.roompaas.app.request.RoomListRequest;
import com.aliyun.roompaas.base.callback.UICallback;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.base.util.IOUtil;
import com.aliyun.roompaas.base.util.ThreadUtil;
import com.aliyun.roompaas.base.util.Utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

/**
 * @author puke
 * @version 2021/10/29
 */
public class GetClassListApi extends BaseAPI {

    private static final String TAG = GetClassListApi.class.getSimpleName();

    public static void getClassList(final RoomListRequest request, Callback<ClassListResponse> cal) {
        final Callback<ClassListResponse> uiCallback = new UICallback<>(cal);
        ThreadUtil.runOnSubThread(()->{
            String api = String.format(Locale.getDefault(),
                    "%s/api/standroom/class/listClasses?appId=%s&pageNumber=%d&pageSize=%d&status=%d",
                    Const.getServerHost(), request.appId, request.pageNumber, request.pageSize, request.status);

            HttpURLConnection urlConnection = null;
            BufferedReader br = null;
            try {
                URL url = new URL(api);
                urlConnection = (HttpURLConnection) url.openConnection();

                int responseCode = urlConnection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    Utils.callError(uiCallback, urlConnection.getResponseMessage());
                } else {
                    br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append("\n");
                    }

                    Type type = new TypeReference<GetResponse<ClassListResponse>>() {
                    }.getType();
                    GetResponse<ClassListResponse> response = JSON.parseObject(sb.toString(), type);
                    takeResultWithNullCheck(uiCallback, response.result);
                }
            } catch (Exception e) {
                Utils.callError(uiCallback, e.getMessage());
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                IOUtil.close(br);
            }
        });
    }
}
