import { FC } from 'react'
import { ConnectProps, RoomModelState, StatusModelState, connect } from 'umi'
import { useMount } from 'ahooks'
import { Spin, message } from 'antd'
import { setPre } from '@/utils'
import { splitSearch, BasicMap } from '@/utils'
import { demoDoLogin } from '@/biz/doLogin'

const DemoDoLogin: FC<ConnectProps> = ({ location, history, dispatch }) => {
  useMount(() => {
    const classId = window.sessionStorage.getItem('classId')
    const userId = window.sessionStorage.getItem('userId')
    const nick = window.sessionStorage.getItem('userId')
    const role = window.sessionStorage.getItem('role')
    if (!userId) {
      message.error('登录失效，请重新登录')
      history.replace('/')
      return
    }
    demoDoLogin(userId)
      .then(() => {
        window.classInstance = window.roomEngine.getClassInstance()
        if (classId) return window.classInstance.getClassDetail(classId)
        else return window.classInstance.createClass('阿里云互动课堂', userId)
      })
      .then((classDetail: any) => {
        console.log(classDetail)
        if (classDetail.createUserId !== userId && role === 'teacher') {
          message.info('您不是教师，将跳转到学生页面')
        }
        if (classDetail.createUserId === userId && role === 'student') {
          message.info('您是教师，将跳转到教师页面')
        }
        window.roomChannel = window.roomEngine.getRoomChannel(classDetail.roomId)
        window.chatService = window.roomChannel.getPluginService('chat')
        window.liveService = window.roomChannel.getPluginService('live')
        window.wbService = window.roomChannel.getPluginService('wb')
        window.rtcService = window.roomChannel.getPluginService('rtc')
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
            type: 'room/updateClassStartTime',
            payload: classDetail.startTime,
          })
          dispatch({
            type: 'status/setClassStatus',
            payload: classDetail.status,
          })
        }
        return window.roomChannel.enterRoom(nick)
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
            payload: nick,
          })
          dispatch({
            type: 'user/setIsOwner',
            payload: userId === detail.ownerId,
          })
        }
        detail.ownerId === userId ? history.replace(`/class/teacher`) : history.replace(`/class/student`)
      })
      .catch((err) => {
        console.error(err)
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
