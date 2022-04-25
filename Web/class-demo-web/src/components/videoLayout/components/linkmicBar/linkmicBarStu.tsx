import { FC, useEffect, useState, useRef } from 'react'
import { RoomModelState, StatusModelState, UserModelState, connect, Dispatch } from 'umi'
import { usePersistFn } from 'ahooks'
import VideoItem from '../videoItem'
import { message } from 'antd'
import Emitter from '@/utils/emitter'
import styles from './linkmicBar.less'
import { RtcUserInfo, StreamConfigInfo } from './linkmicBar'

const emitter = Emitter.getInstance()
const { EventNameEnum } = window.RoomPaasSdk
const onePageSize = 6

interface PageProps {
  room: RoomModelState
  status: StatusModelState
  user: UserModelState
  dispatch: Dispatch
}

const SpeakerStu: FC<PageProps> = ({ room, status, user, dispatch }) => {
  const [page, setPage] = useState(0)
  const [userList, setUserList] = useState<RtcUserInfo[]>([])
  const [currentVideoList, setCurrentVideoList] = useState<RtcUserInfo[]>([])
  const changePageOpFlag = useRef(false)
  const meRtc: RtcUserInfo = {
    userId: user.userId,
    displayName: user.userId,
    muteAudioPlaying: false,
    streamConfigs: [],
  }

  const changePage = (to: 'prev' | 'next') => {
    const list = window.rtcService.getUserList()
    const size = Math.ceil(list.length / onePageSize)
    if ((to === 'prev' && page <= 0) || (to === 'next' && page >= size - 1)) return
    if (changePageOpFlag.current) return message.info('请勿频繁操作')
    changePageOpFlag.current = true
    setTimeout(() => {
      changePageOpFlag.current = false
    }, 1000)
    if (to === 'prev') {
      setPage(page - 1)
    }
    if (to === 'next') {
      setPage(page + 1)
    }
    setTimeout(() => {
      subscribeUser()
    }, 100)
  }

  const getUserList = (): RtcUserInfo[] => {
    const list = (window.rtcService.getUserList() as RtcUserInfo[]).filter((item) => item.userId !== room.ownerId)
    list.unshift(meRtc)
    return list
  }

  const generateCurrentVideoList = (page: number): RtcUserInfo[] => {
    if (!window.rtcService) return []
    const list = getUserList()
    return list.slice(page * onePageSize, (page + 1) * onePageSize)
  }

  const doAllSubscribe = usePersistFn(async () => {
    if (!window.rtcService) return
    const list: RtcUserInfo[] = getUserList()
    const currentList = generateCurrentVideoList(page)
    setUserList(list)
    setCurrentVideoList([])
    setCurrentVideoList(currentList)
    if (list.length === 0) return
    for (let i in list) {
      console.log('开始订阅', list[i].displayName)
      await doSubscribe(list[i].userId)
    }
  })

  const subscribeUser = () => {
    setTimeout(() => {
      console.log('开始订阅全部')
      doAllSubscribe()
    }, 1000)
  }

  const doSubscribe = async (userId: string, userInfo?: RtcUserInfo) => {
    // 获取用户rtc信息
    const confUserInfo = userInfo || getUserList().find((item) => item.userId === userId)
    if (!confUserInfo || confUserInfo.userId === room.ownerId) return
    // 如果没有任何流则不做操作
    if (confUserInfo.streamConfigs.length === 0 && userId !== user.userId) return
    // 查看当前用户是否要显示
    const isCurrent = generateCurrentVideoList(page)
      .map((item) => item.userId)
      .some((item) => item === userId)
    let stream = 1
    const streamInfo = (user.userList[userId] && user.userList[userId].streamInfo) || null
    const audioStreamConfig = confUserInfo.streamConfigs.find((item) => item.label === 'sophon_audio')
    const cameraStreamConfig = confUserInfo.streamConfigs.find((item) => item.label === 'sophon_video_camera_large')
    try {
      if (userId === user.userId) {
        if (isCurrent) {
          setTimeout(() => {
            window.rtcService.startRtcPreview(document.getElementById('preview'))
          }, 500)
        }
        return
      }
      if (isCurrent) {
        // 如果需要显示
        const screenShareStreamConfig = confUserInfo.streamConfigs.find(
          (item) => item.label === 'sophon_video_screen_share',
        )
        if (screenShareStreamConfig && screenShareStreamConfig.state === 'active') {
          if (streamInfo && screenShareStreamConfig.subscribed && !streamInfo.screen) {
            await window.rtcService.unSubscribe(userId)
            screenShareStreamConfig.subscribed = false
          }
          !screenShareStreamConfig.subscribed && (await window.rtcService.subscribeScreen(userId))
          console.log('订阅了', confUserInfo.displayName, 'screen流')
          stream = 2
        } else if (cameraStreamConfig && cameraStreamConfig.state === 'active') {
          if (streamInfo && cameraStreamConfig.subscribed && !streamInfo.camera) {
            await window.rtcService.unSubscribe(userId)
            cameraStreamConfig.subscribed = false
          }
          !cameraStreamConfig.subscribed && (await window.rtcService.subscribeCamera(userId))
          console.log('订阅了', confUserInfo.displayName, 'camera流')
        } else if (audioStreamConfig && audioStreamConfig.state === 'active') {
          if (streamInfo && audioStreamConfig.subscribed && !streamInfo.audio) {
            await window.rtcService.unSubscribe(userId)
            audioStreamConfig.subscribed = false
          }
          !audioStreamConfig.subscribed && (await window.rtcService.subscribeAudio(userId))
          console.log('订阅了', confUserInfo.displayName, 'audio流')
        } else {
          console.log(confUserInfo.displayName, '没有任何流')
        }
        setTimeout(() => {
          console.log('订阅时set了', confUserInfo.displayName)
          window.rtcService.setDisplayRemoteVideo(document.getElementById(`video-${userId}`), userId, stream)
        }, 200)
      } else {
        await window.rtcService.subscribeAudio(userId)
        console.log('静默订阅了', confUserInfo.displayName, 'audio流')
        window.rtcService.setDisplayRemoteVideo(document.createElement('video'), userId, 1)
      }
      dispatch({
        type: 'user/updateUser',
        payload: {
          userId,
          isRtcMute: !cameraStreamConfig || audioStreamConfig?.state === 'inactive' || audioStreamConfig?.muted,
          isRtcMuteCamera: !cameraStreamConfig || cameraStreamConfig?.state === 'inactive',
          streamType: stream,
        },
      })
    } catch (err) {
      console.error(`订阅${confUserInfo.displayName}时失败了`)
      console.error(err)
    }
  }

  const onPublisherHandler = usePersistFn((info: RtcUserInfo) => {
    if (info.userId === room.ownerId) {
      // subscribeUser()
      return
    }
    setTimeout(() => {
      console.log('onPublisher开始订阅')
      doSubscribe(info.userId, info)
    }, 500)
  })

  useEffect(() => {
    if (!window.rtcService) return
    window.rtcService.on(EventNameEnum.onPublisher, onPublisherHandler)
    window.rtcService.on(EventNameEnum.onUnPublisher, subscribeUser)
    emitter.on('needSubscribe', subscribeUser)
    return () => {
      if (!window.rtcService) return
      window.rtcService.stopRtcPreview()
      emitter.remove('needSubscribe', subscribeUser)
      window.rtcService.remove(EventNameEnum.onPublisher, onPublisherHandler)
      window.rtcService.remove(EventNameEnum.onUnPublisher, subscribeUser)
    }
  }, [])

  useEffect(() => {
    setCurrentVideoList([])
    setCurrentVideoList(generateCurrentVideoList(page))
  }, [status.linkMicUserCount])

  useEffect(() => {
    setTimeout(() => {
      subscribeUser()
    }, 600)
  }, [status.layout])

  return (
    <div className={`${styles['sub-video-bar']} ${!status.linkMicBarExpand ? styles['hide-sub-video-bar'] : ''}`}>
      <div className={`${styles.prev} ${styles.arrow}`} onClick={() => changePage('prev')}>
        <svg className="icon" aria-hidden="true">
          <use xlinkHref="#icon-ic_toolbar_arrow"></use>
        </svg>
      </div>
      <div className={`${styles.next} ${styles.arrow}`} onClick={() => changePage('next')}>
        <svg className="icon" aria-hidden="true">
          <use xlinkHref="#icon-ic_toolbar_arrow"></use>
        </svg>
      </div>
      {userList.length > 0 && (
        <div className={styles['sub-video-group']}>
          {currentVideoList.map((item) => {
            if (item.userId === user.userId) {
              return <VideoItem isMe={true} key={item.userId} />
            } else {
              return <VideoItem item={user.userList[item.userId]} key={item.userId} />
            }
          })}
        </div>
      )}
    </div>
  )
}

export default connect(
  ({ room, status, user }: { room: RoomModelState; status: StatusModelState; user: UserModelState }) => ({
    room,
    status,
    user,
  }),
)(SpeakerStu)
