import { ImmerReducer, Subscription } from 'umi'

export interface StatusModelState {
  [index: string]: any
  isInClass: boolean // 正在上课
  layout: '6' | '9' // 宫格布局
  isAutoRecord: boolean // 是否自动录制
  isRecording: boolean // 是否正在录制
  mainLayout: 'normal' | 'reverse' // aside与main是否左右互换 todo
  asideLayout: 'normal' | 'reverse' // aside内部是否上下互换 todo
  viewMode: 'video' | 'whiteBoard' // 主内容展示白板或视频
  isMuteAll: boolean // 是否正在全员禁言
  isMuteSelf: boolean // 是否自己被禁言
  isRtcMuteAll: boolean // 是否所有人静音
  micAvailable: boolean // 是否静音
  cameraAvailable: boolean // 是否禁用摄像头
  showNoticeEditor: boolean // 是否显示ppt上传组件
  isInChannel: boolean // 是否在连麦
  isApplying: boolean // 学生是否正在申请连麦
  isLiving: boolean // 是否正在直播
  isScreenSharing: boolean // 是否正在屏幕分享
  classStatus: 0 | 1 | 2 // 课堂状态 0:未开始 1:上课中 2:已下课
  docConverting: boolean // 文档转码状态
  currentDocId: string // 当前正在转码的文档Id
  teacherCameraAvailable: boolean // 在学生端，老师是否关闭摄像头
  teacherMicAvailable: boolean // 在学生端，老师是否关闭麦克风
  linkMicBarExpand: boolean // 连麦条是否展开
  linkMicUserCount: number // 目前订阅的人数
}

const defaultState: StatusModelState = {
  isInClass: false,
  layout: '6',
  isAutoRecord: true,
  isRecording: false,
  mainLayout: 'normal',
  asideLayout: 'normal',
  viewMode: 'whiteBoard',
  isMuteAll: false,
  isMuteSelf: false,
  isRtcMuteAll: false,
  micAvailable: true,
  cameraAvailable: true,
  showNoticeEditor: false,
  isInChannel: false,
  isApplying: false,
  isLiving: false,
  classStatus: 0,
  isScreenSharing: false,
  docConverting: false,
  currentDocId: '',
  teacherCameraAvailable: true,
  teacherMicAvailable: true,
  linkMicBarExpand: true,
  linkMicUserCount: 0,
}

export interface StatusModelType {
  namespace: 'status'
  state: StatusModelState
  reducers: {
    updateClass: ImmerReducer<StatusModelState>
    setIsInChannel: ImmerReducer<StatusModelState>
    setLayout: ImmerReducer<StatusModelState>
    setIsAutoRecord: ImmerReducer<StatusModelState>
    setIsRecording: ImmerReducer<StatusModelState>
    setMainLayout: ImmerReducer<StatusModelState>
    setAsideLayout: ImmerReducer<StatusModelState>
    setIsMuteAll: ImmerReducer<StatusModelState>
    setIsMuteSelf: ImmerReducer<StatusModelState>
    setMicAvailable: ImmerReducer<StatusModelState>
    setCameraAvailable: ImmerReducer<StatusModelState>
    setViewMode: ImmerReducer<StatusModelState>
    setShowNoticeEditor: ImmerReducer<StatusModelState>
    setIsApplying: ImmerReducer<StatusModelState>
    setIsLiving: ImmerReducer<StatusModelState>
    setClassStatus: ImmerReducer<StatusModelState>
    setIsRtcMuteAll: ImmerReducer<StatusModelState>
    setIsScreenSharing: ImmerReducer<StatusModelState>
    setIsDocConverting: ImmerReducer<StatusModelState>
    setCurrentDocId: ImmerReducer<StatusModelState>
    setTeacherCameraAvailable: ImmerReducer<StatusModelState>
    setTeacherMicAvailable: ImmerReducer<StatusModelState>
    setLinkMicBarExpand: ImmerReducer<StatusModelState>
    setLinkMicUserCount: ImmerReducer<StatusModelState>
    reset: ImmerReducer<StatusModelState>
  }
  subscriptions: { setup: Subscription }
}

const StatusModel: StatusModelType = {
  namespace: 'status',

  state: {
    ...defaultState,
  },

  reducers: {
    updateClass(state, { payload }) {
      state.isInClass = payload
    },
    setIsInChannel(state, { payload }) {
      state.isInChannel = payload
    },
    setLayout(state, { payload }) {
      state.layout = payload
    },
    setIsAutoRecord(state, { payload }) {
      state.isAutoRecord = payload
    },
    setIsRecording(state, { payload }) {
      state.isRecording = payload
    },
    setMainLayout(state, { payload }) {
      state.isAutoRecord = payload
    },
    setAsideLayout(state, { payload }) {
      state.isAutoRecord = payload
    },
    setIsMuteAll(state, { payload }) {
      state.isMuteAll = payload
    },
    setIsMuteSelf(state, { payload }) {
      state.isMuteSelf = payload
    },
    setMicAvailable(state, { payload }) {
      state.micAvailable = payload
    },
    setCameraAvailable(state, { payload }) {
      state.cameraAvailable = payload
    },
    setViewMode(state, { payload }) {
      state.viewMode = payload
    },
    setShowNoticeEditor(state, { payload }) {
      state.showNoticeEditor = payload
    },
    setIsApplying(state, { payload }) {
      state.isApplying = payload
    },
    setIsLiving(state, { payload }) {
      state.isLiving = payload
    },
    setClassStatus(state, { payload }) {
      state.classStatus = payload
    },
    setIsRtcMuteAll(state, { payload }) {
      state.isRtcMuteAll = payload
    },
    setIsScreenSharing(state, { payload }) {
      state.isScreenSharing = payload
    },
    setIsDocConverting(state, { payload }) {
      state.docConverting = payload
    },
    setCurrentDocId(state, { payload }) {
      state.currentDocId = payload
    },
    setTeacherCameraAvailable(state, { payload }) {
      state.teacherCameraAvailable = payload
    },
    setTeacherMicAvailable(state, { payload }) {
      state.teacherMicAvailable = payload
    },
    setLinkMicBarExpand(state, { payload }) {
      state.linkMicBarExpand = payload
    },
    setLinkMicUserCount(state, { payload }) {
      state.linkMicUserCount = payload
    },
    reset(state) {
      Object.keys(defaultState).forEach((key) => {
        state[key] = defaultState[key]
      })
    },
  },

  subscriptions: {
    setup({ dispatch, history }) {
      history.listen(({ pathname }) => {
        if (pathname === '/login') {
          dispatch({
            type: 'reset',
          })
        }
      })
    },
  },
}

export default StatusModel
