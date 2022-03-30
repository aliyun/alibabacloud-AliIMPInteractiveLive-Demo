package com.aliyun.liveroom.demo;

import com.aliyun.liveroom.demo.custom.CustomLiveBeautyView;
import com.aliyun.liveroom.demo.custom.CustomLiveEmptyView;
import com.aliyun.liveroom.demo.custom.CustomLiveGoodsCardView;
import com.aliyun.liveroom.demo.custom.CustomLiveInfoView;
import com.aliyun.liveroom.demo.custom.CustomLiveInputView;
import com.aliyun.liveroom.demo.custom.CustomLiveLikeView;
import com.aliyun.liveroom.demo.custom.CustomLiveMessageView;
import com.aliyun.liveroom.demo.custom.CustomLiveMiddleView;
import com.aliyun.liveroom.demo.custom.CustomLiveMoreView;
import com.aliyun.liveroom.demo.custom.CustomLiveRenderView;
import com.aliyun.liveroom.demo.custom.CustomLiveRightUpperView;
import com.aliyun.liveroom.demo.custom.CustomLiveShareView;
import com.aliyun.liveroom.demo.custom.CustomLiveStartView;
import com.aliyun.liveroom.demo.custom.CustomLiveStopView;
import com.aliyun.liveroom.demo.linkmic.CustomLiveLinkMicView;
import com.aliyun.standard.liveroom.lib.LiveHook;
import com.aliyun.standard.liveroom.lib.LivePrototype;
import com.aliyun.standard.liveroom.lib.component.view.LiveBeautyView;
import com.aliyun.standard.liveroom.lib.component.view.LiveCurtainView;
import com.aliyun.standard.liveroom.lib.component.view.LiveGestureView;
import com.aliyun.standard.liveroom.lib.component.view.LiveInfoView;
import com.aliyun.standard.liveroom.lib.component.view.LiveInputView;
import com.aliyun.standard.liveroom.lib.component.view.LiveLikeView;
import com.aliyun.standard.liveroom.lib.component.view.LiveMessageView;
import com.aliyun.standard.liveroom.lib.component.view.LiveMoreView;
import com.aliyun.standard.liveroom.lib.component.view.LiveRenderView;
import com.aliyun.standard.liveroom.lib.component.view.LiveShareView;
import com.aliyun.standard.liveroom.lib.component.view.LiveStopView;

/**
 * 设置直播间UI
 *
 * @author puke
 * @version 2021/12/13
 */
public class LiveHooker {

    /**
     * 设置直播间默认样式, 即不做任何额外UI定制
     */
    public static void setDefaultStyle() {
        LivePrototype.getInstance().setLiveHook(null);
    }

    /**
     * 设置直播间自定义样式, 根据业务诉求做额外定制
     */
    public static void setCustomStyle() {
        LivePrototype.getInstance().setLiveHook(new LiveHook()
                        // 自定义主播的启播页视图
                        .setReadySlot(context -> new CustomLiveStartView(context, null))
                        // 自定义右上角视图 (信息视图 和 停止直播 之间的空间)
                        .setUpperRightSlot(context -> new CustomLiveRightUpperView(context, null))
                        // 自定义商品卡片视图
                        .setGoodsSlot(context -> new CustomLiveGoodsCardView(context, null))
                        // 自定义腰部视图
                        .setMiddleSlot(context -> new CustomLiveMiddleView(context, null))
                        // 自定义左上角的直播信息视图
                        .replaceComponentView(LiveInfoView.class, CustomLiveInfoView.class)
                        // 自定义右上角的直播停止视图
                        .replaceComponentView(LiveStopView.class, CustomLiveStopView.class)
                        // 自定义左下角直播信息面板视图
                        .replaceComponentView(LiveMessageView.class, CustomLiveMessageView.class)
                        // 自定义页面底部输入框视图
                        .replaceComponentView(LiveInputView.class, CustomLiveInputView.class)
                        // 自定义页面底部分享视图
                        .replaceComponentView(LiveShareView.class, CustomLiveShareView.class)
                        // 自定义页面底部点赞视图
                        .replaceComponentView(LiveLikeView.class, CustomLiveLikeView.class)
                        // 自定义页面底部美颜视图
                        .replaceComponentView(LiveBeautyView.class, CustomLiveBeautyView.class)
                        // 自定义页面底部更多视图
                        .replaceComponentView(LiveMoreView.class, CustomLiveMoreView.class)
                        // 自定义页面渲染视图
                        .replaceComponentView(LiveRenderView.class, CustomLiveRenderView.class)
                // 如果还不能满足自定义的诉求, 参考如下方式来替换整个xml布局文件, 支持任意维度自定义
                // 找到SDK中直播间默认的布局文件 ilr_activity_live.xml, 然后复制一份重命名为 activity_custom_live.xml, 在复制的文件里直接修改
                // .setLiveLayoutRes(R.layout.activity_custom_live)
        );
    }

    /**
     * 设置直播间连麦样式, 目前仅支持观众端
     */
    public static void setLinkMicStyle() {
        LivePrototype.getInstance().setLiveHook(new LiveHook()
                .replaceComponentView(LiveMessageView.class, CustomLiveEmptyView.class)
                .replaceComponentView(LiveCurtainView.class, CustomLiveEmptyView.class)
                .replaceComponentView(LiveGestureView.class, CustomLiveEmptyView.class)
                // 普通直播 => 连麦直播
                .replaceComponentView(LiveRenderView.class, CustomLiveLinkMicView.class)
        );
    }
}
