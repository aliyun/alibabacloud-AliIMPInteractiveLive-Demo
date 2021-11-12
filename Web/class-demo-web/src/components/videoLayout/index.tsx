import { FC, useEffect, useState, useRef } from 'react';
import {
  RoomModelState,
  StatusModelState,
  UserModelState,
  connect,
  Dispatch,
} from 'umi';
import { useMount } from 'ahooks';
import Grid from './grid/grid';
import Speaker from './speaker/speaker';
import SpeakerStu from './speaker/speakerStu';
import styles from './index.less';

interface PageProps {
  room: RoomModelState;
  status: StatusModelState;
  user: UserModelState;
  dispatch: Dispatch;
}

const VideoLayout: FC<PageProps> = ({ room, status, user, dispatch }) => {
  const layout = () => {
    switch (status.layout) {
      case '6':
        return user.isOwner ? <Speaker /> : <SpeakerStu />;
      case '9':
        return <Grid />;
    }
  };

  return <div className={styles['video-container']}>{layout()}</div>;
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
