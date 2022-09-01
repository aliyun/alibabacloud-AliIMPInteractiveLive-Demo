package com.aliyun.roompaas.app.helper;

import android.content.Context;
import android.content.Intent;

import com.aliyun.roompaas.app.activity.EnterCreateRoomInfoActivity;
import com.aliyun.roompaas.app.activity.RoomListActivity;
import com.aliyun.roompaas.app.activity.base.BaseRoomActivity;
import com.aliyun.roompaas.app.activity.business.BusinessActivity;
import com.aliyun.roompaas.app.activity.classroom.ClassroomActivity;

/**
 * Created by KyleCe on 2021/7/6
 */
public class Router {
    public static void openRoomViaBizType(Context context, String roomId, String roomTitle, String userId) {
        if (RoomHelper.isTypeBusiness()) {
            openBusinessRoomPage(context, roomId, roomTitle, userId);
        } else {
            openClassRoomPage(context, roomId, roomTitle, userId);
        }
    }

    public static void openEnterRoomInfoPage(Context context) {
        Intent intent = new Intent(context, EnterCreateRoomInfoActivity.class);
        context.startActivity(intent);
    }

    public static void openBusinessRoomPage(Context context, String roomId, String roomTitle, String nick) {
        Intent intent = new Intent(context, BusinessActivity.class);
        BaseRoomActivity.open(context, intent, roomId, roomTitle, nick);
    }

    public static void openClassRoomPage(Context context, String roomId, String roomTitle, String nick) {
        Intent intent = new Intent(context, ClassroomActivity.class);
        BaseRoomActivity.open(context, intent, roomId, roomTitle, nick);
    }

    public static void openRoomListPage(Context context) {
        Intent intent = new Intent(context, RoomListActivity.class);
        context.startActivity(intent);
    }

}
