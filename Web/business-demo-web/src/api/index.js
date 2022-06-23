import { api } from './axios'
window.api = api

export const liveApi = {
  getToken({
    appKey,
    userId,
    deviceId
  } = {}) {
    return api.get('/getAuthToken', {
      params: {
        appKey,
        userId,
        deviceId
      }
    })
  },
  listLiveRooms({
    status,
    pageNumber,
    pageSize
  } = {}) {
    return api.get('/listLiveRooms', {
      params: {
        status,
        pageNumber,
        pageSize
      }
    })
  },
  createLiveRoom({
    title,
    notice,
    coverUrl,
    anchorId,
    anchorNick,
    userId,
    extension
  } = {}) {
    return api.post('/createLiveRoom', {
      title,
      notice,
      coverUrl,
      anchorId,
      anchorNick,
      userId,
      extension
    })
  },
  getLiveRoom({
    liveId
  } = {}) {
    return api.get('/getLiveRoom', {
      params: {
        liveId
      }
    })
  },
  updateLiveRoom({
    title,
    notice,
    coverUrl,
    anchorId,
    anchorNick,
    userId,
    extension,
    liveId
  } = {}) {
    return api.post('/updateLiveRoom', {
      title,
      notice,
      coverUrl,
      anchorId,
      anchorNick,
      userId,
      extension,
      liveId
    })
  },
  stopLiveRoom({
    liveId,
    userId
  } = {}) {
    return api.post('/stopLiveRoom', {
      liveId,
      userId
    })
  },
  publishLiveRoom({
    liveId,
    userId
  } = {}) {
    return api.post('/publishLiveRoom', {
      liveId,
      userId
    })
  },
  getStandardRoomJumpUrl({
    bizId,
    userId,
    userNick,
    platform
  } = {}) {
    return api.get('/getStandardRoomJumpUrl', {
      params: {
        bizId,
        userId,
        userNick,
        platform: platform || 'web',
        bizType: 'live'
      }
    })
  }
}
