import { FC, useEffect } from 'react'
import { RoomModelState, StatusModelState, UserModelState, connect } from 'umi'
import { usePersistFn } from 'ahooks'
import Emitter from '@/utils/emitter'
import styles from './index.less'

const emitter = Emitter.getInstance()

interface PageProps {
  room: RoomModelState
  status: StatusModelState
  user: UserModelState
}

const Speaker: FC<PageProps> = ({ room, status, user }) => {
  const setVideo = usePersistFn(() => {
    window.rtcService.startRtcPreview(document.getElementById('preview'))
  })

  useEffect(() => {
    if (!window.rtcService) return
    window.rtcService.startRtcPreview(document.getElementById('preview'))
    emitter.on('needSubscribe', setVideo)
    return () => {
      window.rtcService.stopRtcPreview(document.getElementById('preview'))
      emitter.remove('needSubscribe', setVideo)
    }
  }, [])

  return (
    <div
      className={`${styles['preview-main']} ${
        status.viewMode === 'whiteBoard' ||
        Object.values(user.userList).filter((item) => item.isInSeat && !(item.userId === room.ownerId) && !item.isMe)
          .length === 0 ||
        !status.linkMicBarExpand
          ? styles['full-preview-main']
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
  )
}

export default connect(
  ({ room, status, user }: { room: RoomModelState; status: StatusModelState; user: UserModelState }) => ({
    room,
    status,
    user,
  }),
)(Speaker)
