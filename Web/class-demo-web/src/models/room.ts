import { Effect, ImmerReducer, Subscription } from 'umi';

export interface RoomModelState {
  title: string;
  notice: string;
  ownerId: string;
  roomId: string;
  classId: string;
  docKey: string;
  classStartTime: number;
  classDuration: number;
}

export interface RoomModelType {
  namespace: 'room';
  state: RoomModelState;
  effects: {
    query: Effect;
  };
  reducers: {
    setRoomDetail: ImmerReducer<RoomModelState>;
    setRoomId: ImmerReducer<RoomModelState>;
    setDocKey: ImmerReducer<RoomModelState>;
    setClassId: ImmerReducer<RoomModelState>;
    updateClassTime: ImmerReducer<RoomModelState>;
    updateClassStartTime: ImmerReducer<RoomModelState>;
  };
  //   subscriptions: { setup: Subscription };
}

const RoomModel: RoomModelType = {
  namespace: 'room',

  state: {
    title: '互动课堂',
    notice: '',
    ownerId: '',
    roomId: '',
    classId: '',
    docKey: '',
    classStartTime: 0,
    classDuration: 0,
  },

  effects: {
    *query({ payload }, props) {
      console.log(props);
    },
  },

  reducers: {
    setRoomDetail(state, { payload }) {
      state.title = payload.title;
      state.notice = payload.notice;
      state.ownerId = payload.ownerId;
    },
    setRoomId(state, { payload }) {
      state.roomId = payload;
    },
    setClassId(state, { payload }) {
      state.classId = payload;
    },
    setDocKey(state, { payload }) {
      state.docKey = payload;
    },
    updateClassStartTime(state, { payload }) {
      state.classStartTime = payload;
    },
    updateClassTime(state, { payload }) {
      state.classDuration = payload;
    },
  },
};

export default RoomModel;
