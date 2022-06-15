package com.aliyun.vpaas.standard.ecommerce.util

import android.graphics.Color
import com.aliyun.vpaas.standard.ecommerce.model.Message
import kotlin.random.Random

/**
 * @author puke
 * @version 2022/5/17
 */
class MessagesUtil {

    companion object {
        // 展示+1的消息
        private val PLUS_ONE_MESSAGES = setOf(
            "真好",
            "赞",
            "666",
            "你好",
            "太棒了",
        )

        // 弹幕消息颜色
        private val COMMENT_COLORS = listOf(
            Color.parseColor("#FF5722"),
            Color.parseColor("#00B4FF"),
            Color.parseColor("#FF225D"),
            Color.parseColor("#FFC422"),
            Color.parseColor("#C0EB16"),
            Color.parseColor("#24CD66"),
        )

        // 系统消息背景
        private val SYSTEM_MESSAGE_BACKGROUNDS = listOf(
            Color.parseColor("#FF5722"),
            Color.parseColor("#00B4FF"),
            Color.parseColor("#FF225D"),
            Color.parseColor("#FFC422"),
            Color.parseColor("#C0EB16"),
            Color.parseColor("#24CD66"),
        )

        fun canPlusOne(message: Message): Boolean {
            // 命中关键字, 更改消息类型
            return message.content in PLUS_ONE_MESSAGES
        }

        fun getTextColor(userId: String): Int {
            return COMMENT_COLORS[Random(userId.hashCode()).nextInt(COMMENT_COLORS.size)]
        }
    }
}