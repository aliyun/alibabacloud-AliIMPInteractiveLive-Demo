package com.aliyun.roompaas.app.helper;

import android.support.annotation.IntDef;
import android.text.TextUtils;

import com.alibaba.dingpaas.room.RoomDetail;
import com.alibaba.dingpaas.room.RoomInfo;
import com.aliyun.roompaas.app.Const;
import com.aliyun.roompaas.app.sp.RoomTypeSp;
import com.aliyun.roompaas.app.sp.SpHelper;
import com.aliyun.roompaas.biz.exposable.RoomChannel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by KyleCe on 2021/7/5
 */
public class RoomHelper {
    @Retention(RetentionPolicy.RUNTIME)
    @IntDef({ConferenceStatus.NOT_START, ConferenceStatus.ON_GOING, ConferenceStatus.END})
    public @interface ConferenceStatus {
        int NOT_START = 0;
        int ON_GOING = 1;
        int END = 2;
    }

    @Const.BIZ_TYPE
    private static String sRoomType;

    public static boolean isTypeBusiness() {
        if (TextUtils.isEmpty(sRoomType)) {
            sRoomType = SpHelper.getInstance(RoomTypeSp.class).getRoomType();
        }
        if (TextUtils.isEmpty(sRoomType)) {
            sRoomType = Const.BIZ_TYPE.DEFAULT;
            SpHelper.getInstance(RoomTypeSp.class).setRoomType(sRoomType);
            return true;
        }

        return Const.BIZ_TYPE.BUSINESS.equals(sRoomType);
    }

    public static boolean isTypeSameWithCurrent(@Const.BIZ_TYPE String roomType) {
        return (Const.BIZ_TYPE.BUSINESS.equals(roomType) && RoomHelper.isTypeBusiness()) ||
                (Const.BIZ_TYPE.CLASSROOM.equals(roomType) && !RoomHelper.isTypeBusiness());
    }

    public static void updateTypeSelected(@Const.BIZ_TYPE String roomType) {
        sRoomType = roomType;
        SpHelper.getInstance(RoomTypeSp.class).setRoomType(roomType);
    }

    public static String getOwnerId(RoomChannel roomChannel) {
        RoomDetail detail = roomChannel != null ? roomChannel.getRoomDetail(): null;
        RoomInfo info = detail != null ? detail.roomInfo : null;
        return info != null ? info.ownerId : "";
    }

}
