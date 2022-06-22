import qs from 'qs';
import roomEngineConfig from '../constants/config';

interface AuthTokenModel {
  accessToken: string;
  refreshToken: string;
  accessTokenExpiredTime: number;
}

const { RoomEngine } = window.RoomPaasSdk;

export const setConfig = (
  roomEngine: any,
  token: AuthTokenModel,
  appKey: string,
  appId: string,
  deviceId: string,
) => {
  const config = {
    // login需要的config
    appKey,
    appId,
    deviceId,
    authTokenCallback: () => Promise.resolve(token),
  };
  roomEngine.init(config);
};

export const doLogin = async (nickname: string, userId: string) => {
  const { appId, appKey, origin } = roomEngineConfig;
  window.sessionStorage.setItem('nickname', nickname);
  window.sessionStorage.setItem('userId', userId);
  // 获取engine实例
  const roomEngine = RoomEngine.getInstance();
  window.roomEngine = roomEngine;
  try {
    const deviceId = roomEngine.getDeviceId();
    const queryString = qs.stringify({
      // 如果验签，属性顺序需与服务端一致
      appId,
      appKey,
      deviceId: encodeURIComponent(deviceId),
      userId,
    });
    const authTokenModel = await fetch(
      // 如果配置本地proxy跨域，请去掉origin
      `${origin}/api/login/getToken?${queryString}`,
      {
        method: 'POST',
      },
    )
      .then((res) => res.json())
      .then((res) => {
        if (res) {
          const token = { ...res.result };
          window.localStorage.setItem('token', JSON.stringify(token)); // 保存token信息
          window.localStorage.setItem(
            'authTimeStamp',
            (+new Date()).toString(),
          ); // 保存token的时间
          return token;
        }
        throw new Error('没有获取到Token');
      })
      .catch((err) => {
        console.error(err);
      });
    setConfig(roomEngine, authTokenModel, appKey, appId, deviceId);
    // 4. 登录
    await roomEngine.auth(userId);
    return roomEngine;
  } catch (err) {
    console.error(err);
    return Promise.reject(new Error('登录失败，请检查设置'));
  }
};
