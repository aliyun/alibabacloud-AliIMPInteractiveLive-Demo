package com.aliyun.roompaas.app.enums;

import android.text.TextUtils;

/**
 * @author puke
 * @version 2021/5/13
 */
public enum UserRole {

    HOST("host", "主播"),
    AUDIENCE("audience", "观众"),
    ;

    private final String value;
    private final String desc;

    UserRole(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public String getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    public static UserRole of(String value) {
        if (!TextUtils.isEmpty(value)) {
            for (UserRole userRole : values()) {
                if (userRole.value.equals(value)) {
                    return userRole;
                }
            }
        }
        return AUDIENCE;
    }
}
