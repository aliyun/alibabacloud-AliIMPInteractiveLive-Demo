import qs from 'qs';
import SignUtils from '../utils/SignUtils';
import { formatDate } from '../utils';
import roomEngineConfig from '../constants/config';
import { getAuthStatus } from '../biz/getAuthStatus';

// 1. 获取engine
const { RoomEngine } = window.RoomPaasSdk;

// 配置登录用的config
const { appKey, appId, signSecret, origin } = roomEngineConfig;

const authTokenCallbackCreator = (deviceId: string, userId: string) => {
  return () => {
    // 返回promise的函数，获取authToken用
    /***
     * 验证签名流程，可参考以下流程
     */
    const path = 'api/login/getToken';
    const query = {
      // 如果验签，属性顺序需与服务端一致
      appId,
      appKey,
      deviceId: encodeURIComponent(deviceId),
      userId,
    };
    // 构造queryString
    const queryString = qs.stringify(query);
    const headers = {
      // 如果验签，属性顺序需与服务端一致
      'a-app-id': 'imp-room',
      'a-signature-method': SignUtils.SIGNATURE_METHOD,
      'a-signature-nonce': Math.floor(Math.random() * 10000).toString(),
      'a-signature-version': SignUtils.SIGNATURE_VERSION,
      'a-timestamp': formatDate(new Date(), 'yyyy-MM-ddThh:mm:ssZ'),
    };
    // 构造header queryString
    const headerString = qs.stringify(headers);
    const signString = SignUtils.buildSignString(
      'POST',
      `${origin}/${path}`,
      queryString,
      headerString,
    );
    // 生成签名
    const signature = SignUtils.generateSignature(signString, signSecret + '&');
    return fetch(`${origin}/${path}?${queryString}`, {
      method: 'POST',
      // 在生成签名后从header里带入
      headers: {
        ...headers,
        'a-signature': signature,
      },
    })
      .then((res) => res.json())
      .then((res) => {
        if (res) {
          const authToken = res.result;
          const token = { ...authToken };
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
  };
};

export const setConfig = (
  roomEngineInstance: any,
  userId: string,
  appKey: string,
  appId: string,
) => {
  const deviceId = encodeURIComponent(roomEngineInstance.getDeviceId()); // 获取deviceId
  const authed = getAuthStatus();
  let localToken = '';
  if (authed)
    localToken = JSON.parse(window.localStorage.getItem('token') || '');
  const config = {
    // login需要的config
    appKey,
    appId,
    deviceId,
    authTokenCallback: authTokenCallbackCreator(deviceId, userId),
  };
  roomEngineInstance.init(config);
};

export const doLogin = async (nickname: string, uid?: string) => {
  // 2. 获取engine实例
  const roomEngineInstance = RoomEngine.getInstance();
  window.roomEngine = roomEngineInstance;
  try {
    // demo通过昵称判断是否登录过，信息存在localStorage中，没有的话就随机个数字当userId
    let userId = '';
    console.log(uid);
    if (uid) {
      userId = uid;
    } else {
      const savedUserList = window.localStorage.getItem('userList');
      const userList = savedUserList ? JSON.parse(savedUserList) : {};
      userId = userList[nickname] || Math.floor(Math.random() * 1000000);
      window.localStorage.setItem(
        'userList',
        JSON.stringify(
          Object.assign(userList, {
            [nickname]: userId,
          }),
        ),
      );
      window.localStorage.setItem('nickname', nickname);
    }
    // 3. 调用初始化方法
    setConfig(roomEngineInstance, userId, appKey, appId);
    // 4. 登录
    await roomEngineInstance.auth(userId);
    return roomEngineInstance;
  } catch (err) {
    console.error(err);
    throw err;
  }
};
