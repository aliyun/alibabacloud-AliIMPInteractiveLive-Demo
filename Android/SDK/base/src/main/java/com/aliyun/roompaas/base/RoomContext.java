package com.aliyun.roompaas.base;

import android.content.Context;

import com.alibaba.dingpaas.room.RoomDetail;
import com.aliyun.roompaas.base.inner.InnerService;

/**
 * @author puke
 * @version 2021/6/22
 */
public interface RoomContext {

    String getUserId();

    String getRoomId();

    Context getContext();

    PluginManager getPluginManager();

    RoomDetail getRoomDetail();

    boolean isOwner(String userId);

    <IS extends InnerService> IS getInnerService(Class<IS> innerServiceType);
}
