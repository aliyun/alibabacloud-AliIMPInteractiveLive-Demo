package com.aliyun.roompaas.biz.exposable;

import com.alibaba.dingpaas.room.RoomDetail;
import com.alibaba.dingpaas.room.RoomUserModel;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.base.exposable.IEventHandlerManager;
import com.aliyun.roompaas.base.exposable.PluginService;
import com.aliyun.roompaas.base.model.PageModel;
import com.aliyun.roompaas.biz.exposable.model.UserParam;

import java.util.Map;

/**
 * @author puke
 * @version 2021/4/28
 */
public interface RoomChannel extends IEventHandlerManager<RoomEventHandler> {

    /**
     * @return 用户Id
     */
    String getUserId();

    /**
     * @return 房间Id
     */
    String getRoomId();

    /**
     * @return 房间详情信息
     */
    RoomDetail getRoomDetail();

    /**
     * @return 判断当前用户是否是房主
     */
    boolean isOwner();

    /**
     * @return 判断目标用户是否是房主
     */
    boolean isOwner(String userId);

    /**
     * @return 判断当前用户是否是管理员
     */
    boolean isAdmin();

    /**
     * @return 判断目标用户是否是管理员
     */
    boolean isAdmin(String userId);

    /**
     * 进入房间
     *
     * @param nick     用户昵称 (必填参数)
     * @param callback 回调函数
     */
    void enterRoom(String nick, Callback<Void> callback);

    /**
     * 进入房间
     *
     * @param nick     用户昵称
     * @param callback 回调函数
     */
    void enterRoom(String nick, Map<String, String> extension, Callback<Void> callback);

    /**
     * 查询在线用户列表
     *
     * @param param    查询参数
     * @param callback 回调函数
     */
    void listUser(UserParam param, Callback<PageModel<RoomUserModel>> callback);

    /**
     * 踢人
     *
     * @param userId 目标用户Id
     */
    void kickUser(String userId, Callback<Void> callback);

    /**
     * 踢人
     *  @param userId       目标用户Id
     * @param kickedSeconds 踢人时间 (单位: 秒)*/
    void kickUser(String userId, int kickedSeconds, Callback<Void> callback);

    /**
     * 修改标题
     *
     * @param title    标题
     * @param callback 回调函数
     */
    void updateTitle(String title, Callback<Void> callback);

    /**
     * 修改公告
     *
     * @param notice   公告
     * @param callback 回调函数
     */
    void updateNotice(String notice, Callback<Void> callback);

    /**
     * 查询详情
     *
     * @param callback 回调函数
     */
    void getRoomDetail(Callback<RoomDetail> callback);

    /**
     * 离开房间
     *
     * @param callback 回调函数
     */
    void leaveRoom(Callback<Void> callback);

    /**
     * 离开房间
     *
     * @param existPage 是否离开页面
     * @param callback  回调函数
     */
    void leaveRoom(boolean existPage, Callback<Void> callback);

    /**
     * @param <PS>              具象的插件类
     * @param pluginServiceType 插件服务类型
     * @return 插件服务实例
     */
    <PS extends PluginService<?>> PS getPluginService(Class<PS> pluginServiceType);
}
