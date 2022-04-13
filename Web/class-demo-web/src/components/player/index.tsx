import { FC, useEffect, useRef } from 'react'
import { StatusModelState, SettingsModelState, RoomModelState, connect, Dispatch } from 'umi'
import { usePersistFn } from 'ahooks'
import Emitter from '@/utils/emitter'
import styles from './index.less'

const { EventNameEnum } = window.RoomPaasSdk
const emitter = Emitter.getInstance()

interface PageProps {
  status: StatusModelState
  room: RoomModelState
  settings: SettingsModelState
  dispatch: Dispatch
}

const Player: FC<PageProps> = ({ status, room, settings, dispatch }) => {
  const errorTimer = useRef<any>(null)
  const startPlayHandler = usePersistFn(() => {
    setTimeout(() => {
      window.liveService.setPlayerConfig({
        container: '#player',
        width: '100%',
        height: '100%',
        isLive: true,
        autoplay: !window.sessionStorage.getItem('refresh'),
        controlBarVisibility: 'hover',
        aliplayerSdkVer: '2.9.16',
        useArtc: true,
      })
      window.liveService.tryPlayLive()
    }, 500)
  })
  const stopPlayHandler = usePersistFn(() => {
    window.liveService.stopPlay()
  })
  const playerEventHandler = (e: any) => {
    if (e.eventName === 'error') {
      setTimeout(() => {
        window.liveService.stopPlay()
        window.liveService.tryPlayLive()
      }, 3000)
    }
  }
  useEffect(() => {
    if (!window.liveService) return
    emitter.on('livePublish', startPlayHandler)
    emitter.on('liveStop', stopPlayHandler)
    window.liveService.on(EventNameEnum.PaaSPlayerEvent, playerEventHandler)
    return () => {
      emitter.remove('livePublish', startPlayHandler)
      emitter.remove('liveStop', stopPlayHandler)
    }
  }, [])
  return (
    <div className={styles['player-container']}>
      {status.isInClass ? (
        <div className="player" id="player"></div>
      ) : (
        <div className={styles['no-class']}>{status.classStatus === 2 ? '课程已结束' : '课程未开始'}</div>
      )}
    </div>
  )
}

export default connect(
  ({ status, room, settings }: { status: StatusModelState; room: RoomModelState; settings: SettingsModelState }) => ({
    status,
    room,
    settings,
  }),
)(Player)
