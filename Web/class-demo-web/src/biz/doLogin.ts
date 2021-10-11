import roomEngineConfig from '@/constants/config';
import { getWhiteBoard } from './getWhiteBoard';
import { createRoom, getToken } from '@/api/apis';

declare global {
  interface Window {
    RoomPaasSdk: any;
    roomChannel: any;
    roomEngine: any;
    rtcService: any;
    chatService: any;
    liveService: any;
    wbService: any;
    aliyunBoardSDK: any;
    createRoom: any;
  }
}
export const doCreateRoom = (userId: any, roomId: any) => {
  const { appId } = roomEngineConfig;
  try {
    const params = {
      appId,
      templateId: 'default',
      title: 'titleTest',
      notice: 'noticeTest',
      roomId,
      roomOwnerId: userId,
    };
    return createRoom(params)
      .then((res) => {
        return res.responseSuccess;
      })
      .catch((err) => {
        console.error(err);
        throw err;
      });
  } catch (err: any) {
    console.log(err);
    return Promise.reject(err);
  }
};
export const getRoomEngine = async (userId: any) => {
  const { appKey, appId } = roomEngineConfig;
  const { RoomEngine } = window.RoomPaasSdk;
  const roomEngineInstance = RoomEngine.getInstance();
  const deviceId = encodeURIComponent(roomEngineInstance.getDeviceId()); // 获取deviceId
  window.roomEngine = roomEngineInstance;
  try {
    const config = {
      appKey,
      appId,
      deviceId,
      authTokenCallback: async () => {
        // 用来获取token的回调
        /***
         * 如果需要验证签名流程，可参考以下流程
         * 验签流程也可以自己实现，具体与自己服务端同学商定
         * 不需要则直接fetch/xhr 相应接口即可
         */
        const params = {
          appId,
          appKey,
          deviceId,
          userId,
        };
        // 返回一个promise
        return getToken(params)
          .then((res) => {
            if (res) {
              try {
                const authToken = res.result;
                return { ...authToken, uid: userId };
              } catch (err: any) {
                throw new Error(err);
              }
            }
            throw new Error('token is null');
          })
          .catch((err) => {
            console.error(err);
          });
      },
    };
    // 构造完config后调用init初始化
    roomEngineInstance.init(config);
    await roomEngineInstance.auth(userId);
  } catch (err) {
    console.log(err);
    throw err;
  }
};
export const doLogin = async (userId: any, roomId: any) => {
  window.sessionStorage.setItem('roomId', roomId);
  try {
    const roomChannel = await window.roomEngine.getRoomChannel(roomId);
    window.roomChannel = roomChannel;
    const roomDetail = await roomChannel.enterRoom(userId);
    window.rtcService = window.roomChannel.getPluginService('rtc');
    window.chatService = window.roomChannel.getPluginService('chat');
    window.liveService = window.roomChannel.getPluginService('live');
    window.wbService = window.roomChannel.getPluginService('wb');
    const docKey = await window.wbService.getDockey();
    window.sessionStorage.setItem('docKey', docKey);
    const url = `https://***.***.com?userId=${userId}&docKey=${window.sessionStorage.getItem(
      'docKey',
    )}`;
    fetch(url, {
      method: 'POST',
    })
      .then((res: any) => res.json())
      .then((res: any) => {
        window.sessionStorage.setItem(
          'accessToken',
          res.result.documentAccessInfo.accessToken,
        );
      })
      .catch((err: any) => {
        console.log(err);
      });
    getWhiteBoard(userId);
    window.roomEngine.getRoomDetail(roomId).then((res: any) => {
      window.sessionStorage.setItem('ownerId', res.ownerId);
    });
    console.log('==enterRoom==>', roomDetail);
  } catch (err) {
    console.log(err);
    throw err;
  }
};
