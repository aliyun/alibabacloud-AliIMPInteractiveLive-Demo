import { FC, useEffect, useState, useRef } from 'react'
import { RoomModelState, StatusModelState, UserModelState, connect, Dispatch } from 'umi'
import { useMount, useUnmount, usePersistFn } from 'ahooks'
import { User } from '@/models/user'
import VideoItem from '../components/videoItem'
import Emitter from '@/utils/emitter'
import styles from './grid.less'

const emitter = Emitter.getInstance()

interface PageProps {
  room: RoomModelState
  status: StatusModelState
  user: UserModelState
  dispatch: Dispatch
}

const Grid: FC<PageProps> = ({ room, status, user, dispatch }) => {
  const containerEl = useRef<HTMLDivElement>(null)
  const [currentVideoList, setCurrentVideoList] = useState<User[]>([])
  const [basis, setBasis] = useState('100%')

  const setVideo = usePersistFn(() => {
    if (!window.rtcService) return
    const list = Object.values(user.userList).filter((item) => item.isInSeat && !item.isMe)
    const nowList = list.slice(0, 8)
    nowList.unshift({
      userId: user.userId,
      nick: user.nick,
      isOwner: false,
      isMe: true,
      isInSeat: true,
      isApplying: false,
      isRtcMute: !status.micAvailable,
      isRtcMuteCamera: !status.cameraAvailable,
      isInviting: false,
      streamType: 1,
      subscribeResult: true,
    })
    setCurrentVideoList(nowList)
    if (nowList.length === 1) {
      setBasis('100%')
    } else if (nowList.length > 4) {
      setBasis('31%')
    } else {
      setBasis('48%')
    }
    setTimeout(() => {
      nowList.forEach((item) => {
        window.rtcService.setDisplayRemoteVideo(
          document.getElementById(`video-${item.userId}`),
          item.userId,
          item.streamType || 1,
        )
      })
      window.rtcService.startRtcPreview(document.getElementById('preview'))
    }, 100)
    if (!status.isInClass || status.isScreenSharing) return
    if (nowList.length > 0) {
      const list = nowList.map((item) => item.userId).slice(0, 9)
      window.rtcService.setLayout(list, 3)
    } else {
      window.rtcService.setLayout([user.userId], 1)
    }
  })

  useMount(() => {
    if (!window.rtcService) return
    emitter.on('needSetVideo', setVideo)
    setVideo()
  })

  useUnmount(() => {
    if (!window.rtcService) return
    window.rtcService.stopRtcPreview()
    emitter.remove('needSetVideo', setVideo)
  })

  return (
    <div className={styles['grid-container']} ref={containerEl}>
      {currentVideoList.map((item, index) => {
        if (item.isMe) {
          return <VideoItem isMe grid key={index} gridStyle={{ flexBasis: basis }} />
        } else {
          return <VideoItem item={item} grid key={index} gridStyle={{ flexBasis: basis }} />
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
