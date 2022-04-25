import { FC, useEffect, useState } from 'react'
import { RoomModelState, StatusModelState, connect, Dispatch } from 'umi'
import Emitter from '@/utils/emitter'
import Chat from '@/components/chat'
import WhiteBoard from '@/components/whiteBoard'
import Player from '@/components/player'
import TeacherVideo from '@/components/videoLayout/teacherVideo'
import Grid from '@/components/videoLayout/grid/grid'
import LinkmicBarStu from '@/components/videoLayout/components/linkmicBar/linkmicBarStu'
import commonStyles from '../common.less'
import styles from './index.less'

const emitter = Emitter.getInstance()

interface PageProps {
  room: RoomModelState
  status: StatusModelState
  dispatch: Dispatch
}

const Student: FC<PageProps> = ({ room, status, dispatch }) => {
  const [tab, setTab] = useState('chat')
  const [hoverdNotice, setHoverdNotice] = useState(false)

  const switchViewMode = () => {
    if (!dispatch) return
    if (status.viewMode === 'whiteBoard' && status.layout === '9') {
      dispatch({ type: 'status/setLayout', payload: '6' })
    }
    const mode = status.viewMode === 'whiteBoard' ? 'video' : 'whiteBoard'
    dispatch({ type: 'status/setViewMode', payload: mode })
  }

  const linkMicExpand = () => {
    dispatch({
      type: 'status/setLinkMicBarExpand',
      payload: !status.linkMicBarExpand,
    })
  }

  const startPlay = () => {
    // 解决不点击就没法自动播放的问题
    if (status.isLiving && !window.liveService._isLiving) {
      window.liveService.startPlay()
    }
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
      <div className={`${status.viewMode === 'whiteBoard' ? commonStyles['page-main'] : commonStyles.abbreviate}`}>
        <div
          className={commonStyles['abbreviate-container']}
          style={{
            display: status.viewMode === 'whiteBoard' ? 'none' : 'block',
          }}
        >
          <div className={commonStyles['abbr-type']}>讲解画面</div>
          <div className={commonStyles['abbr-operation-bar']}>
            {status.isInClass && (
              <div className={commonStyles['operation-item']} onClick={switchViewMode}>
                <svg className="icon" aria-hidden="true">
                  <use xlinkHref="#icon-ic_qiehuanshitu"></use>
                </svg>
              </div>
            )}
            {/* <div className={commonStyles['operation-item']}>
              <svg className="icon" aria-hidden="true">
                <use
                  xlinkHref={
                    status.teacherMicAvailable ? '#icon-ic_toolbar_jingyin_white' : '#icon-ic_toolbar_quxiaojingyin_white'
                  }
                ></use>
              </svg>
            </div> */}
          </div>
        </div>
        {room.docKey && status.isInClass ? (
          <WhiteBoard
            boardType={status.viewMode === 'whiteBoard' ? 'full' : 'pure'}
            linking={status.isInChannel && status.linkMicBarExpand}
          />
        ) : (
          <div className={styles.beforeclass}></div>
        )}
      </div>
      <div
        className={`${status.viewMode === 'video' ? commonStyles['page-main'] : commonStyles.abbreviate}`}
        onClick={startPlay}
      >
        <div
          className={commonStyles['abbreviate-container']}
          style={{ display: status.viewMode === 'video' ? 'none' : 'block' }}
        >
          <div className={commonStyles['abbr-type']}>教师画面</div>
          <div className={commonStyles['abbr-operation-bar']}>
            {status.isInClass && (
              <div className={commonStyles['operation-item']} onClick={switchViewMode}>
                <svg className="icon" aria-hidden="true">
                  <use xlinkHref="#icon-ic_qiehuanshitu"></use>
                </svg>
              </div>
            )}
            {/* <div className={commonStyles['operation-item']}>
              <svg className="icon" aria-hidden="true">
                <use
                  xlinkHref={
                    status.teacherMicAvailable ? '#icon-ic_toolbar_jingyin_white' : '#icon-ic_toolbar_quxiaojingyin_white'
                  }
                ></use>
              </svg>
            </div> */}
          </div>
        </div>
        {status.isInChannel ? status.layout === '9' ? <Grid /> : <TeacherVideo /> : <Player />}
      </div>
      <aside className={commonStyles.sidebar}>
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
