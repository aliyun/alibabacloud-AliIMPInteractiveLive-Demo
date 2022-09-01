package com.aliyun.standard.liveroom.lib;

import android.content.Context;
import android.content.Intent;

import com.aliyun.roompaas.roombase.Const;

/**
 * Created by KyleCe on 2021/7/6
 */
class Router {

    static void openBusinessRoomPage(Context context, String roomId,
                                     LiveInnerParam innerParam, LivePrototype.OpenLiveParam param) {
        Intent intent = new Intent(context, LiveActivity.class);
        intent.putExtra(Const.PARAM_KEY_ROOM_ID, roomId);
        intent.putExtra(Const.PARAM_KEY_NICK, param.nick);
        intent.putExtra(Const.PARAM_KEY_USER_EXTENSION, param.userExtension);
        intent.putExtra(Const.PARAM_KEY_DISABLE_IMMERSIVE, param.disableImmersive);
        intent.putExtra(Const.PARAM_KEY_STATUS_BAR_COLOR_STRING_WHEN_DISABLE_IMMERSIVE, param.statusBarColorStringWhenDisableImmersive);
        intent.putExtra(Const.PARAM_KEY_PERMISSION_IGNORE_STRICT_CHECK, param.permissionIgnoreStrictCheck);
        intent.putExtra(LiveConst.PARAM_KEY_LIVE_INNER_PARAM, innerParam);

//        intent.putExtra(LiveConst.PARAM_KEY_ROLE, param.role.value);
//        intent.putExtra(LiveConst.PARAM_KEY_STATUS, liveStatus.value);
//        intent.putExtra(LiveConst.PARAM_KEY_LIVE_ID, param.liveId);

        if (param.startActivityCallback != null) {
            param.startActivityCallback.beforeStartActivity(intent);
        }

        if (param.activityFlags != null) {
            intent.addFlags(param.activityFlags);
        }

        context.startActivity(intent);
    }
}
