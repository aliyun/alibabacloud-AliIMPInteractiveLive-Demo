package com.aliyun.roompaas.app;


import android.support.annotation.StringDef;

import com.aliyun.roompaas.app.helper.RoomHelper;
import com.aliyun.roompaas.app.sensitive.SensitiveDomain;
import com.aliyun.roompaas.base.util.CommonUtil;

/**
 * @author puke
 * @version 2021/5/13
 */
public abstract class Const extends SensitiveDomain {

    @StringDef({BIZ_TYPE.BUSINESS, BIZ_TYPE.CLASSROOM})
    public @interface BIZ_TYPE {
        String BUSINESS = "business";
        String CLASSROOM = "classroom";
        String DEFAULT = BUSINESS;
    }

    public static String getAppId() {
        return RoomHelper.isTypeBusiness() ? APP_ID_FOR_BUSINESS : APP_ID_FOR_CLASSROOM;
    }

    public static String getAppKey() {
        return RoomHelper.isTypeBusiness() ? APP_KEY_FOR_BUSINESS : APP_KEY_FOR_CLASSROOM;
    }

    public static String getServerHost(){
        return  RoomHelper.isTypeBusiness() ? SERVER_HOST_BUSINESS : SERVER_HOST_CLASSROOM;
    }

    public static String getServerSecret(){
        return  RoomHelper.isTypeBusiness() ? SERVER_SECRET_BUSINESS : SERVER_SECRET_CLASSROOM;
    }

    /**
     * 设备Id (注: SDK初始化和getLoginToken时保持一致)
     */
    public static final String DEVICE_ID = CommonUtil.getDeviceId();

    /**
     * 当前登录用户Id (仅Demo使用, 接入时替换为App内部的userId)
     */
    public static String currentUserId;
}
