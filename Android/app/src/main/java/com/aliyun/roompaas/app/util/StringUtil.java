package com.aliyun.roompaas.app.util;

/**
 * @author puke
 * @version 2021/5/21
 */
public class StringUtil {

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
}
