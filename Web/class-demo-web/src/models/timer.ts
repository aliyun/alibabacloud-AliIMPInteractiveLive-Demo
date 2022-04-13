import { ImmerReducer } from 'umi'

export interface TimerModelState {
  classStartTime: number
  classDuration: number
}

export interface TimerModelType {
  namespace: 'timer'
  state: TimerModelState
  reducers: {
    updateClassTime: ImmerReducer<TimerModelState>
    updateClassStartTime: ImmerReducer<TimerModelState>
  }
}

const TimerModel: TimerModelType = {
  namespace: 'timer',

  state: {
    classStartTime: 0,
    classDuration: 0,
  },

  reducers: {
    updateClassStartTime(state, { payload }) {
      state.classStartTime = payload
    },
    updateClassTime(state, { payload }) {
      state.classDuration = payload
    },
  },
}

export default TimerModel
