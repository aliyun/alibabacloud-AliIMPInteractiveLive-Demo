package com.aliyun.vpaas.standard.enterprise.view

import android.app.Dialog
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.aliyun.roompaas.base.util.CommonUtil
import com.aliyun.roompaas.live.SampleLiveEventHandler
import com.aliyun.roompaas.live.exposable.event.LiveCommonEvent
import com.aliyun.roompaas.uibase.util.AppUtil
import com.aliyun.roompaas.uibase.util.BottomSheetDialogUtil
import com.aliyun.standard.liveroom.lib.Actions
import com.aliyun.standard.liveroom.lib.LiveContext
import com.aliyun.standard.liveroom.lib.component.BaseComponent
import com.aliyun.standard.liveroom.lib.component.ComponentHolder
import com.aliyun.standard.liveroom.lib.component.IComponent
import com.aliyun.vpaas.standard.enterprise.R


/**
 * 底部: 直播分享组件
 *
 * @author puke
 * @version 2022/5/9
 */
class LiveShareView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
    ComponentHolder {

    private val component = Component()

    private var dialog: Dialog? = null
    private var liveStarted = false

    init {
        setBackgroundResource(R.drawable.ep_icon_share_selector)
        setOnClickListener {
            if (dialog == null) {
                dialog =
                    BottomSheetDialogUtil.create(context, R.layout.ep_layout_share_panel).apply {
                        findViewById<View>(R.id.share_panel)?.setOnClickListener {
                            CommonUtil.showToast(context, "Demo示例不支持分享哦~")
                        }
                        findViewById<View>(R.id.share_cancel)?.setOnClickListener {
                            dismiss()
                        }
                    }
            }
            dialog?.show()
        }
    }

    private inner class Component : BaseComponent() {
        override fun onInit(liveContext: LiveContext?) {
            super.onInit(liveContext)
            liveService.addEventHandler(object : SampleLiveEventHandler() {
                override fun onLiveStarted(event: LiveCommonEvent?) {
                    liveStarted = true
                    isEnabled = true
                }
            })
        }

        override fun onEvent(action: String?, vararg args: Any?) {
            when (action) {
                Actions.GET_LIVE_DETAIL_SUCCESS -> {
                    liveStarted = liveService?.liveDetail?.liveInfo?.status ?: 0 > 0
                    if (!liveStarted) {
                        isEnabled = false
                    }
                }
                LiveBodyLayout.ACTION_SHOW_CHAT_TAB -> {
                    // 切换到互动Tab样式 (互动Tab下, 根据直播是否开启决定点击状态)
                    isEnabled = liveStarted
                }
                LiveBodyLayout.ACTION_SHOW_LIVE_TAB -> {
                    // 切换到直播Tab样式 (直播Tab下, 一直可点击)
                    isEnabled = true
                }
            }
        }
    }

    override fun getComponent(): IComponent {
        return component
    }
}