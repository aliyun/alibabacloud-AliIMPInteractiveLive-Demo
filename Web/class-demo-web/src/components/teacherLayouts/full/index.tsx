import { FC, useEffect, useState } from 'react'
import { RoomModelState, StatusModelState, UserModelState, connect, Dispatch } from 'umi'
import Emitter from '@/utils/emitter'
import OperationBar from '@/components/operationBar'
import Chat from '@/components/chat'
import WhiteBoard from '@/components/whiteBoard'
import Preview from '@/components/videoLayout/preview'
import Grid from '@/components/videoLayout/grid/grid'
import LinkmicBar from '@/components/videoLayout/components/linkmicBar/linkmicBar'
import UserList from '@/components/userList'
import commonStyles from '../common.less'

interface PageProps {
  room: RoomModelState
  status: StatusModelState
  user: UserModelState
  dispatch: Dispatch
}

const emitter = Emitter.getInstance()

const Teacher: FC<PageProps> = ({ room, status, user, dispatch }) => {
  const [tab, setTab] = useState('chat')
  const [hoverdNotice, setHoverdNotice] = useState(false)

  const switchViewMode = () => {
    if (status.viewMode === 'whiteBoard') {
      dispatch({ type: 'status/setLayout', payload: '6' })
    }
    const mode = status.viewMode === 'whiteBoard' ? 'video' : 'whiteBoard'
    dispatch({ type: 'status/setViewMode', payload: mode })
  }

  const switchToUser = () => {
    setTab('user')
  }

  const linkMicExpand = () => {
    dispatch({
      type: 'status/setLinkMicBarExpand',
      payload: !status.linkMicBarExpand,
    })
  }

  const isSomeoneInSeat = () => {
    return (
      Object.values(user.userList).filter((item) => item.isInSeat && !(item.userId === room.ownerId) && !item.isMe)
        .length > 0
    )
  }

  useEffect(() => {
    if (status.viewMode === 'whiteBoard') {
      dispatch({ type: 'status/setLayout', payload: '6' })
    }
  }, [status.viewMode])

  return (
    <>
      <div className={commonStyles['linkmic-bar-container']}>
        {isSomeoneInSeat() && status.layout === '6' && <LinkmicBar />}
        {isSomeoneInSeat() && status.layout === '6' && (
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
          <div className={commonStyles['abbr-type']}>课件展示</div>
          <div className={commonStyles['abbr-operation-bar']}>
            <div className={commonStyles['operation-item']} onClick={switchViewMode}>
              <svg className="icon" aria-hidden="true">
                <use xlinkHref="#icon-ic_qiehuanshitu"></use>
              </svg>
            </div>
          </div>
        </div>
        {room.docKey && (
          <WhiteBoard
            boardType={status.viewMode === 'whiteBoard' ? 'full' : 'pure'}
            linking={isSomeoneInSeat() && status.linkMicBarExpand}
          />
        )}
      </div>
      <div className={`${status.viewMode === 'video' ? commonStyles['page-main'] : commonStyles.abbreviate}`}>
        <div
          className={commonStyles['abbreviate-container']}
          style={{ display: status.viewMode === 'video' ? 'none' : 'block' }}
        >
          <div className={commonStyles['abbr-type']}>教师(我)</div>
          <div className={commonStyles['abbr-operation-bar']}>
            <div className={commonStyles['operation-item']} onClick={switchViewMode}>
              <svg className="icon" aria-hidden="true">
                <use xlinkHref="#icon-ic_qiehuanshitu"></use>
              </svg>
            </div>
            <div className={commonStyles['operation-item']}>
              <svg className="icon" aria-hidden="true">
                <use
                  xlinkHref={
                    status.micAvailable ? '#icon-ic_toolbar_jingyin_white' : '#icon-ic_toolbar_quxiaojingyin_white'
                  }
                ></use>
              </svg>
            </div>
          </div>
        </div>
        {status.layout === '6' ? <Preview /> : <Grid />}
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
          <div
            className={`${commonStyles['tab-item']} ${tab === 'user' ? commonStyles['tab-item-active'] : ''}`}
            onClick={switchToUser}
          >
            <span>成员（{Object.keys(user.userList).length}）</span>
          </div>
        </div>
        {tab === 'chat' ? <Chat from="teacher" /> : <UserList />}
      </aside>
      <div className={commonStyles['page-footer']}>
        <div className={commonStyles['footer-main']}>
          <OperationBar />
        </div>
        <div className={commonStyles.holder}></div>
      </div>
      {status.isRecording && status.isInClass && (
        <div className={commonStyles.recording}>
          <span></span>教师录制中
        </div>
      )}
    </>
  )
}

export default connect(
  ({ room, status, user }: { room: RoomModelState; status: StatusModelState; user: UserModelState }) => ({
    room,
    status,
    user,
  }),
)(Teacher)
