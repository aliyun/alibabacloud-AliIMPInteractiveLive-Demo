import { FC, useEffect, useState } from 'react'
import { RoomModelState, StatusModelState, UserModelState, connect, Dispatch } from 'umi'
import { Checkbox, message } from 'antd'
import styles from './index.less'

interface PageProps {
  room: RoomModelState
  status: StatusModelState
  user: UserModelState
  dispatch: Dispatch
  from: 'teacher' | 'student'
}

const UserList: FC<PageProps> = ({ room, status, user, dispatch, from }) => {
  const muteAllMicHandler = () => {
    if (!status.isInClass) {
      message.error('课程未开始')
      return
    }
    if (!status.isRtcMuteAll) {
      Object.values(user.userList)
        .filter((item) => item.isInSeat && item.isRtcMute)
        .forEach((item) => {
          window.rtcService.muteRemoteMic(item.userId, false)
          dispatch({
            type: 'user/updateUser',
            payload: {
              userId: item.userId,
              isRtcMute: false,
            },
          })
        })
      dispatch({
        type: 'status/setIsRtcMuteAll',
        payload: false,
      })
    } else {
      Object.values(user.userList)
        .filter((item) => item.isInSeat && !item.isRtcMute)
        .forEach((item) => {
          window.rtcService.muteRemoteMic(item.userId, true)
          dispatch({
            type: 'user/updateUser',
            payload: {
              userId: item.userId,
              isRtcMute: true,
            },
          })
        })
      dispatch({
        type: 'status/setIsRtcMuteAll',
        payload: true,
      })
    }
  }
  const muteMicHandler = (userId: string) => {
    window.rtcService
      .muteRemoteMic(userId, !user.userList[userId].isRtcMute)
      .then(() => {
        message.error(`${user.userList[userId].nick}${user.userList[userId].isRtcMute ? '取消' : '静音'}成功`)
        dispatch({
          type: 'user/updateUser',
          payload: {
            userId,
            isRtcMute: !user.userList[userId].isRtcMute,
          },
        })
      })
      .catch((err: any) => {
        console.error(err)
        message.error('操作失败')
      })
  }
  const kickRtcHandler = (userId: string) => {
    window.rtcService
      .kickUserFromChannel([userId])
      .then(() => {
        dispatch({
          type: 'user/updateUser',
          payload: {
            userId,
            isInSeat: false,
          },
        })
        message.info(`已将${user.userList[userId].nick}下麦`)
      })
      .catch((err: any) => {
        console.error(err)
        message.error('操作失败')
      })
  }
  const agreeLinkHandler = (userId: string) => {
    window.rtcService
      .handleApplyJoinChannel(userId, true)
      .then(() => {
        dispatch({
          type: 'user/updateUser',
          payload: {
            userId,
            isApplying: false,
          },
        })
        window.rtcService.inviteJoinChannel([
          {
            userId,
            tenantId: '',
            nickname: user.userList[userId].nickname,
          },
        ])
      })
      .catch((err: any) => {
        console.error(err)
        message.error('操作失败')
      })
  }
  const refuseLinkHandler = (userId: string) => {
    window.rtcService
      .handleApplyJoinChannel(userId, false)
      .then(() => {
        dispatch({
          type: 'user/updateUser',
          payload: {
            userId,
            isApplying: false,
          },
        })
      })
      .catch((err: any) => {
        console.error(err)
        message.error('操作失败')
      })
  }
  const inviteLinkHandler = (userId: string) => {
    if (!status.isInClass) {
      message.error('课程未开始')
      return
    }
    console.log(user.userList[userId])
    if (user.userList[userId].isInviting) return
    window.rtcService
      .inviteJoinChannel([
        {
          userId,
          tenantId: '',
          nickname: user.userList[userId].nickname,
        },
      ])
      .then(() => {
        dispatch({
          type: 'user/updateUser',
          payload: {
            userId,
            isInviting: true,
          },
        })
        message.info(`已邀请${user.userList[userId].nick}连麦`)
      })
      .catch((err: any) => {
        console.error(err)
        message.error('操作失败')
      })
  }
  return (
    <div className={styles['user-list-container']}>
      <div className={styles['user-list-main']}>
        {Object.values(user.userList).filter((item) => item.isInSeat).length > 0 && (
          <div className={styles['link-mic-list']}>
            {Object.values(user.userList)
              .filter((item) => item.isInSeat)
              .map((item, index) => (
                <div className={styles['list-item']} key={index}>
                  <div className={styles['user-info']}>
                    <div className={styles.avatar}>
                      {/* <img src="https://img.alicdn.com/imgextra/i1/O1CN01uLdFRY1mGc6BPQKvm_!!6000000004927-0-tps-1500-1000.jpg" /> */}
                      <div className={styles['name-avatar']}>{item.nick.substr(0, 1).toUpperCase()}</div>
                    </div>
                    <div className={styles['name-part']}>
                      <div className={styles.name} title={item.nick}>
                        <span>{item.nick}</span>
                        {item.isMe && <span>(自己)</span>}
                      </div>
                      {item.isInSeat && <div className={styles['sub-info']}>已连麦</div>}
                    </div>
                  </div>
                  <div className={styles['user-operation']}>
                    <div className={styles['user-operation-item']}>
                      {item.isRtcMuteCamera ? (
                        <svg className="icon" aria-hidden="true">
                          <use xlinkHref="#icon-ic_toolbar_guanshexiangtou"></use>
                        </svg>
                      ) : (
                        <svg className="icon" aria-hidden="true">
                          <use xlinkHref="#icon-ic_toolbar_shexiangtou"></use>
                        </svg>
                      )}
                    </div>
                    <div className={styles['user-operation-item']} onClick={() => muteMicHandler(item.userId)}>
                      {item.isRtcMute ? (
                        <svg className="icon" aria-hidden="true">
                          <use xlinkHref="#icon-ic_toolbar_quxiaojingyin"></use>
                        </svg>
                      ) : (
                        <svg className="icon" aria-hidden="true">
                          <use xlinkHref="#icon-ic_toolbar_jingyin"></use>
                        </svg>
                      )}
                    </div>
                    {!item.isOwner && (
                      <div className={styles['user-operation-item']} onClick={() => kickRtcHandler(item.userId)}>
                        <div className={`${styles['btn']} ${styles['btn-danger']}`}>下麦</div>
                      </div>
                    )}
                  </div>
                </div>
              ))}
          </div>
        )}
        <div className={styles['normal-list']}>
          {Object.values(user.userList)
            .filter((item) => !item.isInSeat)
            .map((item, index) => (
              <div className={styles['list-item']} key={index}>
                <div className={styles['user-info']}>
                  <div className={styles.avatar}>
                    <div className={styles['name-avatar']}>{item.nick.substr(0, 1).toUpperCase()}</div>
                  </div>
                  <div className={styles['name-part']}>
                    <div className={styles.name} title={item.nick}>
                      <span>{item.nick}</span>
                      {item.isMe && <span>(自己)</span>}
                    </div>
                    {item.isApplying && <div className={styles['sub-info']}>申请连麦中</div>}
                  </div>
                </div>
                {item.isApplying ? (
                  <div className={styles['user-operation']}>
                    <div className={styles['user-operation-item']} onClick={() => agreeLinkHandler(item.userId)}>
                      <div className={`${styles['btn']}`}>同意</div>
                    </div>
                    <div className={styles['user-operation-item']} onClick={() => refuseLinkHandler(item.userId)}>
                      <div className={`${styles['btn']} ${styles['btn-danger']}`}>拒绝</div>
                    </div>
                  </div>
                ) : (
                  <div className={styles['user-operation']}>
                    {!item.isMe && status.isInClass && (
                      <div className={styles['user-operation-item']} onClick={() => inviteLinkHandler(item.userId)}>
                        {item.isInviting ? (
                          <div className={`${styles['btn']}`}>邀请中</div>
                        ) : (
                          <div className={`${styles['btn']}`}>邀请</div>
                        )}
                      </div>
                    )}
                  </div>
                )}
              </div>
            ))}
        </div>
      </div>
      <div className={styles['operation-bar']}>
        <div className={styles['operation-item']}>
          <Checkbox onChange={muteAllMicHandler} className={styles.checkbox}>
            全员静音
          </Checkbox>
        </div>
      </div>
    </div>
  )
}

export default connect(
  ({ room, status, user }: { room: RoomModelState; status: StatusModelState; user: UserModelState }) => ({
    room,
    status,
    user,
  }),
)(UserList)
