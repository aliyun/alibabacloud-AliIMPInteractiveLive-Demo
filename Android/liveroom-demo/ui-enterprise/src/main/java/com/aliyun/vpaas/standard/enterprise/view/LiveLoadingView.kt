package com.aliyun.vpaas.standard.enterprise.view

import android.content.Context
import android.support.v4.widget.ContentLoadingProgressBar
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
import com.aliyun.standard.liveroom.lib.component.view.LiveLoadingView
import com.aliyun.vpaas.standard.enterprise.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author puke
 * @version 2022/3/21
 */
class LiveLoadingView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
    ComponentHolder {

    companion object {
        const val ACTION_SHOW_LOADING = "show_loading"
        const val ACTION_HIDE_LOADING = "hide_loading"
    }

    protected val loadingBar: ContentLoadingProgressBar
    private val component: Component = Component()
    private val supportLoadingView = LivePrototype.getInstance().openLiveParam.supportLoadingView

    init {
        visibility = GONE
        inflate(context, R.layout.ep_view_live_loading, this)
        loadingBar = findViewById(R.id.loading_bar)
    }

    private fun showLoading() {
        if (supportLoadingView) {
            visibility = VISIBLE
        }
    }

    private fun hideLoading() {
        visibility = GONE
    }

    override fun getComponent(): IComponent {
        return component
    }

    private inner class Component : BaseComponent() {
        override fun onInit(liveContext: LiveContext) {
            super.onInit(liveContext)
            if (!supportLoadingView) {
                return
            }
            liveService.addEventHandler(object : SampleLiveEventHandler() {
                override fun onLiveStarted(event: LiveCommonEvent?) {
                    // 开始直播时, 显示加载框 (刚进入的时候没开播, 在页面内部时开播了)
                    showLoading()
                }

                override fun onLoadingBegin() {
                    showLoading()
                }

                override fun onLoadingEnd() {
                    hideLoading()
                }

                override fun onPrepared() {
                    hideLoading()
                }

                override fun onPlayerError(errorInfo: ErrorInfo?) {
                    hideLoading()
                }
            })
        }

        override fun onEvent(action: String?, vararg args: Any?) {
            when (action) {
                ACTION_SHOW_LOADING -> showLoading()
                ACTION_HIDE_LOADING -> hideLoading()
                Actions.GET_LIVE_DETAIL_SUCCESS -> {
                    liveService?.liveDetail?.liveInfo?.let {
                        val status = liveService?.liveDetail?.liveInfo?.status
                        if (status == LiveStateView.STATUS_LIVING) {
                            // 直播正在进行 (刚进入的时候已经开播了)
                            showLoading()
                        }
                    }
                }
            }
        }
    }
}