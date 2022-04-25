import { Effect, ImmerReducer, Subscription } from 'umi'
import { BasicMap } from '@/utils'

interface ResolutionsSetting {
  onlineCount: number
  frameRate: number
  width: number
  height: number
}
export interface SettingsModelState {
  settings: BasicMap<any>
  teacherResolutions: any[]
  teacherResolutionSteps: number[]
  teacherScreenResolutions: any[]
  teacherScreenResolutionSteps: number[]
  studentResolutions: any[]
  studentResolutionSteps: number[]
}

export interface SettingsModelType {
  namespace: 'settings'
  state: SettingsModelState
  effects: {
    query: Effect
  }
  reducers: {
    setSettings: ImmerReducer<SettingsModelState>
  }
  //   subscriptions: { setup: Subscription };
}

const generateResolutions = (resolutionStrategy: ResolutionsSetting[]) => {
  const res: any = [],
    steps: any[] = []
  resolutionStrategy.forEach((item) => {
    res.push([item.frameRate, item.width, item.height])
    steps.push(item.onlineCount)
  })
  return {
    res,
    steps,
  }
}

const SettingsModel: SettingsModelType = {
  namespace: 'settings',

  state: {
    settings: {},
    teacherResolutions: [],
    teacherResolutionSteps: [],
    teacherScreenResolutions: [],
    teacherScreenResolutionSteps: [],
    studentResolutions: [],
    studentResolutionSteps: [],
  },

  effects: {
    *query({ payload }, props) {
      console.log(props)
    },
  },

  reducers: {
    setSettings(state, { payload }) {
      state.settings = payload
      if (payload.masterShareScreenResolutionStrategy && payload.masterShareScreenResolutionStrategy.length) {
        const { res, steps } = generateResolutions(payload.masterShareScreenResolutionStrategy)
        state.teacherScreenResolutionSteps = steps
        state.teacherScreenResolutions = res
      }
      if (payload.masterCameraResolutionStrategy && payload.masterCameraResolutionStrategy.length) {
        const { res, steps } = generateResolutions(payload.masterCameraResolutionStrategy)
        state.teacherResolutionSteps = steps
        state.teacherResolutions = res
      }
      if (payload.slaveCameraResolutionStrategy && payload.slaveCameraResolutionStrategy.length) {
        const { res, steps } = generateResolutions(payload.slaveCameraResolutionStrategy)
        state.studentResolutionSteps = steps
        state.studentResolutions = res
      }
    },
  },
}

export default SettingsModel
