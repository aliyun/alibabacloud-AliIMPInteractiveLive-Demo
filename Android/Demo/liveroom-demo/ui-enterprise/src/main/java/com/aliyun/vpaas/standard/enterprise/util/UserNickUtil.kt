package com.aliyun.vpaas.standard.enterprise.util

/**
 * @author puke
 * @version 2022/5/11
 */
class UserNickUtil {

    companion object {
        /**
         * 用户昵称脱敏处理
         */
        fun handleUserNick(userNick: String?): String {
            userNick?.run {
                return when (length) {
                    0, 1 -> {
                        this
                    }
                    else -> {
                        "${this[0]}***${this[length - 1]}"
                    }
                }
            }
            return ""
        }
    }
}