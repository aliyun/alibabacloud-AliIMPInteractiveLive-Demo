package com.aliyun.roompaas.biz.exposable;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.alibaba.dingpaas.sceneclass.CreateClassRsp;
import com.alibaba.dingpaas.sceneclass.GetClassDetailRsp;
import com.alibaba.dingpaas.sceneclass.StartClassRsp;
import com.alibaba.dingpaas.sceneclass.StopClassRsp;
import com.aliyun.roompaas.base.exposable.Callback;

import java.io.Serializable;

/**
 * Created by KyleCe on 2021/10/28
 */
public interface RoomSceneClass extends Serializable {

    /**
     * @param title          // 可选，为空会自动创建标题
     * @param createNickname // 可选，创建者昵称
     */
    void createClass(@Nullable String title, @Nullable String createNickname, @Nullable Callback<CreateClassRsp> ck);

    void getClassDetail(@NonNull String classId, @Nullable Callback<GetClassDetailRsp> ck);

    void startClass(@NonNull String classId, @Nullable Callback<StartClassRsp> ck);

    void stopClass(@NonNull String classId, @Nullable Callback<StopClassRsp> ck);
}
