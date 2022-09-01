package com.aliyun.vpaas.standard.enterprise.view

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.widget.RelativeLayout
import android.widget.TextView
import com.alibaba.dingpaas.chat.CommentModel
import com.alibaba.dingpaas.room.RoomDetail
import com.alibaba.fastjson.JSON
import com.aliyun.roompaas.base.exposable.Callback
import com.aliyun.roompaas.base.log.Logger
import com.aliyun.roompaas.base.model.PageModel
import com.aliyun.roompaas.base.util.CollectionUtil
import com.aliyun.roompaas.biz.SampleRoomEventHandler
import com.aliyun.roompaas.biz.exposable.event.KickUserEvent
import com.aliyun.roompaas.chat.CommentSortType
import com.aliyun.roompaas.chat.SampleChatEventHandler
import com.aliyun.roompaas.chat.exposable.CommentParam
import com.aliyun.roompaas.chat.exposable.event.CommentEvent
import com.aliyun.roompaas.chat.exposable.event.MuteAllCommentEvent
import com.aliyun.roompaas.chat.exposable.event.MuteCommentEvent
import com.aliyun.roompaas.roombase.Const
import com.aliyun.roompaas.uibase.helper.RecyclerViewHelper
import com.aliyun.roompaas.uibase.util.AppUtil
import com.aliyun.standard.liveroom.lib.*
import com.aliyun.standard.liveroom.lib.component.BaseComponent
import com.aliyun.standard.liveroom.lib.component.ComponentHolder
import com.aliyun.standard.liveroom.lib.component.IComponent
import com.aliyun.vpaas.standard.enterprise.R
import com.aliyun.vpaas.standard.enterprise.helper.MessageHelper
import java.util.*

/**
 * 左下角: 直播信息组件
 *
 * @author puke
 * @version 2021/7/29
 */
open class LiveMessageView constructor(
    context: Context?,
    attrs: AttributeSet? = null,
) :
    RelativeLayout(context, attrs), ComponentHolder {

    companion object {
        private val TAG = LiveMessageView::class.java.simpleName
        private const val NICK_SHOW_MAX_LENGTH = 15
        protected fun truncateNick(nick: String): String {
            if (!TextUtils.isEmpty(nick) && nick.length > NICK_SHOW_MAX_LENGTH) {
                return nick.substring(0, NICK_SHOW_MAX_LENGTH)
            }
            return nick
        }
    }

    protected val recyclerView: LimitSizeRecyclerView
    private val messageHelper: MessageHelper?
    private val component: Component = Component()
    private val layoutManager: LinearLayoutManager
    private val recyclerViewHelper: RecyclerViewHelper<MessageModel>?
    private val commentMaxHeight = AppUtil.getScreenHeight() / 3
    private var lastPosition = 0
    private var forceHover = false

    init {
        inflate(context, R.layout.ep_live_message_view, this)
        recyclerView = findViewById(R.id.message_recycler_view)

        // 弹幕面板
        recyclerView.setMaxHeight(commentMaxHeight)
        layoutManager = LinearLayoutManager(context)
        layoutManager.stackFromEnd = true
        recyclerView.layoutManager = layoutManager
        recyclerViewHelper = RecyclerViewHelper.of(
            recyclerView, R.layout.ep_live_message_item_landscape
        ) { holder, model, position, itemCount ->
            // 弹幕内容
            holder.getView<TextView>(R.id.item_content)?.run {
                setTextColor(model.contentColor)
                if (TextUtils.isEmpty(model.type)) {
                    text = model.content
                } else {
                    val prefix = " ${model.type} "
                    val postfix = model.content

                    text = SpannableStringBuilder()
                        .append(
                            prefix,
                            ForegroundColorSpan(model.color),
                            Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                        )
                        .append(postfix)
                }
            }
        }

        // 维度消息控制逻辑
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                refreshUnreadTips()
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                forceHover = false
                refreshUnreadTips()
            }
        })

        // 消息控制辅助类
        messageHelper = MessageHelper()
            .setCallback(object : MessageHelper.Callback {
                override val totalSize: Int
                    get() = recyclerViewHelper.itemCount

                override fun onMessageAdded(message: MessageModel) {
                    addMessageToPanel(listOf(message))
                }

                override fun onMessageRemoved(suggestRemoveCount: Int) {
                    lastPosition -= suggestRemoveCount
                    if (forceHover) {
                        postDelayed({ forceHover = true }, 10)
                    }
                    recyclerViewHelper.removeDataWithoutAnimation(0, suggestRemoveCount)
                }
            })
    }

    /**
     * @return 是否开启未读提示条逻辑
     */
    protected fun enableUnreadTipsLogic(): Boolean {
        return true
    }

    /**
     * @return 是否开启系统消息显示逻辑
     */
    protected fun enableSystemLogic(): Boolean {
        return true
    }

    private fun refreshUnreadTips() {
        if (!enableUnreadTipsLogic()) {
            // 未开启未读提示条逻辑时, 不做额外处理
            return
        }
        val itemCount = recyclerViewHelper!!.itemCount
        val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
        lastPosition = if (lastPosition >= itemCount) {
            lastVisibleItemPosition
        } else {
            Math.max(lastVisibleItemPosition, lastPosition)
        }
        if (forceHover || lastPosition >= 0 && lastPosition < itemCount - 1) {
            // 一旦悬停, 就要等到列表滚动后, 才能解除悬停状态
            forceHover = true
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightMeasureSpec = heightMeasureSpec
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val maxMessageHeight = AppUtil.getScreenHeight() / 3
        if (height > maxMessageHeight) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(
                maxMessageHeight, MeasureSpec.getMode(heightMeasureSpec)
            )
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    protected fun addMessage(message: MessageModel) {
        messageHelper?.addMessage(message)
    }

    /**
     * 弹幕信息添加到面板
     *
     * @param addedList 弹幕信息
     */
    protected open fun addMessageToPanel(addedList: List<MessageModel>) {
        val isLastCompletelyVisible = (layoutManager.findLastVisibleItemPosition()
                == recyclerViewHelper!!.itemCount - 1)
        recyclerViewHelper.addData(addedList)
        if (!forceHover && isLastCompletelyVisible) {
            // 已触底时, 随消息联动
            layoutManager.scrollToPositionWithOffset(
                recyclerViewHelper.itemCount - 1, Int.MIN_VALUE
            )
            postDelayed({ recyclerView.invalidate() }, 100)
            lastPosition = 0
        } else {
            refreshUnreadTips()
        }
    }

    /**
     * @return 首条系统消息 (返回null时, 不展示)
     */
    protected val systemAlertMessage: MessageModel
        get() {
            val systemMessage = MessageModel(
                null, LiveConst.SYSTEM_NOTICE_NICKNAME, LiveConst.SYSTEM_NOTICE_ALERT
            )
            systemMessage.contentColor = Color.parseColor("#12DBE6")
            return systemMessage
        }

    override fun getComponent(): IComponent {
        return component
    }

    inner class Component : BaseComponent() {
        override fun onInit(liveContext: LiveContext) {
            super.onInit(liveContext)

            // 回放不展示信息面板
            visibility = if (needPlayback()) GONE else VISIBLE

            // 监听房间事件
            roomChannel.addEventHandler(object : SampleRoomEventHandler() {
                override fun onRoomUserKicked(event: KickUserEvent) {
                    if (!TextUtils.equals(roomChannel.userId, event.kickUser)) {
                        // 其他人, 移除列表, 面板提示
                        showToast("${truncateNick(event.kickUserName)}被管理员移除房间")
                    }
                }
            })

            // 监听互动事件
            chatService.addEventHandler(object : SampleChatEventHandler() {
                override fun onCommentReceived(event: CommentEvent) {
                    val senderId = event.creatorOpenId
                    if (TextUtils.equals(senderId, Const.getCurrentUserId())) {
                        // 自己发送的消息不做上屏显示
                        return
                    }
                    val nick = truncateNick(event.creatorNick)
                    addMessage(MessageModel(senderId, nick, event.content))
                }

                override fun onCommentMutedOrCancel(event: MuteCommentEvent) {
                    // 禁言 & 取消禁言
                    val action = if (event.mute) "禁言" else "取消禁言"
                    val isSelf = TextUtils.equals(roomChannel.userId, event.muteUserOpenId)
                    val subject = if (isSelf) "您" else truncateNick(event.muteUserNick)
                    showToast(String.format("%s被管理员%s了", subject, action))
                }

                override fun onCommentAllMutedOrCancel(event: MuteAllCommentEvent) {
                    // 全体禁言 & 取消全体禁言
                    val action = if (event.mute) "开启了全体禁言" else "取消了全体禁言"
                    showToast(String.format("管理员%s", action))
                }
            })
        }

        override fun onEnterRoomSuccess(roomDetail: RoomDetail) {
            super.onEnterRoomSuccess(roomDetail)

            // 禁用、切换用户或回放时, 不需要加载弹幕信息
            if (openLiveParam.loadHistoryComment
                && !liveContext.isSwitchUser
                && !needPlayback()
            ) {
                loadComment()
            }
        }

        private fun loadComment() {
            val commentParam = CommentParam()
            commentParam.pageNum = 1
            commentParam.pageSize = 100
            commentParam.sortType = CommentSortType.DESC_BY_TIME
            chatService.listComment(commentParam, object : Callback<PageModel<CommentModel>?> {
                override fun onSuccess(pageModel: PageModel<CommentModel>?) {
                    if (pageModel != null) {
                        val list = pageModel.list
                        if (CollectionUtil.isNotEmpty(list)) {
                            // 记录插入前的索引值
                            val addedList: MutableList<MessageModel> = ArrayList()
                            // 倒序取的
                            for (model in list.reversed()) {
                                val nick = truncateNick(model.creatorNick)
                                addedList.add(MessageModel(model.creatorId, nick, model.content))
                            }
                            addMessageToPanel(addedList)
                        }
                    }
                }

                override fun onError(errorMsg: String) {
                    showToast("获取弹幕列表失败: $errorMsg")
                }
            })
        }

        override fun onEvent(action: String, vararg args: Any) {
            if (Actions.SHOW_MESSAGE == action) {
                if (args.isNotEmpty()) {
                    val arg = args[0]
                    val message = arg as? MessageModel
                    message?.run {
                        // 判断是否忽略弹幕频率限制
                        val ignoreFreqLimit = args.size > 1 && java.lang.Boolean.TRUE == args[1]
                        if (ignoreFreqLimit) {
                            // 忽略限流控制时, 直接上屏
                            addMessageToPanel(listOf(message))
                        } else {
                            // 默认是交给消息流控
                            addMessage(message)
                        }
                    }
                } else {
                    Logger.w(TAG, "Received invalid message param: " + JSON.toJSONString(args))
                }
            }
        }

        override fun onActivityDestroy() {
            messageHelper?.destroy()
        }
    }
}