import { FC, useEffect, useState, useRef } from 'react'
import {
  RoomModelState,
  StatusModelState,
  UserModelState,
  SettingsModelState,
  ConnectProps,
  connect,
  Dispatch,
} from 'umi'
import { message, Modal, Input, Button } from 'antd'
import { usePersistFn } from 'ahooks'
import { generateUserList } from '@/biz/user'
import { debounce } from '@/utils'
import { ExclamationCircleOutlined } from '@ant-design/icons'
import { reSubUser } from '@/biz/link'
import { listAllUsers } from '@/biz/user'
import Emitter from '@/utils/emitter'
import Full from '@/components/teacherLayouts/full'
import WithoutWb from '@/components/teacherLayouts/withoutWb'
import styles from './index.less'

const { EventNameEnum } = window.RoomPaasSdk
const emitter = Emitter.getInstance()
const { TextArea } = Input

interface PageProps extends ConnectProps {
  room: RoomModelState
  status: StatusModelState
  user: UserModelState
  settings: SettingsModelState
  dispatch: Dispatch
}

const Teacher: FC<PageProps> = ({ room, status, user, settings, dispatch, history }) => {
  const [loadFinish, setLoadFinish] = useState(false)
  const [notice, setNotice] = useState('')
  const [noticeLoading, setNoticeLoading] = useState(false)
  const linkCount = useRef<number>(0)

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

  const isSomeoneInSeat = () => {
    return (
      Object.values(user.userList).filter((item) => item.isInSeat && !(item.userId === room.ownerId) && !item.isMe)
        .length > 0
    )
  }

  const debounceEnterRoom = debounce(() => {
    Promise.all([
      window.rtcService.listConfUser(1, 50).then((res: any) => res.userList),
      window.rtcService.listApplyLinkMicUser(1, 50).then((res: any) => res.userList),
      listAllUsers(),
    ]).then((res: any) => {
      const [confUserList, applyList, userList] = res
      const list = generateUserList(userList, room.ownerId, user.userId, applyList, confUserList, true)
      dispatch({
        type: 'user/mergeUser',
        payload: list,
      })
    })
  }, 1500)

  const enterRoomHandler = (e: any) => {
    if (e.data.userId === user.userId) return
    debounceEnterRoom()
  }

  const rtcApplyHandler = usePersistFn((d: any) => {
    if (d.data.isApply) {
      if (!user.userList[d.data.applyUser.userId] || user.userList[d.data.applyUser.userId].isApplying) return
      message.info(`${user.userList[d.data.applyUser.userId].nick}????????????`)
    } else {
      message.info(`${user.userList[d.data.applyUser.userId].nick}?????????????????????`)
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
      message.success(`${user.userList[userList[i].userId].nick}????????????`)
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
      message.info(`${user.userList[userList[i].userId].nick}??????????????????`)
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
  const leaveChannelHandler = usePersistFn((d: any) => {
    const userList = d.data.userList
    for (let i = 0; i < userList.length; i++) {
      if (userList[i].userId === room.ownerId) continue
      message.info(`${user.userList[userList[i].userId].nick}????????????`)
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

  const setCurrentVideoProfile = () => {
    if (settings.teacherResolutionSteps.length === 0) return
    const list = window.rtcService.getUserList()
    for (let i = 0; i < settings.teacherResolutionSteps.length; i++) {
      if (list.length <= settings.teacherResolutionSteps[i]) {
        if (
          linkCount.current === 0 ||
          (i === 0 && linkCount.current > settings.teacherResolutionSteps[i]) ||
          (i > 0 &&
            (linkCount.current <= settings.teacherResolutionSteps[i - 1] ||
              linkCount.current > settings.teacherResolutionSteps[i]))
        ) {
          window.rtcService.setVideoProfile(...settings.teacherResolutions[i])
        }
        break
      }
    }
    for (let i = 0; i < settings.teacherScreenResolutionSteps.length; i++) {
      if (list.length <= settings.teacherScreenResolutionSteps[i]) {
        if (
          linkCount.current === 0 ||
          (i === 0 && linkCount.current > settings.teacherScreenResolutionSteps[i]) ||
          (i > 0 &&
            (linkCount.current <= settings.teacherScreenResolutionSteps[i - 1] ||
              linkCount.current > settings.teacherScreenResolutionSteps[i - 1]))
        ) {
          window.rtcService.setScreenShareVideoProfile(...settings.teacherScreenResolutions[i])
        }
        break
      }
    }
    linkCount.current = list.length
  }

  const rtcJoinHandler = usePersistFn((d: any) => {
    setCurrentVideoProfile()
    dispatch({
      type: 'status/setLinkMicUserCount',
      payload: status.linkMicUserCount + 1,
    })
  })

  const rtcLeaveHandler = usePersistFn((d: any) => {
    setCurrentVideoProfile()
    dispatch({
      type: 'status/setLinkMicUserCount',
      payload: status.linkMicUserCount - 1,
    })
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
      if (window.rtcService.getUserList().length === 0 && !status.isScreenSharing) {
        window.rtcService.setLayout([user.userId], 1)
      }
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
  })

  const rtcErrorHandler = usePersistFn((d: any) => {
    if (d.errorCode === 10011 || d.errorCode === 10012) {
      window.rtcService.stopPublishScreen().then(() => {
        console.log('stop publish screen then~')
        emitter.emit('needSubscribe')
      })
      dispatch({
        type: 'status/setIsScreenSharing',
        payload: false,
      })
      if (!isSomeoneInSeat()) {
        // ????????????????????????setlayout?????????
        window.rtcService.setLayout([user.userId], 1)
      }
    }
    if (d.errorCode === 10302) {
      reSubUser(d.userId, room.ownerId === d.userId)
    }
    if (d.errorCode === 10008) {
      message.info({
        content: '????????????????????????????????????????????????????????????????????????????????????????????????',
      })
    }
  })

  const rtcMediaHandler = usePersistFn((d: any) => {
    if (d.userId === '0') return
    else {
      dispatch({
        type: 'user/updateUser',
        payload: {
          userId: d.userId,
          streamInfo: d.data,
        },
      })
    }
  })

  const changeNoticeHandler = usePersistFn((d: any) => {
    if (d.data) {
      dispatch({
        type: 'room/setRoomDetail',
        payload: {
          notice: d.data.notice,
        },
      })
    }
  })

  const docStatusHandler = usePersistFn((d: any) => {
    if (d.data.status === 'CONVERSION_TASK_STATUS_SUCCESS') {
      emitter.emit('insertPPT')
      emitter.emit('convertDone', true)
    } else {
      emitter.emit('convertDone', false)
    }
    dispatch({
      type: 'status/setIsDocConverting',
      payload: false,
    })
  })

  const closeNoticeHandler = () => {
    dispatch({
      type: 'status/setShowNoticeEditor',
      payload: false,
    })
  }

  const updateNoticeHandler = () => {
    if (!notice) return message.error('????????????')
    setNoticeLoading(true)
    window.roomChannel
      .updateNotice(notice)
      .then(() => {
        message.success('??????????????????')
        closeNoticeHandler()
      })
      .catch((err: any) => {
        console.error(err)
        message.error('??????????????????')
      })
      .finally(() => {
        setNoticeLoading(false)
      })
  }

  const deleteNoticeHandler = () => {
    Modal.confirm({
      title: '??????????????????????????????',
      icon: <ExclamationCircleOutlined />,
      content: '??????????????????????????????????????????????????????',
      okText: '??????',
      cancelText: '??????',
      onOk() {
        setNoticeLoading(true)
        window.roomChannel
          .updateNotice('')
          .then(() => {
            message.success('??????????????????')
            setNotice('')
            closeNoticeHandler()
          })
          .catch((err: any) => {
            console.error(err)
            message.error('??????????????????')
          })
          .finally(() => {
            setNoticeLoading(false)
          })
      },
    })
  }

  const classStopHandler = usePersistFn((d: any) => {
    if (window.sessionStorage.getItem('teacherStopClass')) {
      window.sessionStorage.removeItem('teacherStopClass')
      return
    }
    if (status.isInChannel) {
      window.rtcService.leaveRtc()
    }
    message.info('????????????????????????????????????????????????????????????????????????????????????')
    setTimeout(() => {
      history.replace('/login')
      window.location.reload()
    }, 2000)
  })

  const inputNotice = (e: any) => {
    setNotice(e.target.value)
  }

  const bindEvents = () => {
    window.chatService.on(EventNameEnum.PaaSChatReciveComment, reciveCommentHandler)
    window.chatService.on(EventNameEnum.PaaSChatCustomMessage, reciveCustomMessageHandler)
    window.roomChannel.on(EventNameEnum.PaaSRoomEnter, enterRoomHandler)
    window.roomChannel.on(EventNameEnum.PaaSRoomUpdateNotice, changeNoticeHandler)
    window.roomChannel.on(EventNameEnum.PaaSClassStop, classStopHandler)
    window.docService.on(EventNameEnum.PaaSDocConversionStatus, docStatusHandler)
    window.rtcService.on(EventNameEnum.PaaSRtcApply, rtcApplyHandler)
    window.rtcService.on(EventNameEnum.PaaSRtcJoinSuccess, joinSuccessHandler)
    window.rtcService.on(EventNameEnum.PaaSRtcJoinFailed, joinFailedHandler)
    window.rtcService.on(EventNameEnum.PaaSRtcLeaveChannel, leaveChannelHandler)
    window.rtcService.on(EventNameEnum.PaaSRtcMute, rtcMuteHandler)
    window.rtcService.on(EventNameEnum.PaaSRtcCamera, rtcMuteCameraHandler)
    window.rtcService.on(EventNameEnum.onError, rtcErrorHandler)
    window.rtcService.on(EventNameEnum.onJoin, rtcJoinHandler)
    window.rtcService.on(EventNameEnum.onLeave, rtcLeaveHandler)
    window.rtcService.on(EventNameEnum.onMedia, rtcMediaHandler)
  }

  const initUserList = () => {
    if (status.classStatus === 1) {
      Promise.all([
        window.rtcService.listConfUser(1, 100).then((res: any) => res.userList),
        window.rtcService.listApplyLinkMicUser(1, 50).then((res: any) => res.userList),
        listAllUsers(),
      ]).then((res: any) => {
        const [confUserList, applyList, userList] = res
        const list = generateUserList(userList, room.ownerId, user.userId, applyList, confUserList, true)
        const linkMicList = Object.values(list).filter((item) => item.isInSeat && !item.isOwner)
        if (linkMicList.length > 0) {
          const layoutList = linkMicList.map((item) => item.userId)
          layoutList.unshift(user.userId)
          window.rtcService.setLayout(layoutList, 2)
        } else {
          window.rtcService.setLayout([user.userId], 1)
        }
        dispatch({
          type: 'user/setUserList',
          payload: list,
        })
      })
    } else {
      listAllUsers().then((res: any) => {
        const list = generateUserList(res, room.ownerId, user.userId)
        dispatch({
          type: 'user/setUserList',
          payload: list,
        })
      })
    }
  }

  useEffect(() => {
    setNotice(room.notice)
  }, [status.showNoticeEditor])
  useEffect(() => {
    if (status.viewMode === 'whiteBoard') {
      dispatch({ type: 'status/setLayout', payload: '6' })
    }
  }, [status.viewMode])
  useEffect(() => {
    if (!window.roomEngine) {
      window.sessionStorage.getItem('userNick') ? history.replace('/doLogin') : history.replace('/login')
      return
    }
    setLoadFinish(true)
    bindEvents()
    window.chatService.listComment(0, 1, 50).then((res: any) => {
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
    window.chatService.getChatDetail().then((res: any) => {
      dispatch({
        type: 'status/setIsMuteAll',
        payload: res.muteAll,
      })
      dispatch({
        type: 'status/setIsMuteSelf',
        payload: res.mute && !res.muteAll,
      })
    })
    // ???joinChannel????????????
    emitter.on('needInitUserList', initUserList)
    setTimeout(() => {
      const supportInfo = window.rtcService._rtcManager && window.rtcService._rtcManager.supportInfo
      if (!supportInfo) {
        Modal.info({
          content: '????????????????????????????????????????????????????????????????????????????????????',
        })
      } else {
        if (!supportInfo.isSupported || !supportInfo.audioDevice) {
          Modal.error({
            title: '???????????????',
            content: '??????????????????????????????????????????WebRTC???????????????????????????????????????????????????????????????????????????',
          })
        } else if (!supportInfo.videoDevice) {
          Modal.error({
            title: '???????????????',
            content: '?????????????????????????????????????????????????????????????????????????????????',
          })
        }
      }
    }, 5000)
    window.addEventListener(
      'beforeunload',
      () => {
        if (window.roomChannel) {
          window.rtcService.leaveRtc()
          window.roomChannel.leaveRoom()
        }
      },
      true,
    )
    return () => {
      if (window.roomChannel) {
        window.roomChannel.leaveRoom()
        window.rtcService.leaveRtc().then(() => window.roomEngine.logout())
      }
    }
  }, [])
  return (
    <div className={styles['page-container']}>
      {loadFinish &&
        (settings.settings.classScene && settings.settings.classScene.enableWhiteBoard ? <Full /> : <WithoutWb />)}
      <Modal
        title="??????"
        centered
        visible={status.showNoticeEditor}
        onOk={updateNoticeHandler}
        onCancel={closeNoticeHandler}
        width={450}
        okText="??????"
        cancelText="??????"
        maskClosable={false}
        mask={false}
        confirmLoading={noticeLoading}
        footer={[
          <Button key="cancel" onClick={closeNoticeHandler}>
            ??????
          </Button>,
          room.notice ? (
            <Button key="on" onClick={deleteNoticeHandler} loading={noticeLoading} type="primary">
              ??????
            </Button>
          ) : (
            <Button key="on" onClick={updateNoticeHandler} loading={noticeLoading} type="primary">
              ??????
            </Button>
          ),
        ]}
      >
        <TextArea
          maxLength={120}
          showCount
          value={notice}
          autoSize
          placeholder="??????????????????"
          onChange={inputNotice}
          readOnly={!!room.notice}
          className={styles['notice-textarea']}
        />
      </Modal>
    </div>
  )
}

export default connect(
  ({
    room,
    status,
    user,
    settings,
  }: {
    room: RoomModelState
    status: StatusModelState
    user: UserModelState
    settings: SettingsModelState
  }) => ({
    room,
    status,
    user,
    settings,
  }),
)(Teacher)
