import qs from 'qs'
import roomEngineConfig from '@/constants/config'

interface AuthTokenModel {
  accessToken: string
  refreshToken: string
  accessTokenExpiredTime: number
}

declare global {
  interface Window {
    RoomPaasSdk: any
    roomEngine: any
    roomChannel: any
    chatService: any
    liveService: any
    wbService: any
    rtcService: any
    aliyunBoardSDK: any
    classInstance: any
    createClass: any
  }
}

const { RoomEngine } = window.RoomPaasSdk

export const setConfig = (
  roomEngineInstance: any,
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
    wsUrl: window.localStorage.getItem('pre') ? 'wss://pre-wss.im.dingtalk.cn' : 'wss://wss.im.dingtalk.cn',
    authTokenCallback: () => Promise.resolve(token),
  }
  roomEngineInstance.init(config)
}

export const doLogin = async (userAuthSession: string) => {
  // 2. 获取engine实例
  const roomEngineInstance = RoomEngine.getInstance()
  window.roomEngine = roomEngineInstance
  try {
    const deviceId = roomEngineInstance.getDeviceId()
    const res = await fetch(
      `/api/login/checkUserAuthSessionV2?${qs.stringify({
        deviceId: encodeURIComponent(deviceId),
        userAuthSession,
      })}`,
    )
      .then((res) => res.json())
      .then((res) => {
        if (res) {
          return res.result
        }
        throw new Error('没有获取到Token')
      })
      .catch((err) => {
        console.error(err)
      })
    const { appId, appKey, authTokenModel, userId } = res
    setConfig(roomEngineInstance, authTokenModel, appKey, appId, deviceId)
    // 4. 登录
    await roomEngineInstance.auth(userId)
    return {
      instance: roomEngineInstance,
      userId,
    }
  } catch (err) {
    console.error(err)
    return Promise.reject(new Error('登录失败，请检查设置'))
  }
}

export const demoDoLogin = async (uid: string) => {
  const { appKey, appId, origin } = roomEngineConfig
  // 2. 获取engine实例
  const roomEngineInstance = RoomEngine.getInstance()
  window.roomEngine = roomEngineInstance
  try {
    const deviceId = roomEngineInstance.getDeviceId()
    const userId = uid || 'test'
    const queryString = qs.stringify({
      // 如果验签，属性顺序需与服务端一致
      appId,
      appKey,
      deviceId: encodeURIComponent(deviceId),
      userId,
    })
    const authTokenModel = await fetch(`${origin}/api/login/getToken?${queryString}`, {
      method: 'POST',
    })
      .then((res) => res.json())
      .then((res) => {
        if (res) {
          const token = { ...res.result }
          return token
        }
        throw new Error('没有获取到Token')
      })
      .catch((err) => {
        console.error(err)
      })
    setConfig(roomEngineInstance, authTokenModel, appKey, appId, deviceId)
    // 4. 登录
    await roomEngineInstance.auth(userId)
    return {
      instance: roomEngineInstance,
      userId,
    }
  } catch (err) {
    console.error(err)
    return Promise.reject(new Error('登录失败，请检查设置'))
  }
}
