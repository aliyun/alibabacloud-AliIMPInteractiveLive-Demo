package com.aliyun.vpaas.standard.enterprise.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.aliyun.roompaas.base.util.Utils
import com.aliyun.roompaas.chat.SampleChatEventHandler
import com.aliyun.roompaas.chat.exposable.event.CommentEvent
import com.aliyun.roompaas.chat.exposable.event.LikeEvent
import com.aliyun.roompaas.live.SampleLiveEventHandler
import com.aliyun.roompaas.live.exposable.event.LiveCommonEvent
import com.aliyun.roompaas.uibase.util.AppUtil
import com.aliyun.roompaas.uibase.util.ViewUtil
import com.aliyun.standard.liveroom.lib.Actions
import com.aliyun.standard.liveroom.lib.LiveContext
import com.aliyun.standard.liveroom.lib.component.BaseComponent
import com.aliyun.standard.liveroom.lib.component.ComponentHolder
import com.aliyun.standard.liveroom.lib.component.IComponent
import com.aliyun.vpaas.standard.enterprise.R
import java.lang.ref.WeakReference

/**
 * 底部: 直播点赞组件
 *
 * @author puke
 * @version 2022/5/9
 */
@SuppressLint("ClickableViewAccessibility")
class LiveLikeView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs),
    ComponentHolder, Handler.Callback {

    private val component = Component()
    private val likeIcon: ImageView
    private val likeCount: TextView
    private var currentValue: Int = 0

    private var likeDrawables: Array<Drawable?>? = null
    private var longPressBegin = false
    private var longPressLikeSent = false
    private val handler: Handler = getHandler()

    init {
        clipChildren = false
        inflate(context, R.layout.ep_live_like_view, this)
        likeIcon = findViewById(R.id.like_icon)
        likeCount = findViewById(R.id.like_count)

        setOnClickListener { onLike() }
        setOnLongClickListener {
            longPressBegin = true
            handler.sendEmptyMessageDelayed(MSG_LIKE, ANIMATION_INTERVAL)
            true
        }
        setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
                longPressBegin = false
                longPressLikeSent = false
            }
            false
        }
    }

    override fun getHandler(): Handler {
        return Handler(Looper.getMainLooper(), this)
    }

    private fun onLike() {
        animView()
        if (longPressBegin) {
            if (longPressLikeSent) {
                return
            }
            longPressLikeSent = true
        }
        component.sendLike()
    }

    private fun setLikeCount(count: Int?) {
        count?.run {
            if (count > currentValue) {
                likeCount.visibility = VISIBLE
                likeCount.text = "$this"
                currentValue = count
            }
        }
    }

    fun animView() {
        val iv = ImageView(context)
        iv.setImageDrawable(randomLikeDrawable())
        addView(iv, LayoutParams(
            AppUtil.dp(36f),
            AppUtil.dp(36f)
        ).apply {
            gravity = Gravity.CENTER_HORIZONTAL
            topMargin = AppUtil.dp(12f)
        })
        val sw = AppUtil.getScreenWidth()
        val sh = AppUtil.getScreenHeight()
        val ratioX = Utils.random(5, 0) / 10f
        val ratioY = Utils.random(9, 3) / 10f
        val positionXPoint = x / sw * 10
        val score4LR = Utils.random(10, 1)
        val factorXLR = if (score4LR > positionXPoint) 1 else -1
        val weakRefIV = WeakReference(iv)
        iv.animate()
            .setDuration(1600)
            .scaleX(2f).scaleY(2f)
            .translationX(factorXLR * ratioX * sw).translationY(-ratioY * sh)
            .alpha(.5f)
            .withEndAction {
                val v = Utils.getRef(weakRefIV)
                if (v != null) {
                    ViewUtil.removeSelfSafely(v)
                    Utils.clear(v)
                }
            }
    }

    private fun ofLikeDrawable(): Array<Drawable?> {
        if (likeDrawables == null) {
            likeDrawables = arrayOfNulls(LIKE_RES_ID_ARRAY.size)
            for (i in LIKE_RES_ID_ARRAY.indices) {
                likeDrawables!![i] = AppUtil.getDrawable(
                    LIKE_RES_ID_ARRAY[i]
                )
            }
        }
        return likeDrawables!!
    }

    private fun randomLikeDrawable(): Drawable? {
        return ofLikeDrawable()[Utils.random(LIKE_RES_ID_ARRAY.size - 1, 0)]
    }

    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
            MSG_LIKE -> {
                if (!longPressBegin) {
                    Utils.clear(handler)
                    return false
                }
                onLike()
                handler.sendEmptyMessageDelayed(MSG_LIKE, 50)
            }
            else -> {
            }
        }
        return false
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        likeDrawables = null
        Utils.clear(handler)
    }

    private inner class Component : BaseComponent() {
        override fun onInit(liveContext: LiveContext?) {
            super.onInit(liveContext)
            chatService.addEventHandler(object : SampleChatEventHandler() {
                override fun onLikeReceived(event: LikeEvent?) {
                    // 收到点赞消息后
                    event?.likeCount?.run {
                        // 执行动画
                        if (this > currentValue) {
                            val addedLikeCount = this - currentValue
                            for (i in 0 until addedLikeCount) {
                                handler.postDelayed({ animView() }, i * ANIMATION_INTERVAL)
                            }
                        }

                        // 更改点赞数
                        setLikeCount(this)
                    }
                }
            })
            liveService.addEventHandler(object : SampleLiveEventHandler() {
                override fun onLiveStarted(event: LiveCommonEvent?) {
                    isEnabled = true
                    likeIcon.isEnabled = true
                }
            })
        }

        fun sendLike() {
            chatService.sendLike()
            // 点击立刻+1
            setLikeCount(currentValue + 1)
        }

        override fun onEvent(action: String?, vararg args: Any?) {
            when (action) {
                Actions.GET_CHAT_DETAIL_SUCCESS -> {
                    chatService?.chatDetail?.likeCount?.run { setLikeCount(this) }
                }
                Actions.GET_LIVE_DETAIL_SUCCESS -> {
                    val liveNotStarted = liveService?.liveDetail?.liveInfo?.status ?: 0 == 0
                    if (liveNotStarted) {
                        isEnabled = false
                        likeCount.visibility = GONE
                        likeIcon.isEnabled = false
                    }
                }
            }
        }

        override fun onActivityDestroy() {
            super.onActivityDestroy()
            handler.removeCallbacksAndMessages(null)
        }
    }

    override fun getComponent(): IComponent {
        return component
    }

    companion object {
        private const val MSG_LIKE = 0
        private const val ANIMATION_INTERVAL = 50L
        private val LIKE_RES_ID_ARRAY = intArrayOf(
            com.aliyun.standard.liveroom.lib.R.drawable.ilr_icon_like_clicked_0,
            com.aliyun.standard.liveroom.lib.R.drawable.ilr_icon_like_clicked_1,
            com.aliyun.standard.liveroom.lib.R.drawable.ilr_icon_like_clicked_2,
            com.aliyun.standard.liveroom.lib.R.drawable.ilr_icon_like_clicked_3,
            com.aliyun.standard.liveroom.lib.R.drawable.ilr_icon_like_clicked_4
        )
    }
}