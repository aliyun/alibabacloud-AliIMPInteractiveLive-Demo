package com.aliyun.vpaas.standard.ecommerce.util

import com.aliyun.vpaas.standard.ecommerce.R
import kotlin.random.Random

/**
 * @author puke
 * @version 2022/5/19
 */
enum class UserLevel(val desc: String) {

    NEW("新粉"),
    FE("铁粉"),
    OLD("老粉"),
    ;

    companion object {
        fun getLevel(userId: String?): UserLevel {
            return userId?.run {
                val values = values()
                values[Random(userId.hashCode()).nextInt(values.size)]
            } ?: NEW
        }

        fun getIcon(level: UserLevel): Int {
            return when (level) {
                FE -> {
                    R.drawable.icon_fan_fe
                }
                OLD -> {
                    R.drawable.icon_fan_old
                }
                else -> {
                    R.drawable.icon_fan_new
                }
            }
        }

        fun getBackground(level: UserLevel): Int {
            return when (level) {
                FE -> {
                    R.drawable.bg_fly_fan_fe
                }
                OLD -> {
                    R.drawable.bg_fly_fan_old
                }
                else -> {
                    R.drawable.bg_fly_fan_new
                }
            }
        }
    }
}