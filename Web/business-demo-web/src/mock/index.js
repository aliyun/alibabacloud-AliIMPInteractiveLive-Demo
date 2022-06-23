import user from '../store/user'
const Mock = require('mockjs')
console.log(user.state.userId, '=====user');
let userId = user.state.userId
let userNick = user.state.userNick
let list = []
const liveRoomsList = function(params) {
  userId = JSON.parse(localStorage.getItem('user')).userId
  userNick = JSON.parse(localStorage.getItem('user')).userNick
  let arr = [{
    'anchorId': userId,
    'anchorNick': userNick,
    'appId': 'abcdef',
    'chatId': '7d27fd46-9167-40bc-8d14-6e556fbb9b9f',
    'coverUrl': '',
    'extension': {
      'coverUrl': ''
    },
    'liveId': 'e7c231cc-c058-4583-a6e6-63aded8f32cc',
    'notice': '',
    'onlineCount': 0,
    'pv': 2,
    'roomId': '0322bd49-470c-4797-9b06-088bf4011dba',
    'status': 0,
    'title': '用户' + userNick + '的直播',
    'uv': 1
  },
  {
    'anchorId': userId,
    'anchorNick': userNick,
    'appId': 'abcdef',
    'chatId': 'e1bb6092-e795-4059-9f6d-25bbe2fafb5b',
    'coverUrl': 'https://gw.alicdn.com/imgextra/i3/O1CN01jmDcVV29uDaUHrI8g_!!6000000008127-0-tps-1024-681.jpg',
    'extension': {
      'anchorAvatarURL': 'https://gw.alicdn.com/imgextra/i4/O1CN01J9xh0a1QBeKUiazg6_!!6000000001938-2-tps-80-80.png',
      'anchorIntroduction': '品牌总监，公司品牌宣传推广负责人',
      'coverUrl': 'https://gw.alicdn.com/imgextra/i3/O1CN01jmDcVV29uDaUHrI8g_!!6000000008127-0-tps-1024-681.jpg',
      'deviceInfo': '{"a":8,"b":2861000,"c":7789932544,"d":"HIGH","e":9.6}',
      'enableLinkMic': 'false',
      'liveIntroduction': '这是一个直播简介',
      'pusherDeviceBrand': 'HUAWEI',
      'pusherDeviceModel': 'ANA-AN00',
      'pusherDeviceVersion': '10',
      'pusherOptions': '{"a":false,"b":false,"c":1500,"d":"FPS_20","e":"PUSHER_PREVIEW_ASPECT_FILL","f":"ORIENTATION_PORTRAIT","g":"GOP_FOUR","h":"ENCODE_MODE_HARD","i":"CAMERA_TYPE_FRONT","j":"RESOLUTION_720P"}'
    },
    'liveId': 'c2eb3a88-c816-4e01-936e-1e2f8d6d0b76',
    'notice': '',
    'onlineCount': 0,
    'pv': 1,
    'roomId': 'c6f4513d-41f2-4bf3-b269-da319ecdc91d',
    'status': 0,
    'title': '用户' + userNick + '的直播',
    'uv': 1
  },
  {
    'anchorId': 'senge666',
    'anchorNick': 'senge666',
    'appId': 'abcdef',
    'chatId': '9706cd8b-5acf-49c2-bfd1-c45edb27053c',
    'coverUrl': '',
    'extension': {},
    'liveId': '0aaf76af-6e78-4bae-bf4b-214806332ef4',
    'notice': '',
    'onlineCount': 0,
    'pv': 2,
    'roomId': '902b5ea2-cda7-4d98-ae51-64505c8acaec',
    'status': 2,
    'title': '互动直播',
    'uv': 2
  },
  {
    'anchorId': userId,
    'anchorNick': userNick,
    'appId': 'abcdef',
    'chatId': 'c364cde1-5558-4e55-a5b5-89029a26fbdf',
    'coverUrl': '',
    'extension': {
      'coverUrl': ''
    },
    'liveId': '40641e01-eb83-4752-9ede-e92a06856ef1',
    'notice': '',
    'onlineCount': 0,
    'pv': 1,
    'roomId': '70130008-7108-43e4-b511-56ba4fd63752',
    'status': 0,
    'title': '用户' + userNick + '的直播',
    'uv': 1
  },
  {
    'anchorId': 'Shawn7',
    'anchorNick': '用户Shawn7',
    'appId': 'abcdef',
    'chatId': 'f0ebd77b-5201-4bdb-85b6-f9805646802b',
    'coverUrl': '',
    'extension': {
      'coverUrl': ''
    },
    'liveId': '2e1e1fe5-a83e-4f92-853c-8d5179acdedd',
    'notice': '',
    'onlineCount': 0,
    'pv': 2,
    'roomId': '77a88609-08d2-4613-81b0-5dc262cd65bf',
    'status': 0,
    'title': '用户Shawn7的直播',
    'uv': 2
  },
  {
    'anchorId': 'Shawn7',
    'anchorNick': '用户Shawn7',
    'appId': 'abcdef',
    'chatId': '71b7e5c5-9dbc-4f28-a822-62f2ad339d6a',
    'coverUrl': '',
    'extension': {
      'coverUrl': ''
    },
    'liveId': 'ced904b1-5cea-430f-ab86-9520a14f9e7a',
    'notice': '',
    'onlineCount': 0,
    'pv': 1,
    'roomId': 'e53f71ad-cfe3-43a8-af6b-4b599d852ba4',
    'status': 2,
    'title': '用户Shawn7的直播',
    'uv': 1
  },
  {
    'anchorId': 'Hope9',
    'anchorNick': '用户Hope9',
    'appId': 'abcdef',
    'chatId': 'f8de061b-5281-47e5-a73e-a047e8277077',
    'coverUrl': '',
    'extension': {
      'coverUrl': ''
    },
    'liveId': '63988787-8be6-4c14-a358-c6fd7da52ab0',
    'notice': '',
    'onlineCount': 0,
    'pv': 1,
    'roomId': '68c599a0-dcdc-4557-be45-b7437c28e500',
    'status': 2,
    'title': '用户Hope9的直播',
    'uv': 1
  },
  {
    'anchorId': 'Miranda7',
    'anchorNick': '用户Miranda7',
    'appId': 'abcdef',
    'chatId': 'eb15e9a0-1947-471f-83c9-433c9a9d3530@abcdef',
    'coverUrl': '',
    'extension': {
      'coverUrl': ''
    },
    'liveId': '85cb8f2d-0dff-479f-8321-b2bd5f7a71a7',
    'notice': '',
    'onlineCount': 0,
    'pv': 6,
    'roomId': 'eb15e9a0-1947-471f-83c9-433c9a9d3530',
    'status': 0,
    'title': '用户Miranda7的直播',
    'uv': 4
  }]
  list.unshift(...arr)
  const data = {
    result: {
      totalCount: list.length,
      liveList: list
    }
  }
  return data
}
Mock.mock(RegExp('/listLiveRooms.*'), 'get', liveRoomsList)

const createLiveRoom = function (params) {
  const body = JSON.parse(params.body)
  const liveRoom = {
    'anchorId': body.anchorId,
    'anchorNick': body.anchorNick,
    'appId': 'abcdef',
    'chatId': '7d27fd46-9167-40bc-8d14-6e556fbb9b9e',
    'coverUrl': body.coverUrl,
    'extension': body.extension,
    'liveId': 'e7c231cc-c058-4583-a6e6-63aded8f32ca',
    'notice': body.notice,
    'onlineCount': 0,
    'pv': 0,
    'roomId': '0322bd49-470c-4797-9b06-088bf4011dbc',
    'status': 0,
    'title': body.title,
    'uv': 0
  }
  list.push(liveRoom)
  return true
}
Mock.mock('/createLiveRoom', 'post', createLiveRoom)

const getLiveRoom = function name(params) {
  const liveId = params.url.split('?')[1].split('=')[1]
  const info = list.filter(item => {
    return item.liveId === liveId
  })
  const data = {
    result: {
      'anchorId': info[0].anchorId,
      'anchorNick': info[0].anchorNick,
      'appId': 'abcdef',
      'artcInfo': {
        'artcH5Url': 'artc://demo-livepaas.dingtalk.com/live/8ee28a41-e78a-4137-a3a6-4b96c9f9e8ae_origin-RTS?auth_key=1656482140-0-0-fb0e2d93e554f25a03a6ab7262158fb7',
        'artcUrl': 'artc://demo-livepaas.dingtalk.com/live/8ee28a41-e78a-4137-a3a6-4b96c9f9e8ae?auth_key=1656482140-0-0-8fb7e5bdd017e4795d89157fedc8cd09'
      },
      'chatId': info[0].chatId,
      'createTime': 1655875486000,
      'endTime': info[0].endTime || 0,
      'extension': {},
      'hlsUrl': 'http://demo-livepaas.dingtalk.com/live/8ee28a41-e78a-4137-a3a6-4b96c9f9e8ae.m3u8?auth_key=1656482140-0-0-f765a9bccd8f323739b81a0e408fb074',
      'hlsUrlHttps': 'https://demo-livepaas.dingtalk.com/live/8ee28a41-e78a-4137-a3a6-4b96c9f9e8ae.m3u8?auth_key=1656482140-0-0-f765a9bccd8f323739b81a0e408fb074',
      'liveId': info[0].liveId,
      'liveUrl': 'http://demo-livepaas.dingtalk.com/live/8ee28a41-e78a-4137-a3a6-4b96c9f9e8ae.flv?auth_key=1656482140-0-0-fa5fcad44991c8cde89c63da751a8587',
      'liveUrlHttps': 'https://demo-livepaas.dingtalk.com/live/8ee28a41-e78a-4137-a3a6-4b96c9f9e8ae.flv?auth_key=1656482140-0-0-fa5fcad44991c8cde89c63da751a8587',
      'onlineCount': info[0].onlineCount,
      'playbackUrl': 'http://stanard-demo-livepaas.dingtalk.com/live/record/live/8ee28a41-e78a-4137-a3a6-4b96c9f9e8ae_live.m3u8?auth_key=1655879140-0-0-23607d66c235d3d9fffebce5a30761d9',
      'playbackUrlHttps': 'https://stanard-demo-livepaas.dingtalk.com/live/record/live/8ee28a41-e78a-4137-a3a6-4b96c9f9e8ae_live.m3u8?auth_key=1655879140-0-0-23607d66c235d3d9fffebce5a30761d9',
      'pluginInstanceInfoList': [
        {
          'createTime': 1655875486000,
          'pluginId': '96741186-d3c7-4b65-8531-ff160b512bba',
          'pluginType': 'chat'
        },
        {
          'createTime': 1655875486000,
          'pluginId': '8ee28a41-e78a-4137-a3a6-4b96c9f9e8ae',
          'pluginType': 'live'
        }
      ],
      'pushUrl': 'rtmp://demo-livepaas-push.dingtalk.com/live/8ee28a41-e78a-4137-a3a6-4b96c9f9e8ae?auth_key=1656482140-0-0-8fb7e5bdd017e4795d89157fedc8cd09',
      'pv': info[0].pv,
      'roomId': info[0].roomId,
      'rtmpUrl': 'rtmp://demo-livepaas.dingtalk.com/live/8ee28a41-e78a-4137-a3a6-4b96c9f9e8ae?auth_key=1656482140-0-0-8fb7e5bdd017e4795d89157fedc8cd09',
      'startTime': info[0].startTime || 0,
      'status': info[0].status,
      'title': info[0].title,
      'uv': info[0].uv
    }
  }
  return data
}
Mock.mock(RegExp('/getLiveRoom.*'), 'get', getLiveRoom)

const getToken = function (params) {
  const res = {
    result: {
      'accessToken': 'oauth_cloud_key:tF2fFeDosu9rrnsAdAUFyHGhZV1U0JcSOR8TepohKq+BLcxCkb8EBLgk3GVk0lpoCJ33hoWdbemqX7JIyYCP1AB604epeAU9H5FlkV+plCJ8jhaO65hp0KPC9v2GiAaZ',
      'accessTokenExpiredTime': 86400000,
      'refreshToken': 'oauth_cloud_key:DKYNVMKFrmEMbbPt8ZVPdzIlzXN2lzH84xR1IoESKjsxr7QbEcKbj7U8iNrRPrRoCkXdn6rMliNLqIAPBLMwlXYQ6tzUlM1x8F50EOF3K+U='
    }
  }
  return res
}

Mock.mock(RegExp('/getAuthToken.*'), 'get', getToken)

const updateLiveRoom = function (params) {
  const body = JSON.parse(params.body)
  list.forEach(item => {
    if (item.liveId === body.liveId) {
      item.title = body.title
      item.notice = body.notice
      item.coverUrl = body.coverUrl
      item.anchorId = body.anchorId
      item.userId = body.userId
      item.extension = body.extension
    }
  })
}
Mock.mock('/updateLiveRoom', 'post', updateLiveRoom)

const getStandardRoomJumpUrl = function (params) {
  const data = {
    result: {
      standardRoomJumpUrl: 'aliyunclient://121.41.56.125:8080/entry/standard_live?nick=741&liveId=8ee28a41-e78a-4137-a3a6-4b96c9f9e8ae&userAuthSession=66a5ef38-723c-4b57-acfb-4e92342c030f'
    }
  }
  return data
}
Mock.mock(RegExp('/getStandardRoomJumpUrl.*'), 'get', getStandardRoomJumpUrl)

const publishLiveRoom = function(params) {
  const body = JSON.parse(params.body)
  list.forEach(item => {
    if (item.liveId === body.liveId) {
      item.status = 1
      item.pv = 1
      item.uv = 1
      item.onlineCount = 1
      item.startTime = +new Date()
    }
  })
  const data = {
    result: {
      'liveId': '9549af40-48e2-4c01-aa12-4c5cc689fecf',
      'liveUrl': 'http://demo-livepaas.dingtalk.com/live/9549af40-48e2-4c01-aa12-4c5cc689fecf.flv?auth_key=1656486658-0-0-509ab7a2dafc6e72c18ab4eab781282c',
      'pushUrl': 'rtmp://demo-livepaas-push.dingtalk.com/live/9549af40-48e2-4c01-aa12-4c5cc689fecf?auth_key=1656486658-0-0-c4638f1da389594d8469d10cd81188ec'
    }
  }
  return data
}
Mock.mock('/publishLiveRoom', 'post', publishLiveRoom)

const stopLiveRoom = function (params) {
  const body = JSON.parse(params.body)
  list.forEach(item => {
    if (item.liveId === body.liveId) {
      item.status = 2
      item.onlineCount = 0
      item.endTime = +new Date()
    }
  })
  return true
}
Mock.mock('/stopLiveRoom', 'post', stopLiveRoom)
