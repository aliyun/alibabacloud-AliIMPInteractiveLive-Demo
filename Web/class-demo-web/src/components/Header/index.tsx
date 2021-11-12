import { FC, useEffect, useState, useRef, useCallback } from 'react'
import { useHistory } from 'react-router'
import { RoomModelState, StatusModelState, connect, Dispatch } from 'umi'
import { useInterval, useClickAway } from 'ahooks'
import { message } from 'antd'
import { addZero } from '@/utils'
import Clipboard from 'clipboard'
import styles from './index.less'

interface PageProps {
  room: RoomModelState
  status: StatusModelState
  from: 'student' | 'teacher'
  dispatch: Dispatch
}

const layoutList = [
  {
    layout: '9',
    icon: '2',
    intro: '宫格模式',
    show: true,
  },
  {
    layout: '6',
    icon: '3',
    intro: '主讲模式',
    show: true,
  },
]

const Header: FC<PageProps> = ({ room, status, dispatch, from }) => {
  const [interval, setInterval] = useState<number | null>(null)
  const [showRoomDetail, setShowRoomDetail] = useState(false)
  const [showSetLayout, setShowSetLayout] = useState(false)
  const [showSetting, setShowSetting] = useState(false)

  const roomDetailRef = useRef<HTMLDivElement>(null)
  const roomDetailBtnRef = useRef<HTMLDivElement>(null)
  const setLayoutRef = useRef<HTMLDivElement>(null)
  const setLayoutBtnRef = useRef<HTMLDivElement>(null)
  const settingRef = useRef<HTMLDivElement>(null)
  const settingBtnRef = useRef<HTMLDivElement>(null)

  const clipboardRef = useRef(new Clipboard('.copy-title'))

  const history = useHistory()

  const setLayout = (layout: string) => {
    if (status.viewMode === 'whiteBoard') return
    dispatch({
      type: 'status/setLayout',
      payload: layout,
    })
    message.success(`已切换为${layoutList.find((item) => item.layout === layout)?.intro}`)
  }
  const setAutoRecord = () => {
    dispatch({
      type: 'status/setIsAutoRecord',
      payload: !status.isAutoRecord,
    })
  }
  const copyTitleHandler = useCallback(() => {
    message.success('已复制到剪贴板')
  }, [])
  const leaveRoom = () => {
    history.replace('/')
    message.success('已离开教室')
  }
  const startInterval = () => {
    const now = +new Date()
    let startDuration = 0
    if (room.classStartTime > 0) {
      startDuration = (now - room.classStartTime) / 1000
    }
    dispatch({
      type: 'room/updateClassTime',
      payload: startDuration,
    })
    setInterval(1000)
  }
  const clearInterval = () => {
    setInterval(null)
  }
  const formatTime = (time: number) => {
    const s = Math.floor(time % 60)
    const m = Math.floor((time / 60) % 60)
    const h = Math.floor(time / 3600)
    return `${addZero(h)}:${addZero(m)}:${addZero(s)}`
  }
  useInterval(() => {
    dispatch({
      type: 'room/updateClassTime',
      payload: room.classDuration + 1,
    })
  }, interval)
  useClickAway(() => {
    showRoomDetail && setShowRoomDetail(false)
  }, [roomDetailRef, roomDetailBtnRef])
  useClickAway(() => {
    showSetLayout && setShowSetLayout(false)
  }, [setLayoutRef, setLayoutBtnRef])
  useClickAway(() => {
    showSetting && setShowSetting(false)
  }, [settingRef, settingBtnRef])
  useEffect(() => {
    status.isInClass ? startInterval() : clearInterval()
  }, [status.isInClass])
  useEffect(() => {
    clipboardRef.current.on('success', copyTitleHandler)
    return () => {
      clipboardRef.current.destroy()
    }
  }, [clipboardRef])

  return (
    <header className={styles.header}>
      <div className={styles['topbar-left']}>
        <div className={styles['title-container']}>
          <h1 className={styles.title}>
            <img src="https://img.alicdn.com/imgextra/i1/O1CN01mRetS31eoUUxwnc2E_!!6000000003918-55-tps-40-40.svg" />
            {room.title.length > 22 ? room.title.substr(0, 22) + '...' : room.title}
          </h1>
          <div className={styles.spread} onClick={() => setShowRoomDetail(!showRoomDetail)} ref={roomDetailBtnRef}>
            <svg className="icon" aria-hidden="true">
              <use xlinkHref="#icon-ic_toolbar_arrow"></use>
            </svg>
          </div>
          {showRoomDetail && (
            <div className={styles.popover} ref={roomDetailRef}>
              <div className={styles['class-detail-container']}>
                <div className={styles['class-detail']}>
                  <div className={styles['class-title']}>{room.title}</div>
                  <div className={styles.info}>课堂号：{room.classId}</div>
                  {/* <div className={styles.info}>
                    链接：{window.location.href}
                  </div> */}
                </div>
                <div className={`${styles.copy} copy-title`} data-clipboard-text={room.classId}>
                  <span>复制</span>
                </div>
              </div>
            </div>
          )}
        </div>
        <div className={styles['class-status']}>
          <div className={styles.ring}>
            {status.isInClass ? (
              <svg className="icon" aria-hidden="true">
                <use xlinkHref="#icon-a-ic_header_inclass"></use>
              </svg>
            ) : (
              <svg className="icon" aria-hidden="true">
                <use xlinkHref="#icon-a-ic_header_noclass"></use>
              </svg>
            )}
          </div>
          <span className={styles.description}>{status.isInClass ? formatTime(room.classDuration) : '课程未开始'}</span>
        </div>
      </div>
      <div className={styles['topbar-right']}>
        {from === 'teacher' ? (
          <div className={styles['operation-container']}>
            <div className={`${styles.item} ${showSetLayout ? styles.active : ''}`} ref={setLayoutBtnRef}>
              <div
                className={styles['inner-icon']}
                onClick={() => status.viewMode === 'video' && setShowSetLayout(!showSetLayout)}
              >
                <svg className="icon" aria-hidden="true">
                  <use
                    xlinkHref={`#icon-ic_header_shitu${
                      layoutList.find((item) => item.layout === status.layout)?.icon
                    }_normal`}
                  ></use>
                </svg>
                <svg className="icon" aria-hidden="true">
                  <use xlinkHref="#icon-ic_toolbar_arrow"></use>
                </svg>
              </div>
              {showSetLayout && (
                <div className={styles.popover} style={{ left: 'unset', right: 0 }} ref={setLayoutRef}>
                  <div className={styles['radio-container']}>
                    {layoutList
                      .filter((item) => item.show)
                      .map((item: any, index: number) => (
                        <div
                          className={`${styles['radio-item']} ${
                            status.layout === item.layout ? styles['radio-item-selected'] : ''
                          }`}
                          onClick={() => setLayout(item.layout)}
                          key={index}
                        >
                          <svg className="icon" aria-hidden="true">
                            <use xlinkHref={`#icon-ic_header_shitu${item.icon}_noc`}></use>
                          </svg>
                        </div>
                      ))}
                  </div>
                </div>
              )}
            </div>
            <div className={styles.item} ref={settingBtnRef}>
              <div className={styles['inner-icon']} onClick={() => setShowSetting(!showSetting)}>
                <svg className="icon" aria-hidden="true">
                  <use xlinkHref="#icon-ic_header_set"></use>
                </svg>
              </div>
              {showSetting && (
                <div className={styles.popover} style={{ left: 'unset', right: 0 }} ref={settingRef}>
                  <div className={styles['selector-container']}>
                    <div
                      className={`${styles['selector-item']} ${
                        status.isAutoRecord ? styles['selector-item-selected'] : ''
                      }`}
                      onClick={setAutoRecord}
                    >
                      课程开始是否自动开启录制
                      <div className={styles.check}>
                        {status.isAutoRecord && (
                          <svg className="icon" aria-hidden="true">
                            <use xlinkHref="#icon-ic_header_tick"></use>
                          </svg>
                        )}
                      </div>
                    </div>
                  </div>
                </div>
              )}
            </div>
            <div className={styles.item} ref={settingBtnRef}>
              <div className={styles['inner-icon']} onClick={leaveRoom}>
                <svg className="icon" aria-hidden="true">
                  <use xlinkHref="#icon-xueshengduan-laoshilikaifangjian"></use>
                </svg>
              </div>
            </div>
          </div>
        ) : (
          <div className={styles['operation-container']}>
            <div className={`${styles.item} ${showSetLayout ? styles.active : ''}`} ref={setLayoutBtnRef}>
              <div
                className={styles['inner-icon']}
                onClick={() => status.viewMode === 'video' && setShowSetLayout(!showSetLayout)}
              >
                <svg className="icon" aria-hidden="true">
                  <use
                    xlinkHref={`#icon-ic_header_shitu${
                      layoutList.find((item) => item.layout === status.layout)?.icon
                    }_normal`}
                  ></use>
                </svg>
                <svg className="icon" aria-hidden="true">
                  <use xlinkHref="#icon-ic_toolbar_arrow"></use>
                </svg>
              </div>
              {showSetLayout && (
                <div className={styles.popover} style={{ left: 'unset', right: 0 }} ref={setLayoutRef}>
                  <div className={styles['radio-container']}>
                    {layoutList
                      .filter((item) => item.show)
                      .map((item: any, index: number) => (
                        <div
                          className={`${styles['radio-item']} ${
                            status.layout === item.layout ? styles['radio-item-selected'] : ''
                          }`}
                          onClick={() => setLayout(item.layout)}
                          key={index}
                        >
                          <svg className="icon" aria-hidden="true">
                            <use xlinkHref={`#icon-ic_header_shitu${item.icon}_noc`}></use>
                          </svg>
                        </div>
                      ))}
                  </div>
                </div>
              )}
            </div>
            <div className={styles.item} ref={settingBtnRef}>
              <div className={styles['inner-icon']} onClick={leaveRoom}>
                <svg className="icon" aria-hidden="true">
                  <use xlinkHref="#icon-likaijiaoshi"></use>
                </svg>
              </div>
            </div>
          </div>
        )}
      </div>
    </header>
  )
}

export default connect(({ room, status }: { room: RoomModelState; status: StatusModelState }) => ({
  room,
  status,
}))(Header)
