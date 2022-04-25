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
    docService: any
    rtcService: any
    aliyunBoardSDK: any
    classInstance: any
    createClass: any
    aliyunBoard: any
    setApp: any
  }
}

const { RoomEngine } = window.RoomPaasSdk
// import { RoomEngine } from '../RoomPaasSdk.web.min.js'

export const setConfig = async (
  roomEngine: any,
  token: AuthTokenModel,
  appKey: string,
  appId: string,
  deviceId: string,
  userId: string,
) => {
  const config = {
    // login需要的config
    appKey,
    appId,
    deviceId,
    authTokenCallback: () => Promise.resolve(token),
  }
  roomEngine.init(config)
}

export const demoDoLogin = async (uid: string) => {
  let { appKey, appId, origin } = roomEngineConfig
  const testAppId = window.localStorage.getItem('testAppId')
  const testAppKey = window.localStorage.getItem('testAppKey')
  if (testAppId) appId = testAppId
  if (testAppKey) appKey = testAppKey
  // 2. 获取engine实例
  RoomEngine.bizType = 'class'
  const roomEngine = RoomEngine.getInstance()
  window.roomEngine = roomEngine
  try {
    const deviceId = roomEngine.getDeviceId()
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
        if (res.responseSuccess) {
          const token = { ...res.result }
          return token
        }
        throw new Error(res.message)
      })
    await setConfig(roomEngine, authTokenModel, appKey, appId, deviceId, userId)
    // 4. 登录
    await roomEngine.auth(userId)
    return {
      instance: roomEngine,
      userId,
    }
  } catch (err) {
    console.error(err)
    return Promise.reject(err || new Error('登录失败，请检查登录鉴权是否过期，请生成新的链接'))
  }
}
