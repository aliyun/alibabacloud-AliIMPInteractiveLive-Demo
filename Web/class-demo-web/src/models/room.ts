import { Effect, ImmerReducer, Subscription } from 'umi'

export interface RoomModelState {
  title: string
  notice: string
  ownerId: string
  roomId: string
  classId: string
  docKey: string
  isOwner: boolean
  isAdministrator: boolean
  isRecorder: boolean
}

export interface RoomModelType {
  namespace: 'room'
  state: RoomModelState
  effects: {
    query: Effect
  }
  reducers: {
    setRoomDetail: ImmerReducer<RoomModelState>
    setRoomId: ImmerReducer<RoomModelState>
    setDocKey: ImmerReducer<RoomModelState>
    setClassId: ImmerReducer<RoomModelState>
    setIsAdministrator: ImmerReducer<RoomModelState>
    setIsOwner: ImmerReducer<RoomModelState>
    setIsRecorder: ImmerReducer<RoomModelState>
  }
  //   subscriptions: { setup: Subscription };
}

const RoomModel: RoomModelType = {
  namespace: 'room',

  state: {
    title: '阿里云互动课堂',
    notice: '',
    ownerId: '',
    roomId: '',
    classId: '',
    docKey: '',
    isOwner: false,
    isAdministrator: false,
    isRecorder: false,
  },

  effects: {
    *query({ payload }, props) {
      console.log(props)
    },
  },

  reducers: {
    setRoomDetail(state, { payload }) {
      if (payload.title) state.title = payload.title
      if (payload.notice || payload.notice === '') state.notice = payload.notice
      if (payload.ownerId) state.ownerId = payload.ownerId
    },
    setRoomId(state, { payload }) {
      state.roomId = payload
    },
    setClassId(state, { payload }) {
      state.classId = payload
    },
    setDocKey(state, { payload }) {
      state.docKey = payload
    },
    setIsAdministrator(state, { payload }) {
      state.isAdministrator = payload
    },
    setIsOwner(state, { payload }) {
      state.isOwner = payload
    },
    setIsRecorder(state, { payload }) {
      state.isRecorder = payload
    },
  },
}

export default RoomModel
