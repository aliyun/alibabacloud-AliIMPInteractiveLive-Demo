package com.aliyun.roompaas.chat.exposable;

import com.alibaba.dingpaas.chat.CommentModel;
import com.alibaba.dingpaas.chat.GetTopicInfoRsp;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.base.exposable.PluginService;
import com.aliyun.roompaas.base.model.PageModel;

import java.util.HashMap;
import java.util.List;

/**
 * @author puke
 * @version 2021/6/21
 */
public interface ChatService extends PluginService<ChatEventHandler> {

    /**
     * 查询互动详情
     */
    GetTopicInfoRsp getChatDetail();

    /**
     * 查询互动详情
     *
     * @param callback 回调函数
     */
    void getChatDetail(Callback<GetTopicInfoRsp> callback);

    /**
     * 发送弹幕
     *
     * @param content  弹幕内容
     * @param callback 回调函数
     */
    void sendComment(String content, Callback<String> callback);


    /**
     * 发送弹幕
     * @param content 弹幕内容
     * @param extension 扩展字段
     * @param callback  回调函数
     */
    void sendComment(String content, HashMap<String, String> extension, Callback<String> callback);

    /**
     * 发送自定义消息
     *
     * @param body     消息体
     * @param callback 回调函数
     */
    void sendCustomMessageToAll(String body, Callback<String> callback);

    /**
     * 发送自定义消息给指定用户
     *
     * @param body     消息体
     * @param users    接收者Id列表
     * @param callback 回调函数
     */
    void sendCustomMessageToUsers(String body, List<String> users, Callback<String> callback);

    /**
     * 禁止发弹幕
     *
     * @param userId      目标用户
     * @param muteSeconds 禁言时间 (单位: 秒)
     * @param callback    回调函数
     */
    void banComment(String userId, int muteSeconds, Callback<Void> callback);

    /**
     * 取消禁止发弹幕
     *
     * @param userId   目标用户
     * @param callback 回调函数
     */
    void cancelBanComment(String userId, Callback<Void> callback);

    /**
     * 全体禁止发弹幕
     *
     * @param callback 回调函数
     */
    void banAllComment(Callback<Void> callback);

    /**
     * 取消全体禁止发弹幕
     *
     * @param callback 回调函数
     */
    void cancelBanAllComment(Callback<Void> callback);

    /**
     * 点赞 (内部会做合流处理)
     */
    void sendLike();

    /**
     * 查询弹幕列表
     *
     * @param param    查询弹幕参数
     * @param callback 回调函数
     */
    void listComment(CommentParam param, Callback<PageModel<CommentModel>> callback);
}
