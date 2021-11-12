import { FC, useEffect, useState, useRef } from 'react'
import { RoomModelState, StatusModelState, UserModelState, connect, Dispatch } from 'umi'
import { useMount, usePersistFn } from 'ahooks'
import Emitter from '@/utils/emitter'
import styles from './index.less'

const emitter = Emitter.getInstance()

interface PageProps {
  room: RoomModelState
  status: StatusModelState
  user: UserModelState
  dispatch: Dispatch
}

const Player: FC<PageProps> = ({ room, status, user, dispatch }) => {
  const startPlayHandler = usePersistFn(() => {
    setTimeout(() => {
      window.liveService.setPlayerConfig({
        container: '#player',
        width: '100%',
        height: '100%',
        isLive: true,
        autoplay: true,
        useArtc: true,
        controlBarVisibility: 'hover',
      })
      window.liveService.tryPlayLive()
    }, 500)
  })
  const stopPlayHandler = usePersistFn(() => {
    window.liveService.stopPlay()
  })
  useMount(() => {
    if (!window.liveService) return
    emitter.on('livePublish', startPlayHandler)
    console.log('event.on livePublish')
    emitter.on('liveStop', stopPlayHandler)
  })
  return (
    <div className={styles['player-container']}>
      {status.isInClass ? (
        <div className="player" id="player"></div>
      ) : (
        <div className={styles['no-class']}>课程未开始</div>
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
)(Player)
