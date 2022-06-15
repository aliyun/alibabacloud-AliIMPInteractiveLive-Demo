package com.aliyun.vpaas.standard.enterprise.helper

import android.os.Handler
import android.os.Looper
import com.aliyun.roompaas.base.log.Logger
import com.aliyun.standard.liveroom.lib.MessageModel
import java.util.*

/**
 * @author puke
 * @version 2022/5/19
 */
class MessageHelper {

    private val TAG = MessageHelper::class.java.simpleName
    private val HANDLER = Handler(Looper.getMainLooper())

    // 消息队列
    protected val bufferQueue: MutableList<MessageModel> = ArrayList()

    // 最大消息总数
    private val DEFAULT_MAX_SIZE_FOR_TOTAL = 2000

    // 达到最大消息数时, 建议移除的消息比例
    private val SUGGEST_REMOVE_RATE_WHEN_LIMIT = 1 / 3f

    private var maxSizeForTotal = DEFAULT_MAX_SIZE_FOR_TOTAL

    private var callback: Callback? = null
    private var isRunning = false

    interface Callback {
        val totalSize: Int

        fun onMessageAdded(message: MessageModel)
        fun onMessageRemoved(suggestRemoveCount: Int)
    }

    private val consumeTask: Runnable = object : Runnable {
        override fun run() {
            if (!isRunning) {
                return
            }
            if (bufferQueue.isEmpty()) {
                isRunning = false
            } else {
                val message: MessageModel = bufferQueue.removeAt(0)
                if (callback != null) {
                    callback!!.onMessageAdded(message)
                }
                val intervalMs = getAdjustMessageInterval()
                if (intervalMs == 0) {
                    run()
                } else {
                    HANDLER.postDelayed(this, intervalMs.toLong())
                }
            }
        }
    }

    fun setMaxSizeForTotal(maxSizeForTotal: Int): MessageHelper {
        this.maxSizeForTotal = maxSizeForTotal
        return this
    }

    fun setCallback(callback: Callback?): MessageHelper {
        this.callback = callback
        return this
    }

    fun addMessage(message: MessageModel) {
        bufferQueue.add(message)

        // 超出最大值时, 做移除操作
        removeIfLimit()
        if (!isRunning) {
            isRunning = true
            consumeTask.run()
        }
    }

    /**
     * 获取动态自适应的消息间隔
     */
    protected fun getAdjustMessageInterval(): Int {
        val bufferSize = bufferQueue.size
        return if (bufferSize <= 2) {
            400
        } else if (bufferSize <= 5) {
            200
        } else if (bufferSize <= 10) {
            100
        } else if (bufferSize <= 20) {
            50
        } else if (bufferSize <= 100) {
            10
        } else if (bufferSize <= 200) {
            50
        } else {
            0
        }
    }

    protected fun removeIfLimit() {
        if (callback != null) {
            val totalSize = callback!!.totalSize
            if (totalSize > maxSizeForTotal) {
                val suggestRemoveCount = (maxSizeForTotal * SUGGEST_REMOVE_RATE_WHEN_LIMIT).toInt()
                Logger.i(
                    TAG, String.format(
                        "Current message size is %s, remove count is %s",
                        totalSize, suggestRemoveCount
                    )
                )
                callback!!.onMessageRemoved(suggestRemoveCount)
            }
        }
    }

    fun destroy() {
        isRunning = false
        HANDLER.removeCallbacks(consumeTask)
    }
}