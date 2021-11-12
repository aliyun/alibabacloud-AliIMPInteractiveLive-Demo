import { FC, useState } from 'react'
import { Input, Checkbox, message } from 'antd'
import { RoomModelState, StatusModelState, ChatModelState, UserModelState, connect, Dispatch } from 'umi'
import styles from './index.less'
import teacher from '@/pages/teacher'

const { TextArea } = Input

interface PageProps {
  room: RoomModelState
  status: StatusModelState
  chat: ChatModelState
  user: UserModelState
  dispatch: Dispatch
  from: 'student' | 'teacher'
}

const Chat: FC<PageProps> = ({ room, status, chat, user, dispatch, from }) => {
  const [messageValue, setMessageValue] = useState('')
  const sendComment = async () => {
    if (!messageValue) return
    const msg = messageValue.trim()
    if (!msg) return
    try {
      await window.chatService.sendComment(msg)
      setMessageValue('')
      const messageItem = {
        name: user.nick,
        content: msg,
        isMe: true,
        isOwner: room.ownerId === user.userId,
      }
      dispatch({
        type: 'chat/addMsg',
        payload: messageItem,
      })
    } catch (err) {
      message.error('发送失败')
    }
  }
  const textAreaKeyupHandler = (e: any) => {
    if (e.keyCode !== 13) return
    e.preventDefault()
    sendComment()
  }
  const muteHandler = (e: any) => {
    if (status.isMuteAll) {
      window.chatService.cancelBanAllComment().then(() => {
        dispatch({
          type: 'status/setIsMuteAll',
          payload: false,
        })
      })
    } else {
      window.chatService.banAllComment().then(() => {
        dispatch({
          type: 'status/setIsMuteAll',
          payload: true,
        })
      })
    }
  }
  return (
    <div className={styles['chat-container']} id="chat-container">
      <div className={styles['chat-main']}>
        {chat.messages.map((item, index) => (
          <div className={styles['chat-item']} key={index}>
            <div className={styles.name}>
              {item.name}
              {(item.isOwner || item.isMe) && (
                <span>
                  {item.isOwner ? '教师' : ''}
                  {item.isMe ? '(自己)' : ''}
                </span>
              )}
            </div>
            <div className={styles.content}>{item.content}</div>
          </div>
        ))}
      </div>
      <div className={styles['chat-textarea']}>
        {from === 'teacher' && (
          <div className={styles['operation-bar']}>
            <div className={styles['operation-item']}>
              <Checkbox onChange={muteHandler} className={styles.checkbox} checked={status.isMuteAll}>
                全员禁言
              </Checkbox>
            </div>
          </div>
        )}
        <TextArea
          className={styles.textarea}
          autoSize
          placeholder={
            (!status.isMuteAll && !status.isMuteSelf) || from === 'teacher'
              ? '说点什么吧～'
              : `${status.isMuteAll ? '老师已开启全员禁言' : '您已被禁言'}`
          }
          disabled={(status.isMuteAll || status.isMuteSelf) && from === 'student'}
          onKeyDown={textAreaKeyupHandler}
          value={messageValue}
          onChange={(e) => setMessageValue(e.target.value)}
        ></TextArea>
        <div className={styles['send-btn']} onClick={sendComment}>
          发送
        </div>
      </div>
    </div>
  )
}

export default connect(
  ({
    room,
    status,
    chat,
    user,
  }: {
    room: RoomModelState
    status: StatusModelState
    chat: ChatModelState
    user: UserModelState
  }) => ({
    room,
    status,
    chat,
    user,
  }),
)(Chat)
