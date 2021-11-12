import { FC, useEffect, useState, useRef } from 'react'
import { RoomModelState, StatusModelState, UserModelState, connect, Dispatch } from 'umi'
import { useMount, useUnmount, usePersistFn } from 'ahooks'
import { User } from '@/models/user'
import VideoItem from '../components/videoItem'
import Emitter from '@/utils/emitter'
import styles from './speaker.less'

const emitter = Emitter.getInstance()

interface PageProps {
  room: RoomModelState
  status: StatusModelState
  user: UserModelState
  dispatch: Dispatch
}

const Speaker: FC<PageProps> = ({ room, status, user, dispatch }) => {
  const containerEl = useRef<HTMLDivElement>(null)
  const [page, setPage] = useState(0)
  const [userList, setUserList] = useState<User[]>([])
  const [currentVideoList, setCurrentVideoList] = useState<User[]>([])

  const changePage = (to: 'prev' | 'next') => {
    const list = Object.values(user.userList).filter((item) => item.isInSeat && !(item.userId === room.ownerId))
    const size = Math.floor(list.length / 6)
    if (to === 'prev') {
      if (page <= 0) return
      setPage(page - 1)
    }
    if (to === 'next') {
      if (page >= size) return
      setPage(page + 1)
    }
    setTimeout(() => {
      setVideo()
    }, 0)
  }

  const generateCurrentVideoList = (page: number) => {
    const list = Object.values(user.userList).filter((item) => item.isInSeat && !(item.userId === room.ownerId))
    setUserList(list)
    return list.slice(page * 6, (page + 1) * 6)
  }

  const setVideo = usePersistFn(() => {
    if (!window.rtcService) return
    const videoList = generateCurrentVideoList(page)
    setCurrentVideoList(videoList)
    console.log(videoList)
    const list = Object.values(user.userList).filter(
      (item) => item.isInSeat && !(item.userId === room.ownerId) && !item.isMe,
    )
    const nowList = list.slice(page * 6, (page + 1) * 6)
    setTimeout(() => {
      nowList.forEach((item) => {
        window.rtcService.setDisplayRemoteVideo(
          document.getElementById(`video-${item.userId}`),
          item.userId,
          item.streamType || 1,
        )
      })
    }, 100)
    window.rtcService.startRtcPreview(document.getElementById('preview'))
    if (!status.isInClass || status.isScreenSharing) return
    if (nowList.length > 0) {
      const list = new Array(5).fill('')
      for (let i = 0; i < 4; i++) {
        if (nowList[i]) list[i] = nowList[i].userId
      }
      list[4] = user.userId
      window.rtcService.setLayout(list, 2)
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
    <div className={styles['speaker-container']} ref={containerEl}>
      <div
        className={`${styles['sub-video-bar']} ${
          status.viewMode === 'whiteBoard' ||
          Object.values(user.userList).filter((item) => item.isInSeat && !(item.userId === room.ownerId) && !item.isMe)
            .length === 0
            ? styles['hide-sub-video-bar']
            : ''
        }`}
      >
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
            {currentVideoList.map((item, index) => (
              <VideoItem item={item} key={index} />
            ))}
          </div>
        )}
      </div>
      <div
        className={`${styles['speaker-main']} ${
          status.viewMode === 'whiteBoard' ||
          Object.values(user.userList).filter((item) => item.isInSeat && !(item.userId === room.ownerId) && !item.isMe)
            .length === 0
            ? styles['full-speaker-main']
            : ''
        }`}
      >
        {!status.cameraAvailable && (
          <div className={styles['camera-unavailable']}>
            <svg className="icon" aria-hidden="true">
              <use xlinkHref="#icon-ic_toolbar_guanshexiangtou_noc"></use>
            </svg>
          </div>
        )}
        <video id="preview" className={styles.preview}></video>
      </div>
    </div>
  )
}

export default connect(
  ({ room, status, user }: { room: RoomModelState; status: StatusModelState; user: UserModelState }) => ({
    room,
    status,
    user,
  }),
)(Speaker)
