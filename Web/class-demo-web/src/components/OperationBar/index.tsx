import { FC, useRef, useState } from 'react'
import {
  RoomModelState,
  StatusModelState,
  UserModelState,
  DeviceModelState,
  SettingsModelState,
  connect,
  Dispatch,
} from 'umi'
import { message, Modal } from 'antd'
import { useHistory } from 'react-router'
import { useClickAway, useMount, usePersistFn } from 'ahooks'
import { ExclamationCircleOutlined } from '@ant-design/icons'
import Emitter from '@/utils/emitter'
import styles from './index.less'

interface PageProps {
  room: RoomModelState
  status: StatusModelState
  user: UserModelState
  device: DeviceModelState
  settings: SettingsModelState
  dispatch: Dispatch
}

const emitter = Emitter.getInstance()
const { confirm } = Modal
const toolsList = [
  {
    icon: '#icon-gonggao',
    label: '公告',
    key: 'notice',
  },
]

const OperationBar: FC<PageProps> = ({ room, device, user, status, settings, dispatch }) => {
  const history = useHistory()
  const [showMicSetting, setShowMicSetting] = useState(false)
  const [showCameraSetting, setShowCameraSetting] = useState(false)
  const [showTools, setShowTools] = useState(false)
  const [startClassLoading, setStartClassLoading] = useState(false)
  const micRef = useRef<HTMLDivElement>(null)
  const micBtnRef = useRef<HTMLDivElement>(null)
  const cameraRef = useRef<HTMLDivElement>(null)
  const cameraBtnRef = useRef<HTMLDivElement>(null)
  const toolsBtnRef = useRef<HTMLDivElement>(null)
  const toolsRef = useRef<HTMLDivElement>(null)
  const classClickHandler = usePersistFn((isAuto?: boolean) => {
    if (!isAuto) {
      const supportInfo = window.rtcService._rtcManager && window.rtcService._rtcManager.supportInfo
      if (!supportInfo || !supportInfo.isSupported || !supportInfo.audioDevice || !supportInfo.videoDevice) {
        Modal.error({
          title: '兼容性错误',
          content: '互动课堂的连麦功能需要使用摄像头和麦克风，请检查您的硬件是否可用，或是否通过了浏览器或系统的授权',
        })
        return
      }
    }
    if (startClassLoading) return
    if (status.isInClass) {
      confirm({
        title: '您确定要结束课程吗？',
        cancelText: '取消',
        okText: '确定',
        icon: <ExclamationCircleOutlined />,
        content: '结束课程后无法再次进入',
        onOk() {
          window.sessionStorage.setItem('teacherStopClass', '1')
          window.classInstance
            .stopClass(room.classId)
            .then(() => {
              window.rtcService.leaveRtc(true)
              const recordId = window.sessionStorage.getItem('recordId')
              if (recordId) {
                return window.wbService.stopWhiteboardRecording(recordId)
              }
              return Promise.resolve(true)
            })
            .finally(() => {
              message.success('已下课')
              setTimeout(() => {
                history.replace('/login')
                window.location.reload()
              }, 2000)
            })
        },
        onCancel() {
          message.info('继续上课')
        },
      })
    } else {
      if (status.classStatus === 2) {
        message.error('该课堂已下课，请重新创建课堂')
        history.replace('/login')
        return
      }
      setStartClassLoading(true)
      let cameraProfile = [15, 848, 480]
      let screenProfile = [7, 1920, 1080]
      if (settings.teacherResolutions.length > 0) cameraProfile = settings.teacherResolutions[0]
      if (settings.teacherScreenResolutions.length > 0) screenProfile = settings.teacherScreenResolutions[0]
      window.rtcService.setVideoProfile(...cameraProfile)
      window.rtcService.setScreenShareVideoProfile(...screenProfile)
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
          dispatch({
            type: 'status/setLinkMicUserCount',
            payload: window.rtcService.getUserList().length,
          })
          setTimeout(() => {
            dispatch({
              type: 'user/updateUser',
              payload: {
                userId: user.userId,
                isInSeat: true,
                isRtcMute: false,
                isRtcMuteCamera: false,
              },
            })
          }, 1000)
          message.success('开始上课')
          if (status.isAutoRecord) doStartRecord()
        })
        .catch((err: any) => {
          console.error(err)
          window.rtcService.leaveRtc()
        })
        .finally(() => {
          emitter.emit('needInitUserList')
          setStartClassLoading(false)
        })
    }
  })
  const micClickHandler = (e: any) => {
    if (e.target.dataset.type === 'more') {
      // 点击箭头
      getDevices().then(() => {
        setShowMicSetting(!showMicSetting)
      })
    } else {
      // 点击主要区域
      if (!status.isInClass) return message.error('当前未上课')
      window.rtcService.setMutePush(status.micAvailable).then(() => {
        status.micAvailable ? message.info('麦克风已关闭') : message.success('麦克风已开启')
        dispatch({
          type: 'status/setMicAvailable',
          payload: !status.micAvailable,
        })
        dispatch({
          type: 'user/updateUser',
          payload: {
            userId: user.userId,
            isRtcMute: status.micAvailable,
          },
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
  const cameraClickHandler = async (e: any) => {
    if (e.target.dataset.type === 'more') {
      // 点击箭头
      getDevices().then(() => {
        setShowCameraSetting(!showMicSetting)
      })
    } else {
      // 点击主要区域
      if (!status.isInClass) return message.error('当前未上课')
      try {
        if (status.cameraAvailable) {
          window.rtcService._rtcManager.getAliRtcEngine().configLocalCameraPublish = false
          console.log('设置了不推摄像头流')
          await window.rtcService.startPublish()
          console.log('publish成功')
          await window.rtcService.reportCameraStatus(false)
        } else {
          await window.rtcService.startPublishCamera()
          console.log('publish成功')
          await window.rtcService.reportCameraStatus(true)
        }
        status.cameraAvailable ? message.info('摄像头已关闭') : message.success('摄像头已开启')
        dispatch({
          type: 'status/setCameraAvailable',
          payload: !status.cameraAvailable,
        })
        dispatch({
          type: 'user/updateUser',
          payload: {
            userId: user.userId,
            isRtcMuteCamera: status.cameraAvailable,
          },
        })
      } catch (err) {
        console.error(err)
        message.error('切换摄像头推流失败，请检查配置或尝试刷新页面')
      }
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
    const supportInfo = window.rtcService._rtcManager && window.rtcService._rtcManager.supportInfo
    if (!supportInfo || !supportInfo.supportScreenShare) {
      Modal.error({
        title: '兼容性错误',
        content: '检测到您的浏览器不支持共享屏幕，请检查设置',
      })
      return
    }
    if (!status.isScreenSharing) {
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
          if (err && err.message) {
            switch (err.message) {
              case 'Permission denied':
                break
              case 'Permission denied by system':
                Modal.error({
                  title: '共享屏幕失败',
                  content:
                    '检测到您的系统或浏览器没有共享屏幕权限，请检查相关配置，如Mac系统需检查安全性与隐私-隐私-屏幕录制权限是否开启。',
                })
                break
              default:
                message.error('共享屏幕失败:' + err.message)
            }
            return
          }
          console.dir(err)
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
          emitter.emit('needSubscribe')
          return window.rtcService.setLayout([user.userId], 1)
        })
        .catch((err: any) => {
          console.error(err)
          message.error('取消共享屏幕失败')
        })
    }
  }
  const toolClickHandler = (e: any, key: string) => {
    e.stopPropagation()
    setShowTools(false)
    if (!key) return
    switch (key) {
      case 'notice':
        dispatch({
          type: 'status/setShowNoticeEditor',
          payload: true,
        })
        break
      default:
        message.error('工具错误，请刷新页面')
    }
  }
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
  const getDevices = async () => {
    if (!window.rtcService) return
    if (
      device.audioDevices[0] &&
      device.audioDevices[0].deviceId &&
      device.videoDevices[0] &&
      device.videoDevices[0].deviceId
    )
      return
    try {
      const res = await window.rtcService.getDeviceInfo()
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
          if (defaultIndex > -1 && res.audioDevices[i].groupId === res.audioDevices[defaultIndex].groupId)
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
    } catch (err) {
      throw err
    }
  }
  useClickAway(() => {
    showMicSetting && setShowMicSetting(false)
  }, [micRef, micBtnRef])
  useClickAway(() => {
    showCameraSetting && setShowCameraSetting(false)
  }, [cameraRef, cameraBtnRef])
  useClickAway(() => {
    showTools && setShowTools(false)
  }, [toolsRef, toolsBtnRef])
  useMount(() => {
    console.log(status)
    getDevices()
    if (status.classStatus === 1) {
      classClickHandler(true)
    } else {
      emitter.emit('needInitUserList')
    }
  })
  return (
    <div className={styles['operation-bar-container']}>
      <div className={styles['operation-bar']}>
        <div className={styles.item} onClick={() => classClickHandler(false)}>
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
        </div> */}
        <div className={styles.item} onClick={() => setShowTools(!showTools)} ref={toolsBtnRef}>
          <div className={styles['item-inner']}>
            <div className={styles.icon}>
              <svg className="icon" aria-hidden="true">
                <use xlinkHref="#icon-ic_toolbar_gongju_noc"></use>
              </svg>
            </div>
            <div className={styles.name}>教学工具</div>
          </div>
          {showTools && (
            <div className={styles.popover} ref={toolsRef}>
              <div className={styles['tools-container']}>
                {toolsList.map((item, index) => (
                  <div className={`${styles['tool-item']}`} onClick={(e) => toolClickHandler(e, item.key)} key={index}>
                    <svg className="icon" aria-hidden="true">
                      <use xlinkHref={item.icon}></use>
                    </svg>
                    <span>{item.label}</span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
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
    settings,
  }: {
    room: RoomModelState
    user: UserModelState
    status: StatusModelState
    device: DeviceModelState
    settings: SettingsModelState
  }) => ({
    room,
    user,
    status,
    device,
    settings,
  }),
)(OperationBar)
