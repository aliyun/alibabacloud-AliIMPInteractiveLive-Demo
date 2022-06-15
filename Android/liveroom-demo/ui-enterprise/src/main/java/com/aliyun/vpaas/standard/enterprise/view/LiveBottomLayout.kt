package com.aliyun.vpaas.standard.enterprise.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import com.aliyun.roompaas.biz.exposable.enums.LiveStatus
import com.aliyun.roompaas.live.SampleLiveEventHandler
import com.aliyun.roompaas.live.exposable.event.LiveCommonEvent
import com.aliyun.roompaas.uibase.util.AppUtil
import com.aliyun.roompaas.uibase.util.KeyboardHelper
import com.aliyun.roompaas.uibase.util.KeyboardHelper.OnSoftKeyBoardChangeListener
import com.aliyun.standard.liveroom.lib.Actions
import com.aliyun.standard.liveroom.lib.LiveContext
import com.aliyun.standard.liveroom.lib.component.BaseComponent
import com.aliyun.standard.liveroom.lib.component.ComponentHolder
import com.aliyun.standard.liveroom.lib.component.IComponent
import com.aliyun.vpaas.standard.enterprise.R


/**
 * 底部模块
 *
 * @author puke
 * @version 2022/5/9
 */
class LiveBottomLayout(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs),
    ComponentHolder {

    private val component = Component()
    private var liveStarted = false
    private var isLiveTab = false

    init {
        clipChildren = false
        gravity = Gravity.BOTTOM
        orientation = HORIZONTAL
    }

    private fun showChildren(showCondition: (View) -> Boolean) {
        for (i in 0 until childCount) {
            getChildAt(i)?.run {
                visibility = if (showCondition(this)) VISIBLE else GONE
            }
        }
    }

    private inner class Component : BaseComponent() {
        override fun onInit(liveContext: LiveContext?) {
            super.onInit(liveContext)
            initKeyboard()
            liveService.addEventHandler(object : SampleLiveEventHandler() {
                override fun onLiveStarted(event: LiveCommonEvent?) {
                    // 进页面时直播未开播, 浏览过程中直播开播了, 也做隐藏操作
                    liveStarted = true
                    if (isLiveTab) {
                        visibility = GONE
                    }
                }
            })
        }

        private fun initKeyboard() {
            val keyboardHelper = KeyboardHelper(activity)
            keyboardHelper.setOnSoftKeyBoardChangeListener(object : OnSoftKeyBoardChangeListener {
                override fun keyBoardShow(height: Int) {
                    val layoutParams = layoutParams as MarginLayoutParams
                    layoutParams.bottomMargin = AppUtil.dp(12f)
                    setLayoutParams(layoutParams)
                }

                override fun keyBoardHide(height: Int) {
                    val layoutParams = layoutParams as MarginLayoutParams
                    layoutParams.bottomMargin =
                        resources.getDimensionPixelOffset(R.dimen.ep_bottom_layout_margin_bottom)
                    setLayoutParams(layoutParams)
                }
            })
        }

        // 控制子组件显示/隐藏的逻辑较复杂, 由父控件统一处理
        override fun onEvent(action: String?, vararg args: Any?) {
            when (action) {
                Actions.GET_LIVE_DETAIL_SUCCESS -> {
                    // 请求到直播详情后, 取出直播状态
                    liveStarted = liveService?.liveDetail?.liveInfo?.status ?: 0 > 0
                }
                LiveBodyLayout.ACTION_SHOW_CHAT_TAB -> {
                    // 切换到互动Tab样式, 隐藏预约
                    isLiveTab = false
                    visibility = VISIBLE
                    showChildren { it !is LiveSubscribeView }
                    setPadding(0, 0, 0, 0)
                }
                LiveBodyLayout.ACTION_SHOW_LIVE_TAB -> {
                    // 切换到直播Tab样式
                    isLiveTab = true
                    if (liveStarted) {
                        // 直播已开始时, 全隐藏
                        visibility = GONE
                    } else {
                        // 直播未开始时, 只展示分享和预约
                        visibility = VISIBLE
                        showChildren { it is LiveShareView || it is LiveSubscribeView }
                        setPadding(0, AppUtil.dp(8f), 0, 0)
                    }
                }
                LiveBodyLayout.ACTION_SHOW_OTHER_TAB -> {
                    // 切换到其他Tab样式, 全隐藏
                    isLiveTab = false
                    visibility = GONE
                }
            }
        }

        @SuppressLint("SwitchIntDef")
        override fun onActivityConfigurationChanged(newConfig: Configuration?) {
            when (newConfig?.orientation) {
                Configuration.ORIENTATION_PORTRAIT -> {
                    // 竖屏
                    visibility = VISIBLE
                }
                Configuration.ORIENTATION_LANDSCAPE -> {
                    // 横屏
                    visibility = GONE
                }
            }
        }
    }

    override fun getComponent(): IComponent {
        return component
    }
}