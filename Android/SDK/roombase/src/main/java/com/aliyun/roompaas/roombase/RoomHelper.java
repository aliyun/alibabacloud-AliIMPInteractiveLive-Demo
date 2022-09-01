package com.aliyun.roompaas.roombase;

import android.support.annotation.IntDef;
import android.text.TextUtils;

import com.alibaba.dingpaas.room.RoomDetail;
import com.alibaba.dingpaas.room.RoomInfo;
import com.aliyun.roompaas.biz.exposable.RoomChannel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by KyleCe on 2021/7/5
 */
public class RoomHelper {
    @Retention(RetentionPolicy.RUNTIME)
    @IntDef({GeneralStatus.NOT_START, GeneralStatus.ON_GOING, GeneralStatus.END})
    public @interface GeneralStatus {
        int NOT_START = 0;
        int ON_GOING = 1;
        int END = 2;
    }

    public static String asNick(String id) {
        return "用户" + id;
    }

    public static String asRoomTitle(String id) {
        return asNick(id) + "的房间";
    }

    public static boolean isContentNotEmpty(String classId) {
        return !TextUtils.isEmpty(classId) && !TextUtils.isEmpty(classId.trim());
    }

    public static boolean isValidClassId(String id) {
        Pattern pattern = Pattern.compile("^[A-Za-z0-9-]{6,40}$");
        Matcher m = pattern.matcher(id);
        return m.matches();
    }

    public static String getOwnerId(RoomChannel roomChannel) {
        RoomDetail detail = roomChannel != null ? roomChannel.getRoomDetail() : null;
        RoomInfo info = detail != null ? detail.roomInfo : null;
        return info != null ? info.ownerId : "";
    }
}
