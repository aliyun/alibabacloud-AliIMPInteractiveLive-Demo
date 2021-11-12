import { ImmerReducer } from 'umi';
import { BasicMap } from '@/utils';

export interface User {
  userId: string;
  nick: string;
  isOwner: boolean;
  isMe: boolean;
  isInSeat: boolean; // 是否已经连麦
  isApplying: boolean; // 是否正在申请
  isRtcMute: boolean; // 是否连麦静音
  isRtcMuteCamera: boolean; // 是否关闭摄像头
  isInviting: boolean; // 是否正在邀请中
  streamType: number; // 订阅的流类型
  subscribeResult: boolean; // 是否已订阅此人
  [index: string]: any;
}

export interface UserModelState {
  userId: string;
  nick: string;
  isOwner: boolean;
  userList: BasicMap<User>;
  userUpdate: number;
}

export interface UserModelType {
  namespace: 'user';
  state: UserModelState;
  reducers: {
    setUserId: ImmerReducer<UserModelState>;
    setNick: ImmerReducer<UserModelState>;
    setUserList: ImmerReducer<UserModelState>;
    setIsOwner: ImmerReducer<UserModelState>;
    addUser: ImmerReducer<UserModelState>;
    updateUser: ImmerReducer<UserModelState>;
    deleteUser: ImmerReducer<UserModelState>;
  };
}

export const generateUserList = (
  userList: any[],
  ownerId: string,
  myId: string,
  applyList?: any[],
  confList?: any[],
): BasicMap<User> => {
  console.log(userList, applyList, confList);
  const list: BasicMap<User> = {};
  console.log(ownerId, myId, '--------------');
  userList.forEach((item: any) => {
    if (list[item.userId]) return;
    list[item.userId] = {
      userId: item.userId,
      nick: item.nick,
      isOwner: item.userId === ownerId,
      isMe: item.userId === myId,
      isInSeat: false,
      isApplying: false,
      isRtcMute: false,
      isRtcMuteCamera: false,
      isInviting: false,
      streamType: 1,
      subscribeResult: false,
    };
    console.log(list[item.userId]);
  });
  if (confList) {
    confList.forEach((item: any) => {
      if (!list[item.userId]) return;
      list[item.userId].isInSeat = item.status === 3 || item.status === 4;
      list[item.userId].isRtcMuteCamera = item.cameraStatus === 0;
      list[item.userId].isRtcMute = item.micphoneStatus === 0;
    });
  }
  if (applyList) {
    applyList.forEach((item: any) => {
      list[item.userId].isApplying = item.status === 2;
    });
  }
  return list;
};

const UserModel: UserModelType = {
  namespace: 'user',

  state: {
    userId: '',
    nick: '',
    isOwner: false,
    userList: {},
    userUpdate: 0,
  },

  reducers: {
    setUserId(state, action) {
      state.userId = action.payload;
    },
    setNick(state, action) {
      state.nick = action.payload;
    },
    setUserList(state, action) {
      state.userList = action.payload;
    },
    addUser(state, action) {
      state.userList[action.payload.userId] = action.payload;
    },
    updateUser(state, action) {
      Object.keys(action.payload).forEach((key: string) => {
        if (state.userList[action.payload.userId]) {
          state.userList[action.payload.userId][key] = action.payload[key];
        }
      });
      state.userUpdate += 1;
    },
    deleteUser(state, action) {
      delete state.userList[action.payload];
    },
    setIsOwner(state, action) {
      state.isOwner = action.payload;
    },
  },
};

export default UserModel;
