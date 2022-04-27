package com.aliyun.liveroom.demo.linkmic;

import com.aliyun.standard.liveroom.lib.linkmic.model.LinkMicUserModel;

import java.util.List;

/**
 * 连麦视图接口
 *
 * @author puke
 * @version 2022/1/20
 */
public interface IMicRenderContainer {

    void add(List<LinkMicUserModel> users);

    void remove(String userId);

    void removeAll();

    void update(String userId, boolean refreshRenderView);

    LinkMicUserModel getUser(String userId);
}
