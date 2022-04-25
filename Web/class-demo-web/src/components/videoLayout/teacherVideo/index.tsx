import { FC, useEffect, useRef } from 'react'
import { RoomModelState, UserModelState, StatusModelState, connect, Dispatch } from 'umi'
import { usePersistFn } from 'ahooks'
import Emitter from '@/utils/emitter'
import styles from './index.less'

const { EventNameEnum } = window.RoomPaasSdk
const emitter = Emitter.getInstance()

interface PageProps {
  room: RoomModelState
  user: UserModelState
  status: StatusModelState
  dispatch: Dispatch
}

const SpeakerStu: FC<PageProps> = ({ room, user, status, dispatch }) => {
  const teacherRef = useRef<HTMLVideoElement>(null)

  const subscribeTeacher = usePersistFn(async () => {
    if (!window.rtcService) return
    const ownerRtcInfo = window.rtcService.getUserInfo(room.ownerId)
    if (!ownerRtcInfo || ownerRtcInfo.streamConfigs.length === 0) return
    const screenShareStreamConfig = ownerRtcInfo.streamConfigs.find(
      (item: any) => item.label === 'sophon_video_screen_share',
    )
    const audioStreamConfig = ownerRtcInfo.streamConfigs.find((item: any) => item.label === 'sophon_audio')
    const cameraStreamConfig = ownerRtcInfo.streamConfigs.find(
      (item: any) => item.label === 'sophon_video_camera_large',
    )
    await window.rtcService.unSubscribe(room.ownerId)
    let stream = 1
    if (screenShareStreamConfig && screenShareStreamConfig.state === 'active') {
      await window.rtcService.subscribeScreen(room.ownerId)
      stream = 2
    } else await window.rtcService.subscribeCamera(room.ownerId)
    dispatch({
      type: 'user/updateUser',
      payload: {
        userId: room.ownerId,
        isRtcMute: audioStreamConfig?.state === 'inactive' || audioStreamConfig?.muted,
        isRtcMuteCamera: cameraStreamConfig?.state === 'inactive',
        streamType: stream,
      },
    })
    setTimeout(() => {
      console.log('setDisplayRemoteVideo: ', 'teacher')
      window.rtcService.setDisplayRemoteVideo(teacherRef.current, room.ownerId, stream)
    }, 500)
  })

  const onPublisherHandler = usePersistFn((info: any) => {
    if (info.userId === room.ownerId) {
      setTimeout(() => {
        subscribeTeacher()
      }, 500)
    }
  })

  useEffect(() => {
    if (!window.rtcService) return
    window.rtcService.on(EventNameEnum.onPublisher, onPublisherHandler)
    emitter.on('needSubscribe', subscribeTeacher)
    return () => {
      if (!window.rtcService) return
      window.rtcService.remove(EventNameEnum.onPublisher, onPublisherHandler)
      emitter.remove('needSubscribe', subscribeTeacher)
    }
  }, [])

  useEffect(() => {
    setTimeout(() => {
      subscribeTeacher()
    }, 500)
  }, [status.layout])

  return (
    <div
      className={`${styles['teacher-main']} ${
        status.viewMode === 'whiteBoard' || !status.linkMicBarExpand ? styles['full-teacher-main'] : ''
      }`}
    >
      {user.userList[room.ownerId] &&
        user.userList[room.ownerId].isRtcMuteCamera &&
        user.userList[room.ownerId].streamType === 1 && (
          <div className={styles['camera-unavailable']}>
            <svg className="icon" aria-hidden="true">
              <use xlinkHref="#icon-ic_toolbar_guanshexiangtou_noc"></use>
            </svg>
          </div>
        )}
      <video id="teacher" ref={teacherRef}></video>
    </div>
  )
}

export default connect(
  ({ room, user, status }: { room: RoomModelState; user: UserModelState; status: StatusModelState }) => ({
    room,
    user,
    status,
  }),
)(SpeakerStu)
