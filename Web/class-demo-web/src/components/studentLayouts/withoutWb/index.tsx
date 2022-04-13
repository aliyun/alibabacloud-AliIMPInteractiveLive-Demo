import { FC, useEffect, useState, useRef } from 'react'
import { RoomModelState, StatusModelState, connect, Dispatch } from 'umi'
import Chat from '@/components/chat'
import Player from '@/components/player'
import TeacherVideo from '@/components/videoLayout/teacherVideo'
import Grid from '@/components/videoLayout/grid/grid'
import LinkmicBarStu from '@/components/videoLayout/components/linkmicBar/linkmicBarStu'
import commonStyles from '../common.less'
import styles from './index.less'

interface PageProps {
  room: RoomModelState
  status: StatusModelState
  dispatch: Dispatch
}

const Student: FC<PageProps> = ({ room, status, dispatch }) => {
  const [tab, setTab] = useState('chat')
  const [hoverdNotice, setHoverdNotice] = useState(false)

  const startPlay = () => {
    // 解决不点击就没法自动播放的问题
    if (status.isLiving) {
      window.liveService.startPlay()
    }
  }

  const linkMicExpand = () => {
    dispatch({
      type: 'status/setLinkMicBarExpand',
      payload: !status.linkMicBarExpand,
    })
  }

  useEffect(() => {
    if (status.viewMode === 'whiteBoard') {
      dispatch({ type: 'status/setLayout', payload: '6' })
    }
  }, [status.viewMode])
  return (
    <>
      <div className={commonStyles['linkmic-bar-container']}>
        {status.isInChannel && status.layout === '6' && <LinkmicBarStu />}
        {status.isInChannel && status.layout === '6' && (
          <div className={commonStyles['expander']} onClick={linkMicExpand}>
            {status.linkMicBarExpand ? (
              <svg aria-hidden="true" className="icon">
                <use xlinkHref="#icon-xiangyoufanyesvg"></use>
              </svg>
            ) : (
              <svg aria-hidden="true" className="icon">
                <use xlinkHref="#icon-xiangzuofanye"></use>
              </svg>
            )}
          </div>
        )}
      </div>
      <div className={commonStyles['page-main']} onClick={startPlay}>
        {status.isInChannel ? status.layout === '9' ? <Grid /> : <TeacherVideo /> : <Player />}
      </div>
      <aside className={`${commonStyles.sidebar} ${styles.sidebar}`}>
        {room.notice && (
          <div
            className={`${commonStyles['notice-bar-container']} ${hoverdNotice ? commonStyles['notice-hoverd'] : ''}`}
            onMouseLeave={() => setHoverdNotice(false)}
          >
            <div className={commonStyles['notice-inner']}>
              【公告】{room.notice}
              <svg className="icon" aria-hidden="true" onMouseOver={() => setHoverdNotice(true)}>
                <use xlinkHref="#icon-ic_toolbar_arrow_noc"></use>
              </svg>
            </div>
          </div>
        )}
        <div className={commonStyles['tab-container']}>
          <div
            className={`${commonStyles['tab-item']} ${tab === 'chat' ? commonStyles['tab-item-active'] : ''}`}
            onClick={() => setTab('chat')}
          >
            <span>聊天</span>
          </div>
          {/* <div className={`${commonStyles['tab-item']} ${tab === 'user' ? commonStyles['tab-item-active'] : ''}`}>
            <span>课件</span>
          </div> */}
        </div>
        {tab === 'chat' ? <Chat from="student" /> : <div></div>}
      </aside>
    </>
  )
}

export default connect(({ room, status }: { room: RoomModelState; status: StatusModelState }) => ({
  room,
  status,
}))(Student)
