import { FC } from 'react'
import { ConnectProps, RoomModelState, StatusModelState, connect } from 'umi'
import { useMount } from 'ahooks'
import { Spin, message } from 'antd'
import { setPre } from '@/utils'
import { BasicMap } from '@/utils'
import { demoDoLogin } from '@/biz/doLogin'
import { MD5 } from 'crypto-es/lib/md5.js'

const DemoDoLogin: FC<ConnectProps> = ({ history, dispatch }) => {
  useMount(() => {
    const classId = window.sessionStorage.getItem('classId')
    const userNick = window.sessionStorage.getItem('userNick')
    const role = window.sessionStorage.getItem('role')
    if (!userNick) {
      message.error('登录失效，请重新登录')
      history.replace('/login')
      return
    }
    const userId = MD5(userNick).toString().substr(0, 20)
    // const userId = userNick
    demoDoLogin(userId)
      .then(() => {
        window.classInstance = window.roomEngine.getClassInstance()
        if (classId) return window.classInstance.getClassDetail(classId)
        else return window.classInstance.createClass('阿里云互动课堂', userNick)
      })
      .then((classDetail: any) => {
        console.log(classDetail)
        if (classDetail.classStatus === 2) {
          message.error('该课堂已下课，请重新创建课堂')
          return
        }
        window.sessionStorage.setItem('classId', classDetail.classId)
        if (classDetail.createUserId !== userId && role === 'teacher') {
          message.info('您不是教师，将跳转到学生页面')
          window.sessionStorage.setItem('role', 'student')
        }
        if (classDetail.createUserId === userId && role === 'student') {
          message.info('您是教师，将跳转到教师页面')
          window.sessionStorage.setItem('role', 'teacher')
        }
        window.roomChannel = window.roomEngine.getRoomChannel(classDetail.roomId)
        window.chatService = window.roomChannel.getPluginService('chat')
        window.liveService = window.roomChannel.getPluginService('live')
        window.wbService = window.roomChannel.getPluginService('wb')
        window.docService = window.roomChannel.getPluginService('doc')
        window.rtcService = window.roomChannel.getPluginService('rtc')
        window.rtcService.setAutoSubscribe(false)
        if (dispatch) {
          dispatch({
            type: 'room/setDocKey',
            payload: classDetail.whiteboardId,
          })
          dispatch({
            type: 'room/setClassId',
            payload: classId || classDetail.classId,
          })
          dispatch({
            type: 'timer/updateClassStartTime',
            payload: classDetail.startTime,
          })
          dispatch({
            type: 'status/setClassStatus',
            payload: classDetail.status,
          })
        }
        return window.roomEngine.getAppConfig()
      })
      .then((config: any) => {
        try {
          if (config && config.configMap) {
            const settings = JSON.parse(config.configMap)
            dispatch &&
              dispatch({
                type: 'settings/setSettings',
                payload: Object.assign(
                  {
                    classScene: {},
                    useArtc: true,
                    classDefaultOpenCamera: true,
                  },
                  settings,
                ),
              })
            if (settings.classScene && settings.classScene.enableWhiteBoard === false) {
              dispatch &&
                dispatch({
                  type: 'status/setViewMode',
                  payload: 'video',
                })
            }
          }
        } catch (err) {
          console.error(err)
        }
        return window.roomChannel.enterRoom(userNick)
      })
      .then((detail: any) => {
        if (dispatch) {
          dispatch({
            type: 'room/setRoomDetail',
            payload: detail,
          })
          dispatch({
            type: 'room/setRoomId',
            payload: detail.roomId,
          })
          dispatch({
            type: 'user/setUserId',
            payload: userId,
          })
          dispatch({
            type: 'user/setNick',
            payload: userNick,
          })
          dispatch({
            type: 'room/setIsAdministrator',
            payload: userId === detail.ownerId,
          })
          dispatch({
            type: 'room/setIsOwner',
            payload: userId === detail.ownerId,
          })
        }
        detail.ownerId === userId ? history.replace('/class/teacher') : history.replace('/class/student')
      })
      .catch((err) => {
        if (err.message) {
          if (err.message === '获取课堂详情失败') {
            message.error(err.message + '，请检查classId')
          } else {
            message.error(err.message)
          }
        }
        if (err.body && err.body.reason) {
          message.error(`${err.body.reason} 请检查课堂号是否为同一appId`)
        }
        console.error(err)
        history.replace('/login')
      })
  })
  const style: BasicMap<any> = {
    paddingTop: '200px',
    fontSize: '18px',
    margin: '0 auto',
    display: 'block',
  }
  return (
    <div onClick={setPre}>
      <Spin style={style} tip="登录中..." size="large" />
    </div>
  )
}
export default connect(({ room, status }: { room: RoomModelState; status: StatusModelState }) => ({
  room,
  status,
}))(DemoDoLogin)
