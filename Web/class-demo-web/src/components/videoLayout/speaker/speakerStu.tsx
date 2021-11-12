import { FC, useEffect, useState, useRef } from 'react';
import {
  RoomModelState,
  StatusModelState,
  UserModelState,
  connect,
  Dispatch,
} from 'umi';
import { useMount, useUnmount, usePersistFn } from 'ahooks';
import { User } from '@/models/user';
import VideoItem from '../components/videoItem';
import Emitter from '@/utils/emitter';
import styles from './speaker.less';
import teacher from '@/pages/teacher';

const emitter = Emitter.getInstance();

interface PageProps {
  room: RoomModelState;
  status: StatusModelState;
  user: UserModelState;
  dispatch: Dispatch;
}

const SpeakerStu: FC<PageProps> = ({ room, status, user, dispatch }) => {
  const containerEl = useRef<HTMLDivElement>(null);
  const teacherRef = useRef<HTMLVideoElement>(null);
  const [page, setPage] = useState(0);
  const [userList, setUserList] = useState<User[]>([]);
  const [currentVideoList, setCurrentVideoList] = useState<User[]>([]);

  const changePage = usePersistFn((to: 'prev' | 'next') => {
    const list = Object.values(user.userList).filter(
      (item) => item.isInSeat && !item.isMe,
    );
    const size = Math.floor(list.length / 6);
    if (to === 'prev') {
      if (page <= 0) return;
      setPage(page - 1);
    }
    if (to === 'next') {
      if (page >= size) return;
      setPage(page + 1);
    }
    setTimeout(() => {
      setVideo();
    }, 0);
  });

  const generateCurrentVideoList = usePersistFn((page: number) => {
    const list = Object.values(user.userList).filter(
      (item) => item.isInSeat && !(item.userId === room.ownerId) && !item.isMe,
    );
    console.log(list);
    list.unshift({
      userId: user.userId,
      nick: user.nick,
      isOwner: false,
      isMe: true,
      isInSeat: true,
      isApplying: false,
      isRtcMute: !status.micAvailable,
      isRtcMuteCamera: !status.cameraAvailable,
      isInviting: false,
      streamType: 1,
      subscribeResult: true,
    });
    setUserList(list);
    return list.slice(page * 6, (page + 1) * 6);
  });

  const setVideo = usePersistFn(() => {
    if (!window.rtcService) return;
    setCurrentVideoList(generateCurrentVideoList(page));
    const userList = Object.values(user.userList).filter(
      (item) => item.isInSeat && !item.isMe,
    );
    const nowList = userList.slice(page * 6, (page + 1) * 6);
    console.log(nowList, user.userList);
    setTimeout(() => {
      nowList.forEach((item) => {
        if (item.userId === room.ownerId) {
          window.rtcService.setDisplayRemoteVideo(
            teacherRef.current,
            item.userId,
            item.streamType || 1,
          );
        } else {
          window.rtcService.setDisplayRemoteVideo(
            document.getElementById(`video-${item.userId}`),
            item.userId,
            item.streamType || 1,
          );
        }
      });
    }, 100);
    window.rtcService.startRtcPreview(document.getElementById('preview'));
  });

  useMount(() => {
    if (!window.rtcService) return;
    emitter.on('needSetVideo', setVideo);
    setVideo();
  });

  useUnmount(() => {
    if (!window.rtcService) return;
    window.rtcService.stopRtcPreview();
    emitter.remove('needSetVideo', setVideo);
  });

  return (
    <div className={styles['speaker-container']} ref={containerEl}>
      <div
        className={`${styles['sub-video-bar']} ${
          status.viewMode === 'whiteBoard' || userList.length === 0
            ? styles['hide-sub-video-bar']
            : ''
        }`}
      >
        <div
          className={`${styles.prev} ${styles.arrow}`}
          onClick={() => changePage('prev')}
        >
          <svg className="icon" aria-hidden="true">
            <use xlinkHref="#icon-ic_toolbar_arrow"></use>
          </svg>
        </div>
        <div
          className={`${styles.next} ${styles.arrow}`}
          onClick={() => changePage('next')}
        >
          <svg className="icon" aria-hidden="true">
            <use xlinkHref="#icon-ic_toolbar_arrow"></use>
          </svg>
        </div>
        {userList.length > 0 && (
          <div className={styles['sub-video-group']}>
            {currentVideoList.map((item, index) => {
              if (item.isMe) {
                return <VideoItem isMe={true} key={index} />;
              } else {
                return <VideoItem item={item} key={index} />;
              }
            })}
          </div>
        )}
      </div>
      <div
        className={`${styles['speaker-main']} ${
          status.viewMode === 'whiteBoard' || userList.length === 0
            ? styles['full-speaker-main']
            : ''
        }`}
      >
        {user.userList[room.ownerId].isRtcMuteCamera && (
          <div className={styles['camera-unavailable']}>
            <svg className="icon" aria-hidden="true">
              <use xlinkHref="#icon-ic_toolbar_guanshexiangtou_noc"></use>
            </svg>
          </div>
        )}
        <video id="teacher" ref={teacherRef}></video>
      </div>
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
)(SpeakerStu);
