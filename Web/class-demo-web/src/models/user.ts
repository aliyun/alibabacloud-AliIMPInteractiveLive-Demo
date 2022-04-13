import { ImmerReducer } from 'umi'
import { BasicMap } from '@/utils'

export interface User {
  userId: string
  nick: string
  isMe: boolean
  isInSeat: boolean // 是否已经连麦
  isApplying: boolean // 是否正在申请
  isRtcMute: boolean // 是否连麦静音
  isRtcMuteCamera: boolean // 是否关闭摄像头
  isInviting: boolean // 是否正在邀请中
  streamType: number // 订阅的流类型
  subscribeResult: boolean // 是否已订阅此人
  enterSeatTime: number
  enterRoomTime: number
  isCurrent: boolean // 是否正在连麦显示中
  [index: string]: any
}

export interface UserModelState {
  userId: string
  nick: string
  isOwner: boolean
  userList: BasicMap<User>
}

export interface UserModelType {
  namespace: 'user'
  state: UserModelState
  reducers: {
    setUserId: ImmerReducer<UserModelState>
    setNick: ImmerReducer<UserModelState>
    setUserList: ImmerReducer<UserModelState>
    addUser: ImmerReducer<UserModelState>
    updateUser: ImmerReducer<UserModelState>
    deleteUser: ImmerReducer<UserModelState>
    mergeUser: ImmerReducer<UserModelState>
  }
}

const UserModel: UserModelType = {
  namespace: 'user',

  state: {
    userId: '',
    nick: '',
    isOwner: false,
    userList: {},
  },

  reducers: {
    setUserId(state, { payload }) {
      state.userId = payload
    },
    setNick(state, { payload }) {
      state.nick = payload
    },
    setUserList(state, { payload }) {
      state.userList = payload
    },
    addUser(state, { payload }) {
      state.userList[payload.userId] = payload
    },
    updateUser(state, { payload }) {
      Object.keys(payload).forEach((key: string) => {
        if (state.userList[payload.userId]) {
          state.userList[payload.userId][key] = payload[key]
        }
      })
    },
    deleteUser(state, { payload }) {
      delete state.userList[payload]
    },
    mergeUser(state, { payload }) {
      const final: BasicMap<User> = {}
      Object.keys(payload).forEach((item) => {
        if (state.userList[item]) final[item] = state.userList[item]
        else final[item] = payload[item]
      })
      state.userList = final
    },
  },
}

export default UserModel
