import { FC, useState, useRef } from 'react'
import { StatusModelState, connect } from 'umi'
import { User } from '@/models/user'
import { reSubUser } from '@/biz/link'
import styles from './videoItem.less'

interface PageProps {
  status: StatusModelState
  item?: User
  isMe?: boolean
  grid?: boolean
  gridStyle?: any
}

const VideoLayout: FC<PageProps> = ({ status, item, isMe, grid, gridStyle }) => {
  const [hover, setHover] = useState(false)
  const resubTimer = useRef<any>(null)
  const resub = (userId: string) => {
    clearTimeout(resubTimer.current)
    resubTimer.current = setTimeout(() => {
      reSubUser(userId, false)
    }, 1000)
  }
  return (
    <div
      className={`${styles['sub-video-item']} ${grid ? styles['grid'] : ''}`}
      style={gridStyle}
      onMouseOver={() => setHover(true)}
      onMouseLeave={() => setHover(false)}
    >
      {((isMe && !status.cameraAvailable) || (!isMe && item && item.isRtcMuteCamera)) && (
        <div className={styles['camera-unavailable']}>
          <svg className="icon" aria-hidden="true">
            <use xlinkHref="#icon-ic_toolbar_guanshexiangtou_noc"></use>
          </svg>
        </div>
      )}
      <div className={styles['info-bar']}>
        <div className={styles['operation-item-container-left']}>
          <div className={`${styles['operation-item']} ${styles['nick']}`}>
            {isMe ? '我' : item ? item.nick : '学生'}
          </div>
        </div>
        <div className={styles['operation-item-container-right']}>
          {!isMe && (
            <div
              className={`${styles['operation-item']} ${hover ? '' : styles['hover-item']}`}
              onClick={() => resub(item?.userId || '')}
            >
              <svg className="icon" aria-hidden="true">
                <use xlinkHref="#icon-ic_kejian_shuaxin"></use>
              </svg>
            </div>
          )}
          <div className={styles['operation-item']}>
            <svg className="icon" aria-hidden="true">
              <use
                xlinkHref={
                  (isMe && !status.micAvailable) || (!isMe && item && item.isRtcMute)
                    ? '#icon-ic_toolbar_quxiaojingyin_white'
                    : '#icon-ic_toolbar_jingyin_white'
                }
              ></use>
            </svg>
          </div>
        </div>
      </div>
      <video id={isMe ? 'preview' : `video-${item && item.userId}`}></video>
    </div>
  )
}

export default connect(({ status }: { status: StatusModelState }) => ({
  status,
}))(VideoLayout)
