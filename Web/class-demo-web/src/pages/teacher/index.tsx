import { FC, useEffect, useState, useCallback, useRef } from 'react'
import { RoomModelState, StatusModelState, ChatModelState, UserModelState, ConnectProps, connect, Dispatch } from 'umi'
import { Modal, message } from 'antd'
import { useMount, useUnmount, usePersistFn } from 'ahooks'
import { generateUserList } from '@/models/user'
import Emitter from '@/utils/emitter'
import OperationBar from '@/components/operationBar'
import Chat from '@/components/chat'
import WhiteBoard from '@/components/whiteBoard'
import VideoLayout from '@/components/videoLayout'
import UserList from '@/components/userList'
import OssUploader from '@/components/ossUploader'
import styles from './index.less'

const { EventNameEnum } = window.RoomPaasSdk
const emitter = Emitter.getInstance()

interface PageProps extends ConnectProps {
  room: RoomModelState
  status: StatusModelState
  chat: ChatModelState
  user: UserModelState
  dispatch: Dispatch
}

const Teacher: FC<PageProps> = ({ room, status, chat, user, dispatch, history }) => {
  const [tab, setTab] = useState('chat')
  const [loadFinish, setLoadFinish] = useState(false)
  const switchViewMode = () => {
    if (status.viewMode === 'whiteBoard') {
      dispatch({ type: 'status/setLayout', payload: '6' })
    }
    const mode = status.viewMode === 'whiteBoard' ? 'video' : 'whiteBoard'
    dispatch({ type: 'status/setViewMode', payload: mode })
    // 让学生端切换viewMode
    window.chatService.sendCustomMessageToAll(`classCustom/setViewMode/${mode}`)
    setTimeout(() => {
      emitter.emit('needSetVideo')
    }, 100)
  }
  const reciveCommentHandler = usePersistFn((e: any) => {
    if (user.userId.toString() === e.data.creatorOpenId.toString()) return
    const messageItem = {
      name: e.data.creatorNick,
      content: e.data.content,
      isMe: false,
      isOwner: room.ownerId === e.data.creatorOpenId,
    }
    dispatch({
      type: 'chat/addMsg',
      payload: messageItem,
    })
  })
  const reciveCustomMessageHandler = usePersistFn((e: any) => {
    console.log(e)
  })
  const enterRoomHandler = usePersistFn((e: any) => {
    if (e.data.userId === user.userId) return
    if (e.data.enter) {
      dispatch({
        type: 'user/addUser',
        payload: {
          userId: e.data.userId,
          nick: e.data.nick,
          isOwner: false,
          isMe: false,
          isInSeat: false,
          isApplying: false,
          isRtcMute: false,
          isRtcMuteCamera: false,
        },
      })
    } else {
      dispatch({
        type: 'user/deleteUser',
        payload: e.data.userId,
      })
    }
  })
  const rtcApplyHandler = usePersistFn((d: any) => {
    console.log(user.userList)
    if (d.data.isApply) {
      if (user.userList[d.data.applyUser.userId].isApplying) return
      message.info(`${user.userList[d.data.applyUser.userId].nick}申请连麦`)
    } else {
      message.info(`${user.userList[d.data.applyUser.userId].nick}取消了连麦申请`)
    }
    dispatch({
      type: 'user/updateUser',
      payload: {
        userId: d.data.applyUser.userId,
        isApplying: d.data.isApply,
      },
    })
  })
  const joinSuccessHandler = usePersistFn((d: any) => {
    const userList = d.data.userList
    for (let i = 0; i < userList.length; i++) {
      if (userList[i].userId === room.ownerId) continue
      message.success(`${user.userList[userList[i].userId].nick}连麦成功`)
      dispatch({
        type: 'user/updateUser',
        payload: {
          userId: userList[i].userId,
          isApplying: false,
          isInSeat: true,
          isInviting: false,
          isRtcMuteCamera: userList[i].cameraStatus === 0,
          isRtcMute: userList[i].micphoneStatus === 0,
        },
      })
    }
  })
  const joinFailedHandler = usePersistFn((d: any) => {
    const userList = d.data.userList
    for (let i = 0; i < userList.length; i++) {
      if (userList[i].userId === room.ownerId) continue
      message.info(`${user.userList[userList[i].userId].nick}决定不连麦了`)
      dispatch({
        type: 'user/updateUser',
        payload: {
          userId: userList[i].userId,
          isApplying: false,
          isInSeat: false,
        },
      })
    }
  })
  const leaveChannelHandler = usePersistFn((d: any) => {
    const userList = d.data.userList
    for (let i = 0; i < userList.length; i++) {
      if (userList[i].userId === room.ownerId) continue
      message.info(`${user.userList[userList[i].userId].nick}结束连麦`)
      dispatch({
        type: 'user/updateUser',
        payload: {
          userId: userList[i].userId,
          isApplying: false,
          isInSeat: false,
          isInviting: false,
        },
      })
    }
  })
  const rtcSubscribeHandler = usePersistFn((d: any) => {
    dispatch({
      type: 'user/updateUser',
      payload: {
        userId: d.userId,
        isInSeat: true,
        subscribeResult: true,
        streamType: 1,
      },
    })
    setTimeout(() => {
      emitter.emit('needSetVideo')
    }, 100)
  })
  const rtcLeaveHandler = usePersistFn((d: any) => {
    console.log(d)
    if (d.userId === room.ownerId) return
    dispatch({
      type: 'user/updateUser',
      payload: {
        userId: d.userId,
        isApplying: false,
        isInSeat: false,
        isInviting: false,
      },
    })

    setTimeout(() => {
      emitter.emit('needSetVideo')
    }, 100)
  })
  const rtcMuteHandler = usePersistFn((d: any) => {
    const muteList = d.data.userList
    const status = d.data.open
    for (let i = 0; i < muteList.length; i++) {
      if (muteList[i] === user.userId) return
      dispatch({
        type: 'user/updateUser',
        payload: {
          userId: muteList[i],
          isRtcMute: !status,
        },
      })
    }
  })
  const rtcMuteCameraHandler = usePersistFn((d: any) => {
    const status = d.data.open
    if (d.data.userId === user.userId) return
    dispatch({
      type: 'user/updateUser',
      payload: {
        userId: d.data.userId,
        isRtcMuteCamera: !status,
      },
    })
    setTimeout(() => {
      emitter.emit('needSetVideo')
    }, 100)
  })
  const rtcOnErrorHandler = usePersistFn((d: any) => {
    if (d.errorCode === 10011 || d.errorCode === 10012) {
      window.rtcService.stopPublishScreen().then(() => {
        dispatch({
          type: 'status/setIsScreenSharing',
          payload: false,
        })
        // todo: setLayout
      })
    }
  })
  const docStatusHandler = usePersistFn((d: any) => {
    if (d.data === 'CONVERSION_TASK_STATUS_SUCCESS') {
      message.success('文档转码成功')
      emitter.emit('insertPPT')
    } else {
      message.error('文档转码失败')
    }
    dispatch({
      type: 'status/setIsDocConverting',
      payload: false,
    })
  })
  const bindEvents = () => {
    window.chatService.on(EventNameEnum.PaaSChatReciveComment, reciveCommentHandler)
    window.chatService.on(EventNameEnum.PaaSChatCustomMessage, reciveCustomMessageHandler)
    window.roomChannel.on(EventNameEnum.PaaSRoomEnter, enterRoomHandler)
    window.roomChannel.on(EventNameEnum.PaaSDocStatus, docStatusHandler)
    window.rtcService.on(EventNameEnum.PaaSRtcApply, rtcApplyHandler)
    window.rtcService.on(EventNameEnum.PaaSRtcJoinSuccess, joinSuccessHandler)
    window.rtcService.on(EventNameEnum.PaaSRtcJoinFailed, joinFailedHandler)
    window.rtcService.on(EventNameEnum.PaaSRtcLeaveChannel, leaveChannelHandler)
    window.rtcService.on(EventNameEnum.onSubscribeResult, rtcSubscribeHandler)
    window.rtcService.on(EventNameEnum.PaaSRtcMute, rtcMuteHandler)
    window.rtcService.on(EventNameEnum.PaaSRtcCamera, rtcMuteCameraHandler)
    window.rtcService.on(EventNameEnum.onError, rtcOnErrorHandler)
    window.rtcService.on(EventNameEnum.onLeave, rtcLeaveHandler)
  }
  const initUserList = () => {
    if (status.classStatus === 1) {
      Promise.all([
        window.rtcService.listConfUser(1, 100).then((res: any) => res.userList),
        window.rtcService.listApplyLinkMicUser(1, 50).then((res: any) => res.userList),
        window.roomChannel.listUser(1, 50).then((res: any) => res.userList),
      ]).then((res: any) => {
        const [confUserList, applyList, userList] = res
        const list = generateUserList(userList, room.ownerId, user.userId, applyList, confUserList)
        dispatch({
          type: 'user/setUserList',
          payload: list,
        })
      })
    } else {
      window.roomChannel.listUser(1, 100).then((res: any) => {
        const list = generateUserList(res.userList, room.ownerId, user.userId)
        dispatch({
          type: 'user/setUserList',
          payload: list,
        })
      })
    }
  }
  useEffect(() => {
    if (status.viewMode === 'whiteBoard') {
      dispatch({ type: 'status/setLayout', payload: '6' })
    }
  }, [status.viewMode])
  useMount(() => {
    if (!window.roomEngine) {
      window.sessionStorage.getItem('userId') ? history.replace('/doLogin') : history.replace('/')
      return
    }
    setLoadFinish(true)
    bindEvents()
    window.chatService.listComment(0, 1, 30).then((res: any) => {
      dispatch({
        type: 'chat/addMsg',
        payload: res.commentModelList.reverse().map((item: any) => ({
          name: item.creatorNick,
          content: item.content,
          isOwner: item.creatorId === room.ownerId,
          isMe: item.creatorId === user.userId,
        })),
      })
    })
    setTimeout(() => {
      initUserList()
    }, 3000)
    window.addEventListener(
      'beforeunload',
      () => {
        if (window.roomChannel) {
          window.rtcService.leaveRtc()
          window.roomChannel.leaveRoom()
          window.roomEngine.logout()
        }
      },
      true,
    )
  })
  useUnmount(() => {
    if (window.roomChannel) {
      window.roomChannel.leaveRoom()
      window.rtcService.leaveRtc()
      window.roomEngine.logout()
    }
  })
  return (
    <div className={styles['page-container']}>
      <div className={`${status.viewMode === 'whiteBoard' ? styles['page-main'] : styles.abbreviate}`}>
        <div
          className={styles['abbreviate-container']}
          style={{
            display: status.viewMode === 'whiteBoard' ? 'none' : 'block',
          }}
        >
          <div className={styles['abbr-type']}>课件展示</div>
          <div className={styles['abbr-operation-bar']}>
            <div className={styles['operation-item']} onClick={switchViewMode}>
              <svg className="icon" aria-hidden="true">
                <use xlinkHref="#icon-ic_qiehuanshitu"></use>
              </svg>
            </div>
          </div>
        </div>
        {room.docKey && <WhiteBoard boardType={status.viewMode === 'whiteBoard' ? 'full' : 'pure'} />}
      </div>
      <div className={`${status.viewMode === 'video' ? styles['page-main'] : styles.abbreviate}`}>
        <div
          className={styles['abbreviate-container']}
          style={{ display: status.viewMode === 'video' ? 'none' : 'block' }}
        >
          <div className={styles['abbr-type']}>教师(我)</div>
          <div className={styles['abbr-operation-bar']}>
            <div className={styles['operation-item']} onClick={switchViewMode}>
              <svg className="icon" aria-hidden="true">
                <use xlinkHref="#icon-ic_qiehuanshitu"></use>
              </svg>
            </div>
          </div>
        </div>
        {loadFinish && <VideoLayout />}
      </div>
      <aside className={styles.sidebar}>
        <div className={styles['tab-container']}>
          <div
            className={`${styles['tab-item']} ${tab === 'chat' ? styles['tab-item-active'] : ''}`}
            onClick={() => setTab('chat')}
          >
            <span>聊天</span>
          </div>
          <div
            className={`${styles['tab-item']} ${tab === 'user' ? styles['tab-item-active'] : ''}`}
            onClick={() => setTab('user')}
          >
            <span>成员</span>
          </div>
        </div>
        {tab === 'chat' ? <Chat from="teacher" /> : <UserList from="teacher" />}
      </aside>
      <div className={styles['page-footer']}>
        <div className={styles['footer-main']}>
          <OperationBar />
        </div>
        <div className={styles.holder}></div>
      </div>
      <Modal
        visible={status.showPPTUploader}
        wrapClassName="uploader-wrap"
        onCancel={() => dispatch && dispatch({ type: 'status/setShowPPTUploader', payload: false })}
        title="上传课件"
        footer={null}
      >
        <OssUploader />
      </Modal>
      {status.isRecording && status.isInClass && (
        <div className={styles.recording}>
          <span></span>教师录制中
        </div>
      )}
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
)(Teacher)
