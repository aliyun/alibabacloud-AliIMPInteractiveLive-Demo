package com.aliyun.standard.liveroom.lib.util;

import com.alibaba.dingpaas.rtc.ConfUserModel;
import com.aliyun.standard.liveroom.lib.linkmic.model.LinkMicUserModel;

/**
 * @author puke
 * @version 2022/1/12
 */
public class EntityConverter {

    public static LinkMicUserModel confUser2MicUser(ConfUserModel model) {
        if (model == null) {
            return null;
        }

        LinkMicUserModel micUserModel = new LinkMicUserModel();
        micUserModel.userId = model.userId;
        micUserModel.nickname = model.nickname;
        micUserModel.isCameraOpen = model.cameraStatus == 1;
        micUserModel.isMicOpen = model.micphoneStatus == 1;
        return micUserModel;
    }
}
