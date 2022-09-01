package com.aliyun.roompaas.base.util;

import android.support.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by KyleCe on 2021/7/28
 */
public class JSONUtil {

    public static String parseStringParam(@NonNull JSONObject obj, @NonNull String key) {
        Object val;
        if (obj.containsKey(key) && (val = obj.get(key)) instanceof String) {
            return (String) val;
        }
        return "";
    }

    public static Boolean parseBooleanParam(@NonNull JSONObject obj, @NonNull String key) {
        Object val;
        if (obj.containsKey(key) && (val = obj.get(key)) instanceof Boolean) {
            return (Boolean) val;
        }
        return false;
    }

    public static String loadJSON(File file) {
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            int size = is.available();
            byte[] byteArray = new byte[size];
            is.read(byteArray);
            return new String(byteArray, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            IOUtil.close(is);
        }
    }
}
