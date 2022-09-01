package com.aliyun.vpaas.standard.enterprise.view

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.AttributeSet
import android.widget.TextView
import com.alibaba.dingpaas.chat.CommentModel
import com.alibaba.dingpaas.room.RoomDetail
import com.alibaba.fastjson.JSON
import com.aliyun.roompaas.base.exposable.Callback
import com.aliyun.roompaas.base.log.Logger
import com.aliyun.roompaas.base.model.PageModel
import com.aliyun.roompaas.base.util.CollectionUtil
import com.aliyun.roompaas.chat.CommentSortType
import com.aliyun.roompaas.chat.SampleChatEventHandler
import com.aliyun.roompaas.chat.exposable.CommentParam
import com.aliyun.roompaas.chat.exposable.event.CommentEvent
import com.aliyun.roompaas.chat.exposable.event.MuteAllCommentEvent
import com.aliyun.roompaas.chat.exposable.event.MuteCommentEvent
import com.aliyun.roompaas.live.SampleLiveEventHandler
import com.aliyun.roompaas.live.exposable.event.LiveCommonEvent
import com.aliyun.roompaas.roombase.Const
import com.aliyun.roompaas.uibase.helper.RecyclerViewHelper
import com.aliyun.roompaas.uibase.util.AppUtil
import com.aliyun.standard.liveroom.lib.Actions
import com.aliyun.standard.liveroom.lib.LiveContext
import com.aliyun.standard.liveroom.lib.MessageModel
import com.aliyun.standard.liveroom.lib.component.BaseComponent
import com.aliyun.standard.liveroom.lib.component.ComponentHolder
import com.aliyun.standard.liveroom.lib.component.IComponent
import com.aliyun.vpaas.standard.enterprise.R
import com.aliyun.vpaas.standard.enterprise.helper.MessageHelper
import java.util.ArrayList

/**
 * 第1项Tab: 互动消息
 *
 * @author puke
 * @version 2022/6/6
 */
class LiveChatView(context: Context, attrs: AttributeSet?) : RecyclerView(context, attrs),
    ComponentHolder {

    private val component = Component()
    private val recyclerViewHelper: RecyclerViewHelper<MessageModel>
    private val messageHelper: MessageHelper

    private var lastPosition = 0
    private var forceHover = false

    init {
        overScrollMode = OVER_SCROLL_NEVER
        layoutManager = LinearLayoutManager(context)
        recyclerViewHelper = RecyclerViewHelper.of(
            this, R.layout.ep_live_message_item
        ) { holder, model, position, itemCount ->
            // 弹幕昵称
            holder.getView<TextView>(R.id.item_nick)?.run {
                text = model.type
            }

            // 弹幕内容
            holder.getView<TextView>(R.id.item_content)?.run {
                text = model.content
            }

            // 设置第一项上外边距
            holder.itemView.run {
                (layoutParams as? MarginLayoutParams)?.run {
                    topMargin = if (position == 0) AppUtil.dp(12f) else 0
                    layoutParams = this
                }
            }
        }

        // 维度消息控制逻辑
        addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                forceHover = false
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

    protected fun addMessage(message: MessageModel) {
        messageHelper.addMessage(message)
    }

    /**
     * 弹幕信息添加到面板
     *
     * @param addedList 弹幕信息
     */
    private fun addMessageToPanel(addedList: List<MessageModel>) {
        val layoutManager = layoutManager as LinearLayoutManager
        val isLastCompletelyVisible = (layoutManager.findLastVisibleItemPosition()
                == recyclerViewHelper.itemCount - 1)
        recyclerViewHelper.addData(addedList)
        if (!forceHover && isLastCompletelyVisible) {
            // 已触底时, 随消息联动
            layoutManager.scrollToPositionWithOffset(
                recyclerViewHelper.itemCount - 1, Int.MIN_VALUE
            )
            postDelayed({ invalidate() }, 100)
            lastPosition = 0
        }
    }

    private inner class Component : BaseComponent() {
        override fun onInit(liveContext: LiveContext) {
            super.onInit(liveContext)

            // 回放不展示信息面板
            visibility = if (needPlayback()) GONE else VISIBLE

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
                    val message: MessageModel?
                    if (arg is MessageModel) {
                        // 兼容SDK中的MessageModel
                        message = MessageModel(arg.userId, arg.type, arg.content)
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
            messageHelper.destroy()
        }
    }

    override fun getComponent(): IComponent {
        return component
    }

    companion object {
        private val TAG = LiveChatView::class.java.simpleName
        private const val NICK_SHOW_MAX_LENGTH = 15
        private fun truncateNick(nick: String): String {
            if (!TextUtils.isEmpty(nick) && nick.length > NICK_SHOW_MAX_LENGTH) {
                return nick.substring(0, NICK_SHOW_MAX_LENGTH)
            }
            return nick
        }
    }
}