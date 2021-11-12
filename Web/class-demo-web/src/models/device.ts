import { Effect, ImmerReducer, Subscription } from 'umi';

export interface Device {
  deviceId: string;
  groupId: string;
  kind: string;
  label: string;
}

export interface DeviceModelState {
  audioDevices: Device[];
  videoDevices: Device[];
  currentAudioDevice: string;
  currentVideoDevice: string;
}

export interface DeviceModelType {
  namespace: 'device';
  state: DeviceModelState;
  effects: {
    query: Effect;
  };
  reducers: {
    setVideoDevices: ImmerReducer<DeviceModelState>;
    setAudioDevices: ImmerReducer<DeviceModelState>;
    setCurrentVideoDevice: ImmerReducer<DeviceModelState>;
    setCurrentAudioDevice: ImmerReducer<DeviceModelState>;
  };
  //   subscriptions: { setup: Subscription };
}

const DeviceModel: DeviceModelType = {
  namespace: 'device',

  state: {
    audioDevices: [],
    videoDevices: [],
    currentAudioDevice: 'default',
    currentVideoDevice: 'default',
  },

  effects: {
    *query({ payload }, props) {
      console.log(props);
    },
  },

  reducers: {
    setVideoDevices(state, { payload }) {
      state.videoDevices = payload;
    },
    setAudioDevices(state, { payload }) {
      state.audioDevices = payload;
    },
    setCurrentVideoDevice(state, { payload }) {
      state.currentVideoDevice = payload;
    },
    setCurrentAudioDevice(state, { payload }) {
      state.currentAudioDevice = payload;
    },
  },
};

export default DeviceModel;
