import { FC, useEffect, useState } from 'react'
import { RoomModelState, StatusModelState, ChatModelState, UserModelState, ConnectProps, connect, Dispatch } from 'umi'
import { useMount, useUnmount, usePersistFn } from 'ahooks'
import Emitter from '@/utils/emitter'
import Chat from '@/components/chat'
import WhiteBoard from '@/components/whiteBoard'
import { generateUserList } from '@/models/user'
import VideoLayout from '@/components/videoLayout'
import Player from '@/components/player'
import styles from './index.less'
import { message, Modal } from 'antd'
import { ExclamationCircleOutlined } from '@ant-design/icons'

const { EventNameEnum } = window.RoomPaasSdk
const { confirm } = Modal
const emitter = Emitter.getInstance()

interface PageProps extends ConnectProps {
  room: RoomModelState
  status: StatusModelState
  chat: ChatModelState
  user: UserModelState
  dispatch: Dispatch
}

const Student: FC<PageProps> = ({ room, status, chat, user, dispatch, history }) => {
  const [tab, setTab] = useState('chat')
  const [loadFinish, setLoadFinish] = useState(false)
  const switchViewMode = () => {
    if (!dispatch) return
    if (status.viewMode === 'whiteBoard') {
      dispatch({ type: 'status/setLayout', payload: '6' })
    }
    const mode = status.viewMode === 'whiteBoard' ? 'video' : 'whiteBoard'
    dispatch({ type: 'status/setViewMode', payload: mode })
    setTimeout(() => {
      emitter.emit('needSetVideo')
    }, 100)
  }
  const doStartClass = (classDetail: any) => {
    if (classDetail.startTime) {
      dispatch({
        type: 'room/updateClassStartTime',
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
        window.roomChannel.listUser(1, 50).then((res: any) => res.userList),
      ]).then((res: any) => {
        const [confUserList, applyList, userList] = res
        const list = generateUserList(userList, room.ownerId, user.userId, applyList, confUserList)
        console.log(list)
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
  })
  const rtcInviteHandler = usePersistFn((d: any) => {
    if (!d.data.calleeList) return
    if (!d.data.calleeList.find((item: any) => item.userId === user.userId)) return
    if (status.isApplying) {
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
        })
        .catch((err: any) => {
          message.error('连麦失败')
        })
    } else {
      confirm({
        title: `你收到了老师的连麦请求，是否接受？`,
        cancelText: '拒绝',
        okText: '接受',
        onOk() {
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
                type: 'status/setCameraAvailable',
                payload: true,
              })
            })
            .catch((err: any) => {
              message.error('连麦失败')
            })
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
    const kickList = d.data.userList
    if (!kickList.find((item: any) => item.userId === user.userId)) return
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
        message.info('已被老师静音')
        continue
      }
      dispatch({
        type: 'user/updateUser',
        payload: {
          userId: muteList[i],
          isRtcMute: !status,
        },
      })
    }
    setTimeout(() => {
      emitter.emit('needSetVideo')
    }, 100)
  })
  const rtcMuteCameraHandler = usePersistFn((d: any) => {
    const status = d.data.open
    if (d.data.userId === user.userId) {
      // todo: 判断消息来源
      // window.rtcService.setMuteCamera(!status).then(() => {
      //   dispatch({
      //     type: 'status/setMicAvailable',
      //     payload: status,
      //   });
      // });
      return
    }
    dispatch({
      // 有人关闭了
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

  const rtcOnSubscribeHandler = usePersistFn((d: any) => {
    console.log(d)
    dispatch({
      type: 'user/updateUser',
      payload: {
        userId: d.userId,
        subscribeResult: true,
        isInSeat: true,
        streamType: d.streamType || 1,
      },
    })
    setTimeout(() => {
      emitter.emit('needSetVideo')
    }, 100)
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
    new Promise<void>((resolve) => {
      if (d.data.open) {
        window.rtcService
          .unSubscribe(d.data.userId)
          .then(() => {
            return window.rtcService.subscribeScreen(d.data.userId)
          })
          .then(() => {
            resolve()
          })
      } else {
        window.rtcService
          .unSubscribe(d.data.userId)
          .then(() => {
            return window.rtcService.subscribe(d.data.userId)
          })
          .then(() => {
            resolve()
          })
      }
    }).then(() => {
      setTimeout(() => {
        emitter.emit('needSetVideo')
      }, 100)
    })
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

  const rtcLeaveHandler = usePersistFn((d: any) => {
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

  const bindEvents = () => {
    window.chatService.on(EventNameEnum.PaaSChatReciveComment, reciveCommentHandler)
    window.chatService.on(EventNameEnum.PaaSChatCustomMessage, reciveCustomMessageHandler)
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
    window.rtcService.on(EventNameEnum.onSubscribeResult, rtcOnSubscribeHandler)
    window.rtcService.on(EventNameEnum.PaaSRtcScreen, rtcScreenHandler)
    window.rtcService.on(EventNameEnum.onLeave, rtcLeaveHandler)
    initUserList()
  }
  const startPlay = () => {
    // 解决不点击就没法自动播放的问题
    if (status.isLiving) {
      window.liveService.startPlay()
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
    bindEvents()
    setLoadFinish(true)
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
  const rtcMuteCamera = () => {
    window.rtcService.setMuteCamera(status.cameraAvailable).then(() => {
      dispatch({
        type: 'status/setCameraAvailable',
        payload: !status.cameraAvailable,
      })
    })
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
  const applyLinkMic = () => {
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
      <div className={`${status.viewMode === 'whiteBoard' ? styles['page-main'] : styles.abbreviate}`}>
        <div
          className={styles['abbreviate-container']}
          style={{
            display: status.viewMode === 'whiteBoard' ? 'none' : 'block',
          }}
        >
          <div className={styles['abbr-type']}>讲解画面</div>
          <div className={styles['abbr-operation-bar']}>
            {status.isInChannel && (
              <div className={styles['operation-item']} onClick={switchViewMode}>
                <svg className="icon" aria-hidden="true">
                  <use xlinkHref="#icon-ic_qiehuanshitu"></use>
                </svg>
              </div>
            )}
          </div>
        </div>
        {room.docKey && <WhiteBoard boardType="pure" />}
        {status.isInClass &&
          status.viewMode === 'whiteBoard' &&
          (status.isInChannel ? (
            <div className={styles['link-control-bar']}>
              <div className={styles['bar-item']} onClick={rtcMutePush} title="是否静音">
                <svg className="icon" aria-hidden="true">
                  <use
                    xlinkHref={status.micAvailable ? '#icon-ic_toolbar_jingyin' : '#icon-ic_toolbar_quxiaojingyin'}
                  ></use>
                </svg>
              </div>
              <div className={styles['bar-item']} onClick={rtcMuteCamera}>
                <svg className="icon" aria-hidden="true">
                  <use
                    xlinkHref={
                      status.cameraAvailable ? '#icon-ic_toolbar_shexiangtou' : '#icon-ic_toolbar_guanshexiangtou'
                    }
                  ></use>
                </svg>
              </div>
              <div className={styles['bar-item']} onClick={cancelLinkMic}>
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
      <div className={`${status.viewMode === 'video' ? styles['page-main'] : styles.abbreviate}`} onClick={startPlay}>
        <div
          className={styles['abbreviate-container']}
          style={{ display: status.viewMode === 'video' ? 'none' : 'block' }}
        >
          <div className={styles['abbr-type']}>教师画面</div>
          <div className={styles['abbr-operation-bar']}>
            {status.isInChannel && (
              <div className={styles['operation-item']} onClick={switchViewMode}>
                <svg className="icon" aria-hidden="true">
                  <use xlinkHref="#icon-ic_qiehuanshitu"></use>
                </svg>
              </div>
            )}
          </div>
        </div>
        {loadFinish && (status.isInChannel ? <VideoLayout /> : <Player />)}
        {status.isInClass &&
          status.viewMode === 'video' &&
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
      <aside className={styles.sidebar}>
        <div className={styles['tab-container']}>
          <div
            className={`${styles['tab-item']} ${tab === 'chat' ? styles['tab-item-active'] : ''}`}
            onClick={() => setTab('chat')}
          >
            <span>聊天</span>
          </div>
          <div className={`${styles['tab-item']} ${tab === 'user' ? styles['tab-item-active'] : ''}`}>
            <span>课件</span>
          </div>
        </div>
        {tab === 'chat' ? <Chat from="student" /> : <div></div>}
      </aside>
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
)(Student)
