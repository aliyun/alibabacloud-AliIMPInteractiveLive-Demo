package com.aliyun.vpaas.standard.enterprise.util

import android.app.Activity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.aliyun.roompaas.uibase.util.AppUtil
import com.aliyun.vpaas.standard.enterprise.R
import com.aliyun.vpaas.standard.enterprise.view.LiveGiftView

/**
 * 礼物动画类
 *
 * @author puke
 * @version 2022/5/19
 */
class GiftAnimationUtil {

    companion object {

        /**
         * 礼物向上飞出
         */
        fun showAnimation(activity: Activity, type: String?) {
            val gift = when (type) {
                LiveGiftView.SEND_GIFT_TYPE_FLOWER -> {
                    R.drawable.icon_flower
                }
                LiveGiftView.SEND_GIFT_TYPE_BOAT -> {
                    R.drawable.icon_boat
                }
                LiveGiftView.SEND_GIFT_TYPE_PLANE -> {
                    R.drawable.icon_plane
                }
                LiveGiftView.SEND_GIFT_TYPE_ROCKET -> {
                    R.drawable.icon_rocket
                }
                else -> {
                    null
                }
            }
            gift?.run {
                val image = ImageView(activity).apply {
                    this.alpha = 0f
                    setImageResource(gift)
                }
                val layout = FrameLayout(activity).apply {
                    addView(
                        image, FrameLayout.LayoutParams(
                            AppUtil.dp(100f),
                            AppUtil.dp(100f)
                        )
                    )
                }
                activity.addContentView(
                    layout, FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                )

                image.post {
                    val screenWidth = AppUtil.getScreenWidth()
                    val screenHeight = AppUtil.getScreenHeight()
                    image.x = ((screenWidth - image.width) / 2).toFloat()
                    image.y = screenHeight.toFloat()

                    // 1. 从底部到中点
                    image.animate()
                        .setDuration(500)
                        .withStartAction { image.alpha = 1f }
                        .withEndAction {
                            // 2. 放大
                            image.animate()
                                .setDuration(500)
                                .withEndAction {
                                    // 3. 缩小
                                    image.animate()
                                        .setDuration(500)
                                        .withEndAction {
                                            // 4. 从中点到顶部
                                            image.animate()
                                                .setDuration(500)
                                                .withEndAction {
                                                    layout.parent?.run {
                                                        if (this is ViewGroup) {
                                                            this.removeView(layout)
                                                        }
                                                    }
                                                }
                                                .y(-image.height.toFloat())
                                                .scaleX(1f)
                                                .scaleY(1f)
                                                .start()
                                        }
                                        .scaleX(1.7f)
                                        .scaleY(1.7f)
                                        .start()
                                }
                                .scaleX(3f)
                                .scaleY(3f)
                                .start()
                        }
                        .y((screenHeight - image.height) / 2f)
                        .scaleX(1.7f)
                        .scaleY(1.7f)
                        .start()
                }
            }
        }
    }
}