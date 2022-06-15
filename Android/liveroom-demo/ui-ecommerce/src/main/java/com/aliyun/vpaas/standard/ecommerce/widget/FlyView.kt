package com.aliyun.vpaas.standard.ecommerce.widget

import android.content.Context
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.DynamicDrawableSpan
import android.text.style.ImageSpan
import android.util.AttributeSet
import android.widget.TextView
import com.aliyun.roompaas.uibase.helper.RecyclerViewHelper
import com.aliyun.roompaas.uibase.util.AppUtil
import com.aliyun.standard.liveroom.lib.LimitSizeRecyclerView
import com.aliyun.standard.liveroom.lib.widget.CustomItemAnimator
import com.aliyun.vpaas.standard.ecommerce.R
import com.aliyun.vpaas.standard.ecommerce.model.FlyItem
import com.aliyun.vpaas.standard.ecommerce.span.CenterImageSpan
import java.util.*

/**
 * @author puke
 * @version 2021/9/27
 */
class FlyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LimitSizeRecyclerView(context, attrs, defStyleAttr) {

    private val queue: MutableList<FlyItem> = ArrayList()
    private val recyclerViewHelper: RecyclerViewHelper<FlyItem>
    private val disappearTask: Runnable
    private val showTask: Runnable
    private var isRunning = false

    init {
        setMaxHeight(resources.getDimensionPixelOffset(R.dimen.live_message_fly_height))
        recyclerViewHelper = RecyclerViewHelper.of(
            this, R.layout.live_message_fly_item
        ) { holder, model, position, itemCount ->
            val itemView = holder.getView<TextView>(R.id.item_content)
            val icon = model.icon
            itemView.text = if (icon == null) {
                model.content
            } else {
                SpannableStringBuilder()
                    .append("#", CenterImageSpan(context, icon), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    .append(" ")
                    .append(model.content)
            }
            itemView.setBackgroundResource(model.background ?: 0)
        }
        val recyclerView = recyclerViewHelper.recyclerView
        val animator = CustomItemAnimator()
        animator.addDuration = FLY_IN_DURATION.toLong()
        animator.removeDuration = FLY_OUT_DURATION.toLong()
        recyclerView.itemAnimator = animator

        disappearTask = Runnable {
            if (recyclerViewHelper.itemCount > 0) {
                recyclerViewHelper.removeData(0)
            }
        }

        showTask = object : Runnable {
            override fun run() {
                if (!isRunning || queue.isEmpty()) {
                    isRunning = false
                    removeCallbacks(disappearTask)
                    postDelayed(disappearTask, STAY_DURATION.toLong())
                    return
                }
                val item = queue.removeAt(0)
                disappearTask.run()
                recyclerViewHelper.addData(listOf(item))
                removeCallbacks(this)
                postDelayed(this, SHOW_MESSAGE_INTERVAL.toLong())
            }
        }
    }

    fun addItem(item: FlyItem) {
        if (queue.size > MAX_QUEUE_SIZE) {
            return
        }
        queue.add(item)
        if (!isRunning) {
            isRunning = true
            showTask.run()
        }
    }

    companion object {
        private val TAG = FlyView::class.java.simpleName

        // 划入时长
        private const val FLY_IN_DURATION = 700

        // 划出时长
        private const val FLY_OUT_DURATION = 300

        // 无新消息时, 当前消息停留时长
        private const val STAY_DURATION = 2000

        // 消息队列最大长度
        private const val MAX_QUEUE_SIZE = 10

        // 两条消息之间的间隔
        private const val SHOW_MESSAGE_INTERVAL = FLY_IN_DURATION + FLY_IN_DURATION
    }
}