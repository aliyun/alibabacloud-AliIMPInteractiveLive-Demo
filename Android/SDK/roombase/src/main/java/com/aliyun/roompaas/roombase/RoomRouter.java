package com.aliyun.roompaas.roombase;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import java.io.Serializable;

/**
 * Created by KyleCe on 2021/11/3
 */
public class RoomRouter {

    public static void open(Class<?> clz, Context context, String roomId, String title, @Nullable String nick, @Nullable Bundle extras, Serializable serializable) {
        Intent intent = new Intent(context, clz);
        intent.putExtra(Const.PARAM_KEY_ROOM_ID, roomId);
        intent.putExtra(Const.PARAM_KEY_ROOM_TITLE, title);
        intent.putExtra(Const.PARAM_KEY_NICK, nick != null ? nick : "");
        if (extras != null) {
            intent.putExtras(extras);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Const.PARAM_KEY_EXTRA_SERIALIZABLE + serializable.getClass().getSimpleName(), serializable);
        context.startActivity(intent);
    }
}
