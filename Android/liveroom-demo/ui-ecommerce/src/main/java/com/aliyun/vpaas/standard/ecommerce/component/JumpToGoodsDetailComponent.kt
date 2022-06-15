package com.aliyun.vpaas.standard.ecommerce.component

import com.alibaba.fastjson.JSON
import com.aliyun.roompaas.chat.SampleChatEventHandler
import com.aliyun.roompaas.chat.exposable.event.CustomMessageEvent
import com.aliyun.roompaas.uibase.util.DialogUtil
import com.aliyun.standard.liveroom.lib.LiveContext
import com.aliyun.standard.liveroom.lib.component.BaseComponent
import com.aliyun.standard.liveroom.lib.component.view.LiveRenderView
import com.aliyun.standard.liveroom.lib.floatwindow.FloatWindowManager
import com.aliyun.standard.liveroom.lib.floatwindow.FloatWindowPermissionUtil
import com.aliyun.vpaas.standard.ecommerce.custommessage.CustomMessages
import com.aliyun.vpaas.standard.ecommerce.activity.GoodsDetailActivity
import com.aliyun.vpaas.standard.ecommerce.custommessage.ToBuyMessage
import com.aliyun.vpaas.standard.ecommerce.util.UserNickUtil

/**
 * @author puke
 * @version 2022/5/10
 */
class JumpToGoodsDetailComponent : BaseComponent() {

    companion object {
        const val ACTION_JUM_TO_GOODS_DETAIL = "JumpToGoodsDetail"
    }

    override fun onEvent(action: String?, vararg args: Any?) {
        super.onEvent(action, *args)
        if (action == ACTION_JUM_TO_GOODS_DETAIL) {
            if (isOwner) {
                // 主播没有小窗和购买逻辑
                GoodsDetailActivity.open(activity)
                return
            }

            if (FloatWindowPermissionUtil.checkPermission(activity)) {
                // 有悬浮窗权限
                if (!FloatWindowManager.instance().isShowing) {
                    postEvent(LiveRenderView.ACTION_SHOW_FLOAT_WINDOW)
                }
                GoodsDetailActivity.open(activity)
                reportEvent()
            } else {
                // 无悬浮窗权限
                DialogUtil.confirm(activity, "开启悬浮窗权限，自动变小窗",
                    {
                        // 同意
                        FloatWindowPermissionUtil.navToPermissionSettingPage(activity)
                    },
                    {
                        // 拒绝
                        GoodsDetailActivity.open(activity)
                        reportEvent()
                    }
                )
            }
        }
    }

    private fun reportEvent() {
        CustomMessages.doSend(chatService, ToBuyMessage())
    }
}