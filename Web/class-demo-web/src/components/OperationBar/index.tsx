import { FC, useRef, useState } from 'react'
import { RoomModelState, StatusModelState, UserModelState, DeviceModelState, connect, Dispatch } from 'umi'
import { message, Modal } from 'antd'
import { useClickAway, useMount, usePersistFn } from 'ahooks'
import { ExclamationCircleOutlined } from '@ant-design/icons'
import styles from './index.less'

const { confirm } = Modal

interface PageProps {
  room: RoomModelState
  status: StatusModelState
  user: UserModelState
  device: DeviceModelState
  dispatch: Dispatch
}

const OperationBar: FC<PageProps> = ({ room, device, user, status, dispatch }) => {
  const [showMicSetting, setShowMicSetting] = useState(false)
  const [showCameraSetting, setShowCameraSetting] = useState(false)
  const [startClassLoading, setStartClassLoading] = useState(false)
  const micRef = useRef<HTMLDivElement>(null)
  const micBtnRef = useRef<HTMLDivElement>(null)
  const cameraRef = useRef<HTMLDivElement>(null)
  const cameraBtnRef = useRef<HTMLDivElement>(null)
  const classClickHandler = usePersistFn(() => {
    if (startClassLoading) return
    if (status.isInClass) {
      confirm({
        title: '您确定要结束课程吗？',
        cancelText: '取消',
        okText: '确定',
        icon: <ExclamationCircleOutlined />,
        content: '结束课程后无法再次进入',
        onOk() {
          window.classInstance.stopClass(room.classId).then(() => {
            dispatch({
              type: 'status/updateClass',
              payload: false,
            })
            dispatch({
              type: 'status/setIsRecording',
              payload: false,
            })
            dispatch({
              type: 'status/setIsInChannel',
              payload: false,
            })
            window.rtcService.leaveRtc(true)
            message.success('已下课')
          })
        },
        onCancel() {
          message.info('继续上课')
        },
      })
    } else {
      if (status.classStatus === 2) {
        message.error('该课堂已下课，请重新创建课堂')
        return
      }
      setStartClassLoading(true)
      window.rtcService
        .joinChannel(user.nick)
        .then(() => {
          dispatch({
            type: 'status/setIsInChannel',
            payload: true,
          })
          return window.rtcService.startRoadPublish()
        })
        .then(() => {
          return window.classInstance.startClass(room.classId)
        })
        .then(() => {
          dispatch({
            type: 'status/updateClass',
            payload: true,
          })
          message.success('开始上课')
          if (status.isAutoRecord) doStartRecord()
        })
        .catch((err: any) => {
          console.error(err)
          window.rtcService.leaveRtc()
        })
        .finally(() => {
          setStartClassLoading(false)
        })
    }
  })
  const micClickHandler = (e: any) => {
    if (e.target.dataset.type === 'more') {
      // 点击箭头
      setShowMicSetting(!showMicSetting)
    } else {
      // 点击主要区域
      window.rtcService.setMutePush(status.micAvailable).then(() => {
        dispatch({
          type: 'status/setMicAvailable',
          payload: !status.micAvailable,
        })
      })
    }
  }
  const switchMicHandler = (e: any) => {
    e.stopPropagation()
    if (e.target && e.target.dataset.id) {
      const id = e.target.dataset.id
      if (id === device.currentAudioDevice) return
      window.rtcService
        .switchMic(id)
        .then(() => {
          dispatch({
            type: 'device/setCurrentAudioDevice',
            payload: id,
          })
          message.success('切换音频设备成功')
        })
        .catch((err: any) => {
          console.error(err)
          message.error('切换音频设备失败')
        })
    }
  }
  const cameraClickHandler = (e: any) => {
    if (e.target.dataset.type === 'more') {
      // 点击箭头
      setShowCameraSetting(!showMicSetting)
    } else {
      // 点击主要区域
      window.rtcService.setMuteCamera(status.cameraAvailable).then(() => {
        dispatch({
          type: 'status/setCameraAvailable',
          payload: !status.cameraAvailable,
        })
      })
    }
  }
  const switchCameraHandler = (e: any) => {
    e.stopPropagation()
    if (e.target && e.target.dataset.id) {
      const id = e.target.dataset.id
      if (id === device.currentVideoDevice) return
      window.rtcService
        .switchCamera(id)
        .then(() => {
          message.success('切换视频设备成功')
          dispatch({
            type: 'device/setCurrentVideoDevice',
            payload: id,
          })
        })
        .catch((err: any) => {
          console.error(err)
          message.error('切换视频设备失败')
        })
    }
  }
  const screenShareClickHandler = () => {
    if (!status.isInClass) {
      message.error('当前未上课')
      return
    }
    if (!status.isScreenSharing) {
      window.rtcService.setScreenShareVideoProfile(10, 2560, 1600)
      window.rtcService
        .startPublishScreen()
        .then(() => {
          dispatch({
            type: 'status/setIsScreenSharing',
            payload: true,
          })
          return window.rtcService.setLayout([user.userId], 4)
        })
        .catch((err: any) => {
          console.error(err)
          if (err && err.message && err.message === 'Permission denied') return
          message.error('共享屏幕失败')
        })
    } else {
      window.rtcService
        .stopPublishScreen()
        .then(() => {
          dispatch({
            type: 'status/setIsScreenSharing',
            payload: false,
          })
          return window.rtcService.setLayout([user.userId], 1)
        })
        .catch((err: any) => {
          console.error(err)
          message.error('取消共享屏幕失败')
        })
    }
  }
  const uploadPPTClickHandler = () => {
    dispatch({
      type: 'status/setShowPPTUploader',
      payload: !status.showPPTUploader,
    })
  }
  const toolsClickHandler = () => {}
  const recordClickHandler = () => {
    if (!status.isInClass) {
      message.error('当前未上课')
      return
    }
    doStartRecord()
  }
  const doStartRecord = () => {
    window.rtcService.startRecord()
    return window.wbService
      .startWhiteboardRecording()
      .then((res: any) => {
        window.sessionStorage.setItem('recordId', res.recordId)
        dispatch({
          type: 'status/setIsRecording',
          payload: true,
        })
      })
      .catch((err: any) => {
        console.log(err)
        message.error('录制开启失败')
      })
  }
  const getDevices = () => {
    if (!window.rtcService) return
    return window.rtcService
      .getDeviceInfo()
      .then((res: any) => {
        console.log(res)
        if (res.videoDevices.length === 0) {
          message.error('获取视频设备失败，请检查设备接入')
        } else {
          dispatch({
            type: 'device/setVideoDevices',
            payload: res.videoDevices,
          })
          dispatch({
            type: 'device/setCurrentVideoDevice',
            payload: res.videoDevices[0].deviceId,
          })
        }
        if (res.audioDevices.length === 0) {
          message.error('获取音频设备失败，请检查设备接入')
        } else {
          // audioDevices里会有一个default选项，需要去掉
          const defaultIndex = res.audioDevices.findIndex((item: any) => item.deviceId === 'default')
          const audioDevices = []
          let defaultDeviceId = ''
          for (let i = 0; i < res.audioDevices.length; i++) {
            if (i === defaultIndex) continue
            if (res.audioDevices[i].groupId === res.audioDevices[defaultIndex].groupId)
              defaultDeviceId = res.audioDevices[i].deviceId
            audioDevices.push(res.audioDevices[i])
          }
          dispatch({
            type: 'device/setAudioDevices',
            payload: audioDevices,
          })
          dispatch({
            type: 'device/setCurrentAudioDevice',
            payload: defaultDeviceId,
          })
        }
      })
      .catch((err: any) => {
        throw err
      })
  }
  useClickAway(() => {
    showMicSetting && setShowMicSetting(false)
  }, [micRef, micBtnRef])
  useClickAway(() => {
    showCameraSetting && setShowCameraSetting(false)
  }, [cameraRef, cameraBtnRef])
  useMount(() => {
    console.log(status)
    getDevices()
    if (status.classStatus === 1) {
      classClickHandler()
    }
  })
  return (
    <div className={styles['operation-bar-container']}>
      <div className={styles['operation-bar']}>
        <div className={styles.item} onClick={classClickHandler}>
          <div className={styles['item-inner']}>
            <div className={styles.icon}>
              {startClassLoading ? (
                <svg viewBox="25 25 50 50" className={styles.loading}>
                  <circle className={styles.circle} cx="50" cy="50" r="20"></circle>
                </svg>
              ) : (
                <svg className="icon" aria-hidden="true" style={status.isInClass ? { color: '#fe7c7c' } : {}}>
                  <use xlinkHref="#icon-a-ic_toolbar_attendclass"></use>
                </svg>
              )}
            </div>
            <div className={styles.name}>{status.isInClass ? '下课' : '上课'}</div>
          </div>
        </div>
        <div className={styles.divider}></div>
        <div className={styles.item} onClick={(e) => micClickHandler(e)}>
          <div className={styles['item-inner']}>
            <div className={styles.icon}>
              {status.micAvailable ? (
                <svg className="icon" aria-hidden="true">
                  <use xlinkHref="#icon-ic_toolbar_jingyin"></use>
                </svg>
              ) : (
                <svg className="icon" aria-hidden="true">
                  <use xlinkHref="#icon-ic_toolbar_quxiaojingyin"></use>
                </svg>
              )}
            </div>
            <div className={styles.name}>{status.micAvailable ? '静音' : '关闭静音'}</div>
          </div>
          <div className={styles.more} data-type="more" ref={micBtnRef}>
            <svg className="icon" aria-hidden="true" data-type="more">
              <use xlinkHref="#icon-ic_toolbar_arrow" data-type="more"></use>
            </svg>
          </div>
          {showMicSetting && (
            <div className={styles.popover} ref={micRef}>
              <div className={styles['selector-container']}>
                {device.audioDevices.map((item, index) => (
                  <div
                    className={`${styles['selector-item']} ${
                      device.currentAudioDevice === item.deviceId ? styles['selector-item-selected'] : ''
                    }`}
                    onClick={switchMicHandler}
                    data-id={item.deviceId}
                    key={index}
                  >
                    {item.label}
                    <div className={styles.check}>
                      {device.currentAudioDevice === item.deviceId && (
                        <svg className="icon" aria-hidden="true">
                          <use xlinkHref="#icon-ic_header_tick"></use>
                        </svg>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
        <div className={styles.item} onClick={(e) => cameraClickHandler(e)}>
          <div className={styles['item-inner']}>
            <div className={styles.icon}>
              {status.cameraAvailable ? (
                <svg className="icon" aria-hidden="true">
                  <use xlinkHref="#icon-ic_toolbar_shexiangtou"></use>
                </svg>
              ) : (
                <svg className="icon" aria-hidden="true">
                  <use xlinkHref="#icon-ic_toolbar_guanshexiangtou"></use>
                </svg>
              )}
            </div>
            <div className={styles.name}>{status.cameraAvailable ? '关闭摄像头' : '打开摄像头'}</div>
          </div>
          <div className={styles.more} data-type="more" ref={cameraBtnRef}>
            <svg className="icon" aria-hidden="true" data-type="more">
              <use xlinkHref="#icon-ic_toolbar_arrow" data-type="more"></use>
            </svg>
          </div>
          {showCameraSetting && (
            <div className={styles.popover} ref={cameraRef}>
              <div className={styles['selector-container']}>
                {device.videoDevices.map((item, index) => (
                  <div
                    className={`${styles['selector-item']} ${
                      device.currentVideoDevice === item.deviceId ? styles['selector-item-selected'] : ''
                    }`}
                    onClick={switchCameraHandler}
                    data-id={item.deviceId}
                    key={index}
                  >
                    {item.label}
                    <div className={styles.check}>
                      {device.currentVideoDevice === item.deviceId && (
                        <svg className="icon" aria-hidden="true">
                          <use xlinkHref="#icon-ic_header_tick"></use>
                        </svg>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
        <div className={styles.item} onClick={screenShareClickHandler}>
          <div className={styles['item-inner']}>
            <div className={styles.icon}>
              <svg className="icon" aria-hidden="true">
                <use xlinkHref="#icon-ic_toolbar_gongxiang"></use>
              </svg>
            </div>
            <div className={styles.name}>{status.isScreenSharing ? '正在共享' : '共享屏幕'}</div>
          </div>
        </div>
        {/* <div className={styles.item} onClick={uploadPPTClickHandler}>
          <div className={styles['item-inner']}>
            <div className={styles.icon}>
              <svg className="icon" aria-hidden="true">
                <use xlinkHref="#icon-ic_toolbar_kejian"></use>
              </svg>
            </div>
            <div className={styles.name}>课件管理</div>
          </div>
        </div>
        <div className={styles.item} onClick={toolsClickHandler}>
          <div className={styles['item-inner']}>
            <div className={styles.icon}>
              <svg className="icon" aria-hidden="true">
                <use xlinkHref="#icon-ic_toolbar_gongju_noc"></use>
              </svg>
            </div>
            <div className={styles.name}>教学工具</div>
          </div>
        </div> */}
        <div className={styles.item} onClick={recordClickHandler}>
          <div className={styles['item-inner']}>
            <div className={styles.icon}>
              <svg className="icon" aria-hidden="true" style={status.isRecording ? { color: '#fe7c7c' } : {}}>
                <use xlinkHref="#icon-ic_toolbar_luzhi"></use>
              </svg>
            </div>
            <div className={styles.name}>{status.isRecording ? '正在录制' : '录制'}</div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default connect(
  ({
    room,
    user,
    status,
    device,
  }: {
    room: RoomModelState
    user: UserModelState
    status: StatusModelState
    device: DeviceModelState
  }) => ({
    room,
    user,
    status,
    device,
  }),
)(OperationBar)
