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
import { useMount, useUnmount, usePersistFn } from 'ahooks'
import Emitter from '@/utils/emitter'
import { reSubUser } from '@/biz/link'
import { listAllUsers } from '@/biz/user'
import { debounce } from '@/utils'
import { generateUserList } from '@/biz/user'
import Full from '@/components/studentLayouts/full'
import WithoutWb from '@/components/studentLayouts/withoutWb'
import styles from './index.less'
import { message, Modal } from 'antd'
import { ExclamationCircleOutlined } from '@ant-design/icons'

const { EventNameEnum } = window.RoomPaasSdk
const { confirm } = Modal
const emitter = Emitter.getInstance()

interface PageProps extends ConnectProps {
  room: RoomModelState
  status: StatusModelState
  user: UserModelState
  settings: SettingsModelState
  dispatch: Dispatch
}

const Student: FC<PageProps> = ({ room, status, user, dispatch, settings, history }) => {
  const [loadFinish, setLoadFinish] = useState(false)
  const linkCount = useRef<number>(0)

  const doStartClass = (classDetail: any) => {
    if (classDetail.startTime) {
      dispatch({
        type: 'timer/updateClassStartTime',
        payload: classDetail.startTime,
      })
    }
    dispatch({
      type: 'status/updateClass',
      payload: true,
    })
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
        if (list[room.ownerId]) {
          if (list[room.ownerId].isRtcMuteCamera) {
            dispatch({
              type: 'status/setTeacherCameraAvailable',
              payload: false,
            })
          }
          if (list[room.ownerId].isRtcMute) {
            dispatch({
              type: 'status/setTeacherMicAvailable',
              payload: false,
            })
          }
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

  const reciveCommentHandler = usePersistFn((e: any) => {
    if (user.userId.toString() === e.data.creatorOpenId.toString()) return
    const messageItem = {
      name: e.data.creatorNick,
      content: e.data.content,
      isMe: false,
      isOwner: room.ownerId === e.data.creatorOpenId,
    }
    dispatch &&
      dispatch({
        type: 'chat/addMsg',
        payload: messageItem,
      })
  })

  const reciveCustomMessageHandler = usePersistFn((e: any) => {
    const msg = e.data.split('/')
    if (msg[0] === 'classCustom') {
      if (msg[1] === 'setViewMode') {
        if (status.isInChannel) return
        dispatch({
          type: 'status/setViewMode',
          payload: msg[2],
        })
      }
    }
  })

  const muteAllHandeler = usePersistFn((d: any) => {
    dispatch({
      type: 'status/setIsMuteAll',
      payload: d.data.mute,
    })
  })

  const livePublishHandler = usePersistFn((d: any) => {
    dispatch({
      type: 'status/setIsLiving',
      payload: true,
    })
    setTimeout(() => {
      emitter.emit('livePublish')
    }, 1000)
  })

  const liveStopHandler = usePersistFn((d: any) => {
    dispatch({
      type: 'status/setIsLiving',
      payload: false,
    })
    emitter.emit('liveStop')
  })

  const classStartHandler = usePersistFn((d: any) => {
    window.classInstance.getClassDetail(room.classId).then((classDetail: any) => {
      doStartClass(classDetail)
    })
  })

  const classStopHandler = usePersistFn((d: any) => {
    dispatch({
      type: 'status/updateClass',
      payload: false,
    })
    if (status.isInChannel) {
      window.rtcService.leaveRtc()
      dispatch({
        type: 'status/setIsInChannel',
        payload: false,
      })
      dispatch({
        type: 'status/viewMode',
        payload: 'whiteBoard',
      })
    }
    message.info('已下课')
    setTimeout(() => {
      history.replace('/login')
      window.location.reload()
    }, 2000)
  })

  const studentJoinChannel = () => {
    window.rtcService.setVideoProfile(10, 160, 120)
    window.rtcService.aliRtcInstance.configLocalCameraPublish = settings.settings.classDefaultOpenCamera
    dispatch({
      type: 'status/setCameraAvailable',
      payload: settings.settings.classDefaultOpenCamera,
    })
    window.rtcService
      .joinChannel(user.nick)
      .then(() => {
        window.liveService.stopPlay()
        dispatch({
          type: 'status/setIsInChannel',
          payload: true,
        })
        dispatch({
          type: 'status/setIsApplying',
          payload: false,
        })
        dispatch({
          type: 'status/setMicAvailable',
          payload: true,
        })
        dispatch({
          type: 'status/setLinkMicUserCount',
          payload: window.rtcService.getUserList().length,
        })
        setTimeout(() => {
          window.rtcService.reportCameraStatus(settings.settings.classDefaultOpenCamera)
          window.rtcService.setMutePush(false)
        }, 3000)
      })
      .catch((err: any) => {
        message.error('连麦失败')
      })
  }

  const rtcInviteHandler = usePersistFn((d: any) => {
    if (!d.data.calleeList) return
    if (!d.data.calleeList.find((item: any) => item.userId === user.userId)) return
    if (status.isApplying) {
      studentJoinChannel()
    } else {
      confirm({
        title: `你收到了老师的连麦请求，是否接受？`,
        cancelText: '拒绝',
        okText: '接受',
        onOk() {
          studentJoinChannel()
        },
        onCancel() {
          window.rtcService.refuseInvite(user.nick, user.userId)
        },
      })
    }
  })

  const rtcRefuseHandler = usePersistFn((d: any) => {
    if (!d.data.approve) {
      message.error('老师拒绝了你的连麦申请')
      dispatch({
        type: 'status/setIsApplying',
        payload: false,
      })
    }
  })

  const rtcKickHandler = usePersistFn((d: any) => {
    d.data.userList.forEach((item: any) => {
      if (item.userId === user.userId) {
        // 自己被踢出
        window.rtcService.leaveRtc()
        message.info('已退出连麦')
        dispatch({
          type: 'status/setIsInChannel',
          payload: false,
        })
        dispatch({
          type: 'status/setIsApplying',
          payload: false,
        })
        setTimeout(() => {
          emitter.emit('livePublish')
        }, 1000)
      } else {
        // 别人被踢出
        dispatch({
          type: 'user/updateUser',
          payload: {
            userId: item.userId,
            isApplying: false,
            isInSeat: false,
            isInviting: false,
          },
        })
      }
    })
  })

  const rtcMuteHandler = usePersistFn((d: any) => {
    const muteList = d.data.userList
    const status = d.data.open
    for (let i = 0; i < muteList.length; i++) {
      if (muteList[i] === user.userId) {
        window.rtcService.setMutePush(!status).then(() => {
          dispatch({
            type: 'status/setMicAvailable',
            payload: status,
          })
        })
        message.info(status ? '已解除静音' : '已被老师静音')
      }
      if (muteList[i] === room.ownerId) {
        dispatch({
          type: 'status/setTeacherMicAvailable',
          payload: status,
        })
        message.info(status ? '老师已解除静音' : '老师已静音')
      }
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
    if (d.data.userId === room.ownerId) {
      dispatch({
        type: 'status/setTeacherCameraAvailable',
        payload: status,
      })
      message.info(status ? '老师已开启摄像头' : '老师已关闭摄像头')
    }
    dispatch({
      // 有人关闭了
      type: 'user/updateUser',
      payload: {
        userId: d.data.userId,
        isRtcMuteCamera: !status,
      },
    })
  })

  const rtcScreenHandler = usePersistFn((d: any) => {
    if (!status.isInChannel) return
    dispatch({
      type: 'user/updateUser',
      payload: {
        userId: d.data.userId,
        streamType: d.data.open ? 2 : 1,
      },
    })
  })

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

  const setCurrentVideoProfile = () => {
    if (settings.studentResolutionSteps.length === 0) return
    const list = window.rtcService.getUserList()
    for (let i = 0; i < settings.studentResolutionSteps.length; i++) {
      if (list.length <= settings.studentResolutionSteps[i]) {
        if (
          linkCount.current === 0 ||
          (i === 0 && linkCount.current > settings.studentResolutionSteps[i]) ||
          (i > 0 &&
            (linkCount.current <= settings.studentResolutionSteps[i - 1] ||
              linkCount.current > settings.studentResolutionSteps[i]))
        ) {
          window.rtcService.setVideoProfile(...settings.studentResolutions[i])
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

  const bindEvents = () => {
    window.chatService.on(EventNameEnum.PaaSChatReciveComment, reciveCommentHandler)
    window.chatService.on(EventNameEnum.PaaSChatCustomMessage, reciveCustomMessageHandler)
    window.roomChannel.on(EventNameEnum.PaaSRoomUpdateNotice, changeNoticeHandler)
    window.roomChannel.on(EventNameEnum.PaaSRoomEnter, enterRoomHandler)
    window.chatService.on(EventNameEnum.PaaSChatMuteAll, muteAllHandeler)
    window.liveService.on(EventNameEnum.PaaSLivePublish, livePublishHandler)
    window.liveService.on(EventNameEnum.PaaSLiveStop, liveStopHandler)
    window.roomChannel.on(EventNameEnum.PaaSClassStart, classStartHandler)
    window.roomChannel.on(EventNameEnum.PaaSClassStop, classStopHandler)
    window.rtcService.on(EventNameEnum.PaaSRtcInvite, rtcInviteHandler)
    window.rtcService.on(EventNameEnum.PaaSRtcRefuse, rtcRefuseHandler)
    window.rtcService.on(EventNameEnum.PaaSRtcKickUser, rtcKickHandler)
    window.rtcService.on(EventNameEnum.PaaSRtcMute, rtcMuteHandler)
    window.rtcService.on(EventNameEnum.PaaSRtcCamera, rtcMuteCameraHandler)
    window.rtcService.on(EventNameEnum.PaaSRtcScreen, rtcScreenHandler)
    window.rtcService.on(EventNameEnum.onJoin, rtcJoinHandler)
    window.rtcService.on(EventNameEnum.onLeave, rtcLeaveHandler)
    window.rtcService.on(EventNameEnum.onMedia, rtcMediaHandler)
    window.rtcService.on(EventNameEnum.onError, rtcErrorHandler)
  }

  useEffect(() => {
    if (status.viewMode === 'whiteBoard') {
      dispatch({ type: 'status/setLayout', payload: '6' })
    }
  }, [status.viewMode])

  useMount(() => {
    if (!window.roomEngine) {
      if (window.sessionStorage.getItem('userNick')) {
        window.sessionStorage.setItem('refresh', '1')
        history.replace('/doLogin')
      } else {
        history.replace('/login')
      }
      return
    }
    bindEvents()
    initUserList()
    setLoadFinish(true)
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
    window.classInstance.getClassDetail(room.classId).then((classDetail: any) => {
      if (classDetail.status === 1) {
        doStartClass(classDetail)
        dispatch({
          type: 'status/setIsLiving',
          payload: true,
        })
        setTimeout(() => {
          emitter.emit('livePublish')
        }, 1000)
      }
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
      window.rtcService.leaveRtc()
      window.roomChannel.leaveRoom()
      window.roomEngine.logout()
    }
  })

  const rtcMuteCamera = async () => {
    try {
      if (status.cameraAvailable) {
        window.rtcService._rtcManager.getAliRtcEngine().configLocalCameraPublish = false
        await window.rtcService.startPublish()
        await window.rtcService.reportCameraStatus(false)
      } else {
        await window.rtcService.startPublishCamera()
        await window.rtcService.reportCameraStatus(true)
      }
      dispatch({
        type: 'status/setCameraAvailable',
        payload: !status.cameraAvailable,
      })
    } catch (err) {
      console.error(err)
      message.error('切换摄像头推流失败，请检查配置或尝试刷新页面')
    }
  }

  const rtcMutePush = () => {
    window.rtcService.setMutePush(status.micAvailable).then(() => {
      dispatch({
        type: 'status/setMicAvailable',
        payload: !status.micAvailable,
      })
    })
  }

  const cancelLinkMic = () => {
    if (status.isApplying) {
      window.rtcService
        .applyJoinChannel(false)
        .then(() => {
          dispatch({
            type: 'status/setIsApplying',
            payload: false,
          })
          message.success('已取消连麦申请')
        })
        .catch((err: any) => {
          console.error(err)
          message.error('操作失败')
        })
    } else {
      window.rtcService.leaveRtc()
      dispatch({
        type: 'status/setIsInChannel',
        payload: false,
      })
      setTimeout(() => {
        emitter.emit('livePublish')
      }, 1000)
      message.success('已下麦')
    }
  }

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

  const rtcErrorHandler = (d: any) => {
    if (d.errorCode === 10302) {
      reSubUser(d.userId, room.ownerId === d.userId)
    }
    if (d.errorCode === 10008) {
      message.info({
        content: '检测到您的外界设备麦克风拔出，可能会导致推流失败，可刷新页面解决',
      })
    }
  }

  const applyLinkMic = () => {
    const supportInfo = window.rtcService._rtcManager && window.rtcService._rtcManager.supportInfo
    if (!supportInfo || !supportInfo.isSupported || !supportInfo.audioDevice || !supportInfo.videoDevice) {
      Modal.error({
        title: '兼容性错误',
        content: '互动课堂的连麦功能需要使用摄像头和麦克风，请检查您的硬件是否可用，或是否通过了浏览器或系统的授权',
      })
      return
    }
    if (status.isApplying) {
      window.rtcService
        .applyJoinChannel(false)
        .then(() => {
          dispatch({
            type: 'status/setIsApplying',
            payload: false,
          })
          message.success('已取消连麦申请')
        })
        .catch((err: any) => {
          console.error(err)
          message.error('操作失败')
        })
    } else {
      confirm({
        title: '您确定要向老师申请连麦吗？',
        cancelText: '取消',
        okText: '确定',
        icon: <ExclamationCircleOutlined />,
        content: '连麦成功后，方可语音和视频沟通内容',
        onOk() {
          window.rtcService
            .applyJoinChannel(true)
            .then(() => {
              dispatch({
                type: 'status/setIsApplying',
                payload: true,
              })
              message.success('已向老师发送请求')
            })
            .catch((err: any) => {
              console.error(err)
              message.error('申请连麦失败')
            })
        },
        onCancel() {
          message.info('取消连麦申请')
        },
      })
    }
  }

  return (
    <div className={styles['page-container']}>
      {loadFinish &&
        (settings.settings.classScene && settings.settings.classScene.enableWhiteBoard ? <Full /> : <WithoutWb />)}
      {status.isInClass &&
        (status.isInChannel ? (
          <div className={styles['link-control-bar']}>
            <div className={styles['bar-item']} onClick={rtcMutePush} title="是否静音">
              <svg className="icon" aria-hidden="true">
                <use
                  xlinkHref={status.micAvailable ? '#icon-ic_toolbar_jingyin' : '#icon-ic_toolbar_quxiaojingyin'}
                ></use>
              </svg>
            </div>
            <div className={styles['bar-item']} onClick={rtcMuteCamera} title="是否关闭摄像头">
              <svg className="icon" aria-hidden="true">
                <use
                  xlinkHref={
                    status.cameraAvailable ? '#icon-ic_toolbar_shexiangtou' : '#icon-ic_toolbar_guanshexiangtou'
                  }
                ></use>
              </svg>
            </div>
            <div className={styles['bar-item']} onClick={cancelLinkMic} title="取消连麦">
              <svg className="icon" aria-hidden="true">
                <use xlinkHref="#icon-ic_toolbar_quxiaolianmai"></use>
              </svg>
            </div>
          </div>
        ) : (
          <div className={styles['link-control-bar']}>
            <div className={styles['bar-item']}>
              <svg className="icon" aria-hidden="true" onClick={applyLinkMic}>
                <use
                  xlinkHref={status.isApplying ? '#icon-ic_toolbar_quxiaolianmai' : '#icon-ic_toolbar_lianmai'}
                ></use>
              </svg>
            </div>
          </div>
        ))}
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
)(Student)
