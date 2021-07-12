package com.aliyun.roompaas.app.helper;

import android.text.TextUtils;

import com.aliyun.roompaas.app.Const;
import com.aliyun.roompaas.app.sp.RoomTypeSp;
import com.aliyun.roompaas.app.sp.SpHelper;

/**
 * Created by KyleCe on 2021/7/5
 */
public class RoomHelper {
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
}
