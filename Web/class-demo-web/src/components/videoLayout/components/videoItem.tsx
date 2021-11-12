import { FC, useEffect, useState, useRef } from 'react';
import {
  RoomModelState,
  StatusModelState,
  UserModelState,
  connect,
  Dispatch,
} from 'umi';
import { User } from '@/models/user';
import styles from './videoItem.less';

interface PageProps {
  room: RoomModelState;
  status: StatusModelState;
  user: UserModelState;
  item?: User;
  isMe?: boolean;
  grid?: boolean;
  gridStyle?: any;
  dispatch: Dispatch;
}

const VideoLayout: FC<PageProps> = ({
  room,
  status,
  user,
  dispatch,
  item,
  isMe,
  grid,
  gridStyle,
}) => {
  return (
    <div
      className={`${styles['sub-video-item']} ${grid ? styles['grid'] : ''}`}
      style={gridStyle}
    >
      {((isMe && !status.cameraAvailable) ||
        (!isMe && item && item.isRtcMuteCamera)) && (
        <div className={styles['camera-unavailable']}>
          <svg className="icon" aria-hidden="true">
            <use xlinkHref="#icon-ic_toolbar_guanshexiangtou_noc"></use>
          </svg>
        </div>
      )}
      <div className={styles['info-bar']}>
        <div className={`${styles['operation-item']} ${styles['nick']}`}>
          {isMe ? '我' : item ? item.nick : '学生'}
        </div>
        <div className={styles['operation-item']}>
          <svg className="icon" aria-hidden="true">
            <use
              xlinkHref={
                (isMe && !status.micAvailable) ||
                (!isMe && item && item.isRtcMute)
                  ? '#icon-ic_toolbar_quxiaojingyin_white'
                  : '#icon-ic_toolbar_jingyin_white'
              }
            ></use>
          </svg>
        </div>
      </div>
      <video id={isMe ? 'preview' : `video-${item && item.userId}`}></video>
    </div>
  );
};

export default connect(
  ({
    room,
    status,
    user,
  }: {
    room: RoomModelState;
    status: StatusModelState;
    user: UserModelState;
  }) => ({
    room,
    status,
    user,
  }),
)(VideoLayout);
