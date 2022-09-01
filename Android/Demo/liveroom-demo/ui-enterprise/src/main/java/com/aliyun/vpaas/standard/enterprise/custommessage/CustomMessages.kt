package com.aliyun.vpaas.standard.enterprise.custommessage

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONException
import com.aliyun.roompaas.chat.exposable.ChatService
import com.aliyun.roompaas.roombase.Const
import com.aliyun.standard.liveroom.lib.LivePrototype

/**
 * 该类用于承载自定义消息的逻辑
 *
 * @author puke
 * @version 2022/5/12
 */
class CustomMessages {

    companion object {
        /**
         * 送礼物
         */
        private const val ACTION_SEND_GIFT = "sendGift"

        /**
         * 发送自定义消息
         */
        fun doSend(chatService: ChatService, message: BaseCustomMessage) {
            message.userId = Const.getCurrentUserId()
            message.userNick = LivePrototype.getInstance().openLiveParam.nick
            message.action = when (message) {
                is SendGiftMessage -> ACTION_SEND_GIFT
                else -> return
            }

            val messageBody = JSON.toJSONString(message)
            chatService.sendCustomMessageToAll(messageBody, null)
        }

        /**
         * 解析收到的消息
         */
        fun parseMessage(body: String?): BaseCustomMessage? {
            body?.run {
                try {
                    val json = JSON.parseObject(body)
                    json?.run {
                        getString("action")?.run {
                            val type = when (this) {
                                ACTION_SEND_GIFT -> SendGiftMessage::class.java
                                else -> null
                            }
                            type?.run {
                                return json.toJavaObject(type)
                            }
                        }
                    }
                } catch (e: JSONException) {
                }
            }
            return null
        }
    }
}