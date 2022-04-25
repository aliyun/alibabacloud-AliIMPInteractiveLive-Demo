import { BasicMap } from '@/utils'
import { User } from '@/models/user'

export const listAllUsers = async (singleCount = 30) => {
  let result: any[] = []
  let hasMore = true
  let page = 1
  try {
    while (hasMore) {
      const res = await window.roomChannel.listUser(page, singleCount)
      hasMore = res.hasMore
      page += 1
      result = [...result, ...res.userList]
    }
    return result
  } catch (err) {
    console.error(err)
    throw err
  }
}

export const generateUserList = (
  userList: any[],
  ownerId: string,
  myId: string,
  applyList?: any[],
  confList?: any[],
  isInClass?: boolean,
): BasicMap<User> => {
  const list: BasicMap<User> = {}
  userList.forEach((item: any) => {
    if (list[item.userId]) return
    if (item.nick === 'SystemRecorder') return
    list[item.userId] = {
      userId: item.userId,
      nick: item.nick,
      isOwner: item.userId === ownerId,
      isMe: item.userId === myId,
      isInSeat: item.userId === ownerId && !!isInClass,
      isApplying: false,
      isRtcMute: false,
      isRtcMuteCamera: false,
      isInviting: false,
      streamType: 1,
      subscribeResult: false,
      enterRoomTime: item.userId === ownerId ? 0 : +new Date(),
      enterSeatTime: item.userId === ownerId ? 0 : +new Date(),
      isCurrent: false,
    }
  })
  if (confList) {
    // 入会状态 1：初始状态 2：呼叫状态 3：会议中 4：入会失败 5：被踢出 6：离会
    confList.forEach((item: any) => {
      if (!list[item.userId]) return
      list[item.userId].isInSeat = item.status === 3 || item.status === 4
      list[item.userId].isRtcMuteCamera = item.cameraStatus === 0
      list[item.userId].isRtcMute = item.micphoneStatus === 0
    })
  }
  if (applyList) {
    applyList.forEach((item: any) => {
      if (list[item.userId]) {
        list[item.userId].isApplying = item.status === 2
      }
    })
  }
  return list
}
