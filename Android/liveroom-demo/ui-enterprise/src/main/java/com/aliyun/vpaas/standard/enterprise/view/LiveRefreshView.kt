package com.aliyun.vpaas.standard.enterprise.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.aliyun.player.bean.ErrorInfo
import com.aliyun.roompaas.live.SampleLiveEventHandler
import com.aliyun.roompaas.live.exposable.event.LiveCommonEvent
import com.aliyun.standard.liveroom.lib.Actions
import com.aliyun.standard.liveroom.lib.LiveContext
import com.aliyun.standard.liveroom.lib.LivePrototype
import com.aliyun.standard.liveroom.lib.component.BaseComponent
import com.aliyun.standard.liveroom.lib.component.ComponentHolder
import com.aliyun.standard.liveroom.lib.component.IComponent
import com.aliyun.vpaas.standard.enterprise.R

/**
 * @author puke
 * @version 2022/3/21
 */
class LiveRefreshView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
    ComponentHolder {

    private val component: Component = Component()

    private var liveStarted = false

    init {
        visibility = GONE
        inflate(context, R.layout.ep_view_live_refresh, this)

        setOnClickListener {
            component.handleRefreshClick()
        }
    }

    private fun showRefresh() {
        visibility = VISIBLE
    }

    private fun hideRefresh() {
        visibility = GONE
    }

    override fun getComponent(): IComponent {
        return component
    }

    private inner class Component : BaseComponent() {
        override fun onInit(liveContext: LiveContext) {
            super.onInit(liveContext)
            liveService.addEventHandler(object : SampleLiveEventHandler() {

                override fun onLiveStarted(event: LiveCommonEvent?) {
                    liveStarted = true
                }

                override fun onRenderStart() {
                    hideRefresh()
                }

                override fun onPrepared() {
                    hideRefresh()
                }

                override fun onPlayerError(errorInfo: ErrorInfo?) {
                    if (liveStarted) {
                        showRefresh()
                    }
                }
            })
        }

        fun handleRefreshClick() {
            postEvent(LiveLoadingView.ACTION_SHOW_LOADING)
            hideRefresh()
            playerService.refreshPlay()
        }

        override fun onEvent(action: String?, vararg args: Any?) {
            when (action) {
                Actions.GET_LIVE_DETAIL_SUCCESS -> {
                    // 请求到直播详情后, 取出直播状态
                    liveStarted = liveService?.liveDetail?.liveInfo?.status ?: 0 > 0
                }
            }
        }
    }
}