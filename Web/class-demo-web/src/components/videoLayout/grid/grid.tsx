import { FC, useEffect, useState, useRef } from 'react'
import { RoomModelState, StatusModelState, UserModelState, connect, Dispatch } from 'umi'
import { usePersistFn } from 'ahooks'
import VideoItem from '../components/videoItem'
import Emitter from '@/utils/emitter'
import styles from './grid.less'
import { RtcUserInfo, StreamConfigInfo } from '../components/linkmicBar/linkmicBar'

const emitter = Emitter.getInstance()
const { EventNameEnum } = window.RoomPaasSdk

interface PageProps {
  room: RoomModelState
  status: StatusModelState
  user: UserModelState
  dispatch: Dispatch
}

const Grid: FC<PageProps> = ({ room, status, user, dispatch }) => {
  const containerEl = useRef<HTMLDivElement>(null)
  const [currentVideoList, setCurrentVideoList] = useState<RtcUserInfo[]>([])
  const [basis, setBasis] = useState('100%')
  const [videoHeight, setVideoHeight] = useState(200)
  const meRtc: RtcUserInfo = {
    userId: user.userId,
    displayName: user.userId,
    muteAudioPlaying: false,
    streamConfigs: [],
  }

  const getUserList = (): RtcUserInfo[] => {
    const list: RtcUserInfo[] = window.rtcService
      .getUserList()
      .sort((item: any) => (item.userId === room.ownerId ? -1 : 1))
    if (room.ownerId === user.userId) {
      list.unshift(meRtc)
      return list
    } else {
      const newList = list.sort((item) => (item.userId === room.ownerId ? -1 : 1))
      newList.splice(1, 0, meRtc)
      return newList
    }
  }

  const setGridLayout = (nowList: any[]) => {
    setCurrentVideoList([])
    setCurrentVideoList(nowList)
    if (nowList.length === 1) {
      setBasis('100%')
    } else if (nowList.length > 4) {
      setBasis('31%')
    } else {
      setBasis('48%')
    }
    setTimeout(() => {
      const previewEl = document.getElementById('preview')
      if (previewEl) {
        window.rtcService.startRtcPreview(document.getElementById('preview'))
        const videoWidth = previewEl.clientWidth
        setVideoHeight((videoWidth * 9) / 16)
      }
      if (!status.isInClass || status.isScreenSharing) return
      if (room.isOwner) {
        setLayout(nowList)
      }
    }, 200)
  }

  const setVideo = usePersistFn(() => {
    if (!window.rtcService) return
    const list = getUserList()
    const nowList = list.slice(0, 9)
    setGridLayout(nowList)
    setTimeout(() => {
      list.forEach((item) => {
        console.log('grid开始订阅', item.displayName)
        doSubscribe(item.userId, item)
      })
    }, 100)
  })

  const doSubscribe = async (userId: string, userInfo: RtcUserInfo) => {
    // 获取用户rtc信息
    const confUserInfo = userInfo || getUserList().find((item) => item.userId === userId)
    console.log('订阅：', confUserInfo)
    if (!confUserInfo) return
    // 如果没有任何流则不做操作
    if (confUserInfo.streamConfigs.length === 0 && userId !== user.userId) return
    let stream = 1
    const streamInfo = (user.userList[userId] && user.userList[userId].streamInfo) || null
    const audioStreamConfig = confUserInfo.streamConfigs.find((item) => item.label === 'sophon_audio')
    const cameraStreamConfig = confUserInfo.streamConfigs.find((item) => item.label === 'sophon_video_camera_large')
    const isCurrent = getUserList()
      .slice(0, 9)
      .some((i) => i.userId === userId)
    try {
      if (userId === user.userId) {
        return
      }
      if (isCurrent) {
        const screenShareStreamConfig = confUserInfo.streamConfigs.find(
          (item) => item.label === 'sophon_video_screen_share',
        )
        if (screenShareStreamConfig && screenShareStreamConfig.state === 'active') {
          if (streamInfo && screenShareStreamConfig.subscribed && !streamInfo.screen) {
            await window.rtcService.unSubscribe(userId)
            screenShareStreamConfig.subscribed = false
          }
          !screenShareStreamConfig.subscribed && (await window.rtcService.subscribeScreen(userId))
          console.log('grid订阅了', confUserInfo.displayName, 'screen流')
          stream = 2
        } else if (cameraStreamConfig && cameraStreamConfig.state === 'active') {
          if (streamInfo && cameraStreamConfig.subscribed && !streamInfo.camera) {
            await window.rtcService.unSubscribe(userId)
            cameraStreamConfig.subscribed = false
          }
          !cameraStreamConfig.subscribed && (await window.rtcService.subscribeCamera(userId))
          console.log('grid订阅了', confUserInfo.displayName, 'camera流')
        } else if (audioStreamConfig && audioStreamConfig.state === 'active') {
          if (streamInfo && audioStreamConfig.subscribed && !streamInfo.audio) {
            await window.rtcService.unSubscribe(userId)
            audioStreamConfig.subscribed = false
          }
          !audioStreamConfig.subscribed && (await window.rtcService.subscribeAudio(userId))
          console.log('grid订阅了', confUserInfo.displayName, 'audio流')
        } else {
          console.log(confUserInfo.displayName, '没有任何流')
        }
        setTimeout(() => {
          console.log('订阅时set了', confUserInfo.displayName)
          window.rtcService.setDisplayRemoteVideo(document.getElementById(`video-${userId}`), userId, stream)
        }, 200)
      } else {
        await window.rtcService.subscribeAudio(userId)
        console.log('grid静默订阅了', confUserInfo.displayName, 'audio流')
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
    setTimeout(() => {
      console.log('onPublisher开始订阅')
      doSubscribe(info.userId, info)
    }, 500)
  })

  useEffect(() => {
    setGridLayout(getUserList().slice(0, 9))
  }, [status.linkMicUserCount])

  const setLayout = (nowList: RtcUserInfo[]) => {
    if (nowList.length > 1) {
      const list = nowList.map((item) => item.userId).slice(0, 9)
      window.rtcService.setLayout(list, 3)
    } else {
      window.rtcService.setLayout([user.userId], 1)
    }
  }

  useEffect(() => {
    if (!window.rtcService) return
    emitter.on('needSubscribe', setVideo)
    window.rtcService.on(EventNameEnum.onPublisher, onPublisherHandler)
    setVideo()
    return () => {
      if (!window.rtcService) return
      window.rtcService.stopRtcPreview()
      emitter.remove('needSubscribe', setVideo)
      window.rtcService.remove(EventNameEnum.onPublisher, onPublisherHandler)
    }
  }, [])

  return (
    <div className={styles['grid-container']} ref={containerEl}>
      {currentVideoList.map((item) => {
        if (item.userId === user.userId) {
          return <VideoItem isMe grid key="preview" gridStyle={{ flexBasis: basis, height: videoHeight + 'px' }} />
        } else {
          return (
            <VideoItem
              item={user.userList[item.userId]}
              grid
              key={item.userId}
              gridStyle={{ flexBasis: basis, height: videoHeight + 'px' }}
            />
          )
        }
      })}
    </div>
  )
}

export default connect(
  ({ room, status, user }: { room: RoomModelState; status: StatusModelState; user: UserModelState }) => ({
    room,
    status,
    user,
  }),
)(Grid)
