package com.aliyun.standard.liveroom.lib;

/**
 * @author puke
 * @version 2021/7/30
 */
public interface Actions {

    /* 点击空白区域回调 */
    String EMPTY_SPACE_CLICK = "EmptySpaceClick";

    /* 观众端进入直播间页面后, 添加渲染视图成功 (注: 此时不一定有直播) */
    String TRY_PLAY_LIVE_SUCCESS = "TryPlayLiveSuccess";

    /* 主播端预览调用完成 */
    String PREVIEW_SUCCESS = "PreviewSuccess";

    /* 点击分享按钮回调 */
    String SHARE_CLICKED = "ShareClicked";

    /* 显示商品卡片 */
    String SHOW_GOODS_CARD = "ShowGoodsCard";

    /* 隐藏商品卡片 */
    String HIDE_GOODS_CARD = "HideGoodsCard";

    /* 信息面板添加消息 */
    String SHOW_MESSAGE = "ShowMessage";

    /* 系统消息条添加消息 */
    String SHOW_SYSTEM_MESSAGE = "ShowSystemMessage";

    /*发送评论-输入框点击*/
    String SEND_COMMENT_INPUT_CLICKED = "SEND_COMMENT_INPUT_CLICKED";
    /*发送评论-触发发送评论*/
    String SEND_COMMENT_TRIGGERED = "SEND_COMMENT_TRIGGERED";
    /*发送评论-发送评论成功*/
    String SEND_COMMENT_SUCCESS = "SEND_COMMENT_SUCCESS";
    /*发送评论-发送评论失败*/
    String SEND_COMMENT_FAIL = "SEND_COMMENT_FAIL";

    /* 进入页面后, 查询直播详情成功回调 */
    String GET_LIVE_DETAIL_SUCCESS = "GetLiveDetailSuccess";

    /* 进入页面后, 查询互动详情成功回调 */
    String GET_CHAT_DETAIL_SUCCESS = "GetChatDetailSuccess";

    /* 点击弹幕信息回调 */
    String SHOW_MESSAGE_CLICKED = "ShowMessageClicked";

    /* 长按弹幕信息回调 */
    String SHOW_MESSAGE_LONG_CLICKED = "ShowMessageLongClicked";

    /* 点击美颜回调 */
    String SHOW_BEAUTY_CLICKED = "ShowBeautyClicked";
}
