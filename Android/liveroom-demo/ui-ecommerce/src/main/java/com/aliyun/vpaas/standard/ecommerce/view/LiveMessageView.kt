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
 * ?????????: ??????????????????
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

        // ????????????
        recyclerView.setMaxHeight(commentMaxHeight)
        layoutManager = LinearLayoutManager(context)
        layoutManager.stackFromEnd = true
        recyclerView.layoutManager = layoutManager
        recyclerViewHelper = RecyclerViewHelper.of(
            recyclerView, R.layout.live_message_item
        ) { holder, model, position, itemCount ->
            // ????????????
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

            // ??????Action
            holder.getView<TextView>(R.id.item_action)?.run {
                when (model.messageType) {
                    Message.MESSAGE_TYPE_FOLLOW -> {
                        if (component.isOwner || isFollow) {
                            // ???????????????, ????????????, ????????????"????????????"
                            visibility = GONE
                        } else {
                            // ?????????
                            visibility = VISIBLE
                            text = "????????????"
                            setOnClickListener { component.doFollow() }
                        }
                    }
                    Message.MESSAGE_TYPE_PLUS_ONE -> {
                        if (component.isOwner) {
                            // ??????????????????+1
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

        // ????????????????????????
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

        // ?????????????????????
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
     * @return ?????????????????????????????????
     */
    protected fun enableUnreadTipsLogic(): Boolean {
        return true
    }

    /**
     * @return ????????????????????????????????????
     */
    protected fun enableSystemLogic(): Boolean {
        return true
    }

    private fun refreshUnreadTips() {
        if (!enableUnreadTipsLogic()) {
            // ?????????????????????????????????, ??????????????????
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
            // ????????????, ???????????????????????????, ????????????????????????
            forceHover = true
            unreadTips.visibility = VISIBLE
            val showCount = lastPosition + 1
            val unreadCount = itemCount - showCount
            unreadTips.text = String.format("%s????????????", unreadCount)
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
            // ??????
            maxMessageHeight = AppUtil.getScreenHeight() / 3
            // ?????????????????????
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(
                AppUtil.getScreenWidth() / 2, MeasureSpec.getMode(widthMeasureSpec)
            )
        } else {
            // ??????
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
     * ???????????????????????????
     *
     * @param addedList ????????????
     */
    protected open fun addMessageToPanel(addedList: List<Message>) {
        addedList
            .filter { it.messageType == Message.MESSAGE_TYPE_TEXT && MessagesUtil.canPlusOne(it) }
            .forEach { it.messageType = Message.MESSAGE_TYPE_PLUS_ONE }

        val isLastCompletelyVisible = (layoutManager.findLastVisibleItemPosition()
                == recyclerViewHelper!!.itemCount - 1)
        recyclerViewHelper.addData(addedList)
        if (!forceHover && isLastCompletelyVisible) {
            // ????????????, ???????????????
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
     * @return ?????????????????? (??????null???, ?????????)
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
            addSystemMessage(event.userId, "${subject}??????")
        }
    }

    override fun getComponent(): IComponent {
        return component
    }

    inner class Component : BaseComponent() {
        override fun onInit(liveContext: LiveContext) {
            super.onInit(liveContext)

            // ???????????????????????????
            visibility = if (needPlayback()) GONE else VISIBLE

            // ??????????????????
            roomChannel.addEventHandler(object : SampleRoomEventHandler() {
                override fun onEnterOrLeaveRoom(event: RoomInOutEvent) {
                    this@LiveMessageView.onEnterOrLeaveRoom(event)
                }

                override fun onRoomUserKicked(event: KickUserEvent) {
                    if (!TextUtils.equals(roomChannel.userId, event.kickUser)) {
                        // ?????????, ????????????, ????????????
                        showToast("${truncateNick(event.kickUserName)}????????????????????????")
                    }
                }
            })

            // ??????????????????
            chatService.addEventHandler(object : SampleChatEventHandler() {
                override fun onCommentReceived(event: CommentEvent) {
                    val senderId = event.creatorOpenId
                    if (TextUtils.equals(senderId, Const.getCurrentUserId())) {
                        // ???????????????????????????????????????
                        return
                    }
                    val nick = truncateNick(event.creatorNick)
                    addMessage(Message(senderId, nick, event.content))
                }

                override fun onCommentMutedOrCancel(event: MuteCommentEvent) {
                    // ?????? & ????????????
                    val action = if (event.mute) "??????" else "????????????"
                    val isSelf = TextUtils.equals(roomChannel.userId, event.muteUserOpenId)
                    val subject = if (isSelf) "???" else truncateNick(event.muteUserNick)
                    showToast(String.format("%s????????????%s???", subject, action))
                }

                override fun onCommentAllMutedOrCancel(event: MuteAllCommentEvent) {
                    // ???????????? & ??????????????????
                    val action = if (event.mute) "?????????????????????" else "?????????????????????"
                    showToast(String.format("?????????%s", action))
                }

                override fun onCustomMessageReceived(event: CustomMessageEvent?) {
                    // ?????????????????????
                    CustomMessages.parseMessage(event?.data)?.run {
                        when (this) {
                            is ToBuyMessage -> {
                                // ?????????
                                val handledUserNick = UserNickUtil.handleUserNick(userNick)
                                FlyItem().apply {
                                    content = "${handledUserNick}????????????"
                                    icon = R.drawable.icon_to_buying
                                    background = R.drawable.bg_fly_to_buying
                                    addSystemMessage(this)
                                }
                            }
                            is SendGiftMessage -> {
                                // ?????????
                                val handledUserNick = UserNickUtil.handleUserNick(userNick)
                                GiftAnimationUtil.showAnimation(activity, type)
                                showToast("${handledUserNick}????????????????????????~~")
                            }
                            is FollowMessage -> {
                                // ??????
                                component.renderFollow(this)
                            }
                            else -> {
                            }
                        }
                    }
                }
            })

            // ??????????????????
            liveService.addEventHandler(object : SampleLiveEventHandler() {
                override fun onLiveStarted(event: LiveCommonEvent) {
                    if (!isOwner) {
                        // showToast("???????????????")
                    }
                }

                override fun onLiveStopped(event: LiveCommonEvent) {
                    if (!isOwner) {
                        // showToast("???????????????")
                    }
                }
            })
        }

        override fun onEnterRoomSuccess(roomDetail: RoomDetail) {
            super.onEnterRoomSuccess(roomDetail)

            // ?????????????????????????????????, ???????????????????????????
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
                            // ???????????????????????????
                            val addedList: MutableList<Message> = ArrayList()
                            // ????????????
                            for (model in list.reversed()) {
                                val nick = truncateNick(model.creatorNick)
                                addedList.add(Message(model.creatorId, nick, model.content))
                            }
                            addMessageToPanel(addedList)
                        }
                    }
                }

                override fun onError(errorMsg: String) {
                    showToast("????????????????????????: $errorMsg")
                }
            })
        }

        override fun onEvent(action: String, vararg args: Any) {
            if (Actions.SHOW_MESSAGE == action) {
                if (args.isNotEmpty()) {
                    val arg = args[0]
                    val message: Message?
                    if (arg is MessageModel) {
                        // ??????SDK??????MessageModel
                        message = Message(arg.userId, arg.type, arg.content)
                    } else if (arg is Message) {
                        // ??????LiveInfo??????????????????
                        message = arg
                    } else {
                        message = null
                    }

                    message?.run {
                        // ????????????????????????????????????
                        val ignoreFreqLimit = args.size > 1 && java.lang.Boolean.TRUE == args[1]
                        if (ignoreFreqLimit) {
                            // ?????????????????????, ????????????
                            addMessageToPanel(listOf(message))
                        } else {
                            // ???????????????????????????
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
            // ?????????
            chatService.sendComment(content, null)
            // ??????
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
                // ????????? => ?????? ???, ??????Item
                val needRefreshFollowState = !isFollow
                // ?????????????????????, ???????????????
                isFollow = true

                if (needRefreshFollowState) {
                    // ??????????????????, ??????"????????????"??????
                    recyclerViewHelper?.recyclerView?.adapter?.notifyDataSetChanged()
                }
            }
            val content = "?????????${if (isOwner) "???" else "??????"}"
            val fromNick = followMessage.userNick
            val userId = Const.getCurrentUserId()
            // ????????????
            addSystemMessage(userId, "${fromNick}${content}")
            // ????????????
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