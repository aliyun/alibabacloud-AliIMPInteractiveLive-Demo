package com.aliyun.vpaas.standard.ecommerce.model

import com.aliyun.standard.liveroom.lib.MessageModel
import com.aliyun.vpaas.standard.ecommerce.util.MessagesUtil
import com.aliyun.vpaas.standard.ecommerce.util.UserLevel

/**
 * 拓展的消息数据类型
 *
 * @author puke
 * @version 2022/5/17
 */
class Message(userId: String?, type: String?, content: String?) :
    MessageModel(userId, type, content) {

    companion object {
        const val MESSAGE_TYPE_TEXT = "text"
        const val MESSAGE_TYPE_FOLLOW = "follow"
        const val MESSAGE_TYPE_PLUS_ONE = "plusOne"
    }

    /**
     * 用户等级 (模拟)
     */
    val level: UserLevel
        get() = UserLevel.getLevel(userId)

    /**
     * 消息类型
     */
    var messageType: String = MESSAGE_TYPE_TEXT
}