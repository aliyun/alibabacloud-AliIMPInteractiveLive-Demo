package com.aliyun.vpaas.standard.ecommerce.view

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
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
import com.aliyun.roompaas.biz.exposable.event.RoomInOutEvent
import com.aliyun.roompaas.chat.CommentSortType
import com.aliyun.roompaas.chat.SampleChatEventHandler
import com.aliyun.roompaas.chat.exposable.CommentParam
import com.aliyun.roompaas.chat.exposable.event.CommentEvent
import com.aliyun.roompaas.chat.exposable.event.CustomMessageEvent
import com.aliyun.roompaas.chat.exposable.event.MuteAllCommentEvent
import com.aliyun.roompaas.chat.exposable.event.MuteCommentEvent
import com.aliyun.roompaas.live.SampleLiveEventHandler
import com.aliyun.roompaas.live.exposable.event.LiveCommonEvent
import com.aliyun.roompaas.roombase.Const
import com.aliyun.roompaas.uibase.helper.RecyclerViewHelper
import com.aliyun.roompaas.uibase.util.AppUtil
import com.aliyun.standard.liveroom.lib.*
import com.aliyun.standard.liveroom.lib.component.BaseComponent
import com.aliyun.standard.liveroom.lib.component.ComponentHolder
import com.aliyun.standard.liveroom.lib.component.IComponent
import com.aliyun.vpaas.standard.ecommerce.R
import com.aliyun.vpaas.standard.ecommerce.custommessage.CustomMessages
import com.aliyun.vpaas.standard.ecommerce.custommessage.FollowMessage
import com.aliyun.vpaas.standard.ecommerce.custommessage.SendGiftMessage
import com.aliyun.vpaas.standard.ecommerce.custommessage.ToBuyMessage
import com.aliyun.vpaas.standard.ecommerce.helper.MessageHelper
import com.aliyun.vpaas.standard.ecommerce.model.FlyItem
import com.aliyun.vpaas.standard.ecommerce.model.Message
import com.aliyun.vpaas.standard.ecommerce.span.CenterImageSpan
import com.aliyun.vpaas.standard.ecommerce.util.GiftAnimationUtil
import com.aliyun.vpaas.standard.ecommerce.util.MessagesUtil
import com.aliyun.vpaas.standard.ecommerce.util.UserLevel
import com.aliyun.vpaas.standard.ecommerce.util.UserNickUtil
import com.aliyun.vpaas.standard.ecommerce.widget.FlyView
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

    protected val flyView: FlyView
    protected val recyclerView: LimitSizeRecyclerView
    protected val unreadTips: TextView
    private val messageHelper: MessageHelper?
    private val component: Component = Component()
    private val layoutManager: LinearLayoutManager
    private val recyclerViewHelper: RecyclerViewHelper<Message>?
    private val commentMaxHeight = AppUtil.getScreenHeight() / 3
    private var isSystemAlertMessageAlreadyAdded = false
    private var lastPosition = 0
    private var forceHover = false
    private var isFollow = false

    init {
        inflate(context, R.layout.live_message_view, this)
        flyView = findViewById(R.id.message_fly_view)
        recyclerView = findViewById(R.id.message_recycler_view)
        unreadTips = findViewById(R.id.message_unread_tips)

        // 弹幕面板
        recyclerView.setMaxHeight(commentMaxHeight)
        layoutManager = LinearLayoutManager(context)
        layoutManager.stackFromEnd = true
        recyclerView.layoutManager = layoutManager
        recyclerViewHelper = RecyclerViewHelper.of(
            recyclerView, R.layout.live_message_item
        ) { holder, model, position, itemCount ->
            // 弹幕内容
            holder.getView<TextView>(R.id.item_content)?.run {
                setTextColor(model.contentColor)
                if (TextUtils.isEmpty(model.type)) {
                    text = model.content
                } else {
                    val prefix = " ${model.type} "
                    val postfix = model.content
                    val userNickColor = MessagesUtil.getTextColor(model.userId)
                    val icon = UserLevel.getIcon(model.level)

                    text = SpannableStringBuilder()
                        .append(
                            "#",
                            CenterImageSpan(context!!, icon),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        .append(
                            prefix,
                            ForegroundColorSpan(userNickColor),
                            Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                        )
                        .append(postfix)
                }
            }

            // 弹幕Action
            holder.getView<TextView>(R.id.item_action)?.run {
                when (model.messageType) {
                    Message.MESSAGE_TYPE_FOLLOW -> {
                        if (component.isOwner || isFollow) {
                            // 是主播自己, 或已关注, 均不显示"我也关注"
                            visibility = GONE
                        } else {
                            // 未关注
                            visibility = VISIBLE
                            text = "我也关注"
                            setOnClickListener { component.doFollow() }
                        }
                    }
                    Message.MESSAGE_TYPE_PLUS_ONE -> {
                        if (component.isOwner) {
                            // 主播端不显示+1
                            visibility = GONE
                        } else {
                            visibility = VISIBLE
                            text = "+1"
                            setOnClickListener {
                                component.handlePlusOne(model.content)
                            }
                        }
                    }
                    else -> {
                        // Unknown message type
                        visibility = GONE
                    }
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
        unreadTips.setOnClickListener {
            lastPosition = 0
            forceHover = false
            recyclerView.scrollToPosition(recyclerViewHelper.itemCount - 1)
        }

        // 消息控制辅助类
        messageHelper = MessageHelper()
            .setCallback(object : MessageHelper.Callback {
                override val totalSize: Int
                    get() = recyclerViewHelper.itemCount

                override fun onMessageAdded(message: Message) {
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
            unreadTips.visibility = VISIBLE
            val showCount = lastPosition + 1
            val unreadCount = itemCount - showCount
            unreadTips.text = String.format("%s条新消息", unreadCount)
        } else {
            unreadTips.visibility = GONE
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var widthMeasureSpec = widthMeasureSpec
        var heightMeasureSpec = heightMeasureSpec
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val maxMessageHeight: Int
        if (component.isLandscape) {
            // 横屏
            maxMessageHeight = AppUtil.getScreenHeight() / 3
            // 宽度为屏幕一半
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(
                AppUtil.getScreenWidth() / 2, MeasureSpec.getMode(widthMeasureSpec)
            )
        } else {
            // 竖屏
            val systemMaxHeight =
                if (enableSystemLogic()) resources.getDimensionPixelOffset(R.dimen.live_message_fly_height) + resources.getDimensionPixelOffset(
                    R.dimen.message_fly_bottom_margin
                ) else 0
            maxMessageHeight = commentMaxHeight + systemMaxHeight
        }
        if (height > maxMessageHeight) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(
                maxMessageHeight, MeasureSpec.getMode(heightMeasureSpec)
            )
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    protected fun addSystemMessage(userId: String?, content: String?) {
        FlyItem().run {
            this.content = content
            val level = UserLevel.getLevel(userId)
            this.icon = UserLevel.getIcon(level)
            this.background = UserLevel.getBackground(level)
            addSystemMessage(this)
        }
    }

    protected fun addSystemMessage(flyItem: FlyItem) {
        if (enableSystemLogic()) {
            flyView.addItem(flyItem)
        }
    }

    protected fun addMessage(message: Message) {
        messageHelper?.addMessage(message)
    }

    /**
     * 弹幕信息添加到面板
     *
     * @param addedList 弹幕信息
     */
    protected open fun addMessageToPanel(addedList: List<Message>) {
        addedList
            .filter { it.messageType == Message.MESSAGE_TYPE_TEXT && MessagesUtil.canPlusOne(it) }
            .forEach { it.messageType = Message.MESSAGE_TYPE_PLUS_ONE }

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
    protected val systemAlertMessage: Message
        get() {
            val systemMessage = Message(
                null, LiveConst.SYSTEM_NOTICE_NICKNAME, LiveConst.SYSTEM_NOTICE_ALERT
            )
            systemMessage.contentColor = Color.parseColor("#12DBE6")
            return systemMessage
        }

    protected fun onEnterOrLeaveRoom(event: RoomInOutEvent) {
        if (event.enter) {
            val subject = truncateNick(event.nick)
            addSystemMessage(event.userId, "${subject}来了")
        }
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
                override fun onEnterOrLeaveRoom(event: RoomInOutEvent) {
                    this@LiveMessageView.onEnterOrLeaveRoom(event)
                }

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
                    addMessage(Message(senderId, nick, event.content))
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

                override fun onCustomMessageReceived(event: CustomMessageEvent?) {
                    // 收到自定义消息
                    CustomMessages.parseMessage(event?.data)?.run {
                        when (this) {
                            is ToBuyMessage -> {
                                // 去购买
                                val handledUserNick = UserNickUtil.handleUserNick(userNick)
                                FlyItem().apply {
                                    content = "${handledUserNick}正在去买"
                                    icon = R.drawable.icon_to_buying
                                    background = R.drawable.bg_fly_to_buying
                                    addSystemMessage(this)
                                }
                            }
                            is SendGiftMessage -> {
                                // 送礼物
                                val handledUserNick = UserNickUtil.handleUserNick(userNick)
                                GiftAnimationUtil.showAnimation(activity, type)
                                showToast("${handledUserNick}为主播送来了礼物~~")
                            }
                            is FollowMessage -> {
                                // 关注
                                component.renderFollow(this)
                            }
                            else -> {
                            }
                        }
                    }
                }
            })

            // 监听直播事件
            liveService.addEventHandler(object : SampleLiveEventHandler() {
                override fun onLiveStarted(event: LiveCommonEvent) {
                    if (!isOwner) {
                        // showToast("直播已开始")
                    }
                }

                override fun onLiveStopped(event: LiveCommonEvent) {
                    if (!isOwner) {
                        // showToast("直播已结束")
                    }
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
                        if (!isSystemAlertMessageAlreadyAdded && recyclerViewHelper != null) {
                            recyclerViewHelper.insertCell(0, systemAlertMessage)
                            isSystemAlertMessageAlreadyAdded = true
                        }
                        if (CollectionUtil.isNotEmpty(list)) {
                            // 记录插入前的索引值
                            val addedList: MutableList<Message> = ArrayList()
                            // 倒序取的
                            for (model in list.reversed()) {
                                val nick = truncateNick(model.creatorNick)
                                addedList.add(Message(model.creatorId, nick, model.content))
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
                    val message: Message?
                    if (arg is MessageModel) {
                        // 兼容SDK中的MessageModel
                        message = Message(arg.userId, arg.type, arg.content)
                    } else if (arg is Message) {
                        // 接收LiveInfo中的关注事件
                        message = arg
                    } else {
                        message = null
                    }

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

        fun handlePlusOne(content: String?) {
            // 发消息
            chatService.sendComment(content, null)
            // 上屏
            val message = Message(
                Const.getCurrentUserId(),
                liveContext.nick,
                content
            )
            addMessageToPanel(listOf(message))
        }

        fun doFollow() {
            CustomMessages.doSend(chatService, FollowMessage())
        }

        fun renderFollow(followMessage: FollowMessage) {
            if (followMessage.userId == Const.getCurrentUserId()) {
                // 未关注 => 关注 时, 刷新Item
                val needRefreshFollowState = !isFollow
                // 我关注了主播后, 记录标识位
                isFollow = true

                if (needRefreshFollowState) {
                    // 刷新全部视图, 刷掉"我也关注"标识
                    recyclerViewHelper?.recyclerView?.adapter?.notifyDataSetChanged()
                }
            }
            val content = "关注了${if (isOwner) "你" else "主播"}"
            val fromNick = followMessage.userNick
            val userId = Const.getCurrentUserId()
            // 系统消息
            addSystemMessage(userId, "${fromNick}${content}")
            // 弹幕消息
            val message = Message(userId, fromNick, content).apply {
                messageType = Message.MESSAGE_TYPE_FOLLOW
            }
            addMessageToPanel(listOf(message))
        }
    }

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
}