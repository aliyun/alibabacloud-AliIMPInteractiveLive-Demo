export const reSubUser = (userId: string, isOwner: boolean) => {
  window.rtcService
    .unSubscribe(userId)
    .then(() => {
      return window.rtcService.subscribe(userId)
    })
    .then(() => {
      const teacherDom = document.getElementById('teacher')
      const dom =
        isOwner && teacherDom
          ? teacherDom
          : document.getElementById(`video-${userId}`) || document.createElement('video')
      const publishInfo = window.rtcService.getPublishInfo(userId)
      window.rtcService.setDisplayRemoteVideo(
        dom,
        userId,
        publishInfo.indexOf('sophon_video_screen_share') > -1 ? 2 : 1,
      )
    })
}
