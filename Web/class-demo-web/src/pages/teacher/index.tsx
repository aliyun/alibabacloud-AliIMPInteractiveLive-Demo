import Chatlist from '@/components/Chatlist';
import WhiteBoard from '@/components/WhiteBoard';
import OperationBar from '@/components/OperationBar';
import Video from '@/components/Video';
import { useHistory } from 'react-router';
import { useEffect, useState } from 'react';
import { message } from 'antd';
import Emitter from '../../utils/emitter';
import './index.less';

const { EventNameEnum } = window.RoomPaasSdk;
const emitter = Emitter.getInstance();

export default function Teacher(props: any) {
  const userId = props.location.query.userId || '';
  const [windowChangeData, setWindowChangeData] = useState(true);
  const [isPlaying, setIsPlaying] = useState(false);
  const [replayState, setReplayState] = useState(false);
  const [isScreening, setIsScreening] = useState(false);
  const [autoTranscribe, setAutoTranscribe] = useState(true);
  const [cameraState, setCameraState] = useState(true);
  const history = useHistory();
  const initChannel = () => {
    if (!window.roomChannel) {
      history.replace('/login');
      return;
    }
    emitter.on('setTrans', setAutoTranscribe);
    window.rtcService.startRtcPreview(document.getElementById('preview'));
    window.roomChannel.on(EventNameEnum.PaaSOtherEvent, (d: any) => {
      console.log(EventNameEnum.PaaSOtherEvent, d);
    });
  };
  const onSend = (message: any) => {
    return window.chatService.sendComment(message);
  };
  const catchCommit = (page: any) => {
    return window.chatService.listComment(0, page, 30);
  };
  const startPublish = () => {
    return window.rtcService
      .joinChannel(userId)
      .then(() => {
        return window.rtcService.startRoadPublish();
      })
      .then(() => {
        message.success('开始授课');
      })
      .catch((err: any) => {
        console.dir(err);
        throw {
          err: 'live',
          errMsg: err.body.reason,
        };
      });
  };
  const stopPublish = () => {
    window.rtcService.leaveRtc(true);
  };
  const leaveRoom = () => {
    if (!window.roomChannel) return;
    window.sessionStorage.removeItem('userId');
    window.sessionStorage.removeItem('roomId');
    window.sessionStorage.removeItem('ownerId');
    window.sessionStorage.removeItem('docKey');
    window.sessionStorage.removeItem('lessonId');
    window.sessionStorage.removeItem('accessToken');
    window.sessionStorage.removeItem('recordId');
    window.roomChannel.leaveRoom(true);
    window.roomEngine.logout().then(() => {
      window.roomEngine = null;
      window.roomChannel = null;
      window.rtcService = null;
      window.liveService = null;
      window.chatService = null;
      window.aliyunBoard = null;
      window.replayAliyunBoard = null;
      history.replace('/login');
    });
  };
  const windowChange = () => {
    emitter.emit('windowChange', '');
    setWindowChangeData(!windowChangeData);
  };
  useEffect(() => {
    initChannel();
    emitter.on('leaveRoom', () => {
      leaveRoom();
    });
    window.addEventListener('beforeunload', () => {
      leaveRoom();
    });
    return () => {
      leaveRoom();
    };
  }, []);
  return (
    <div className="teacher">
      <div className="teacher-main">
        <div
          className={`${
            windowChangeData ? 'teacher-main-board-white' : ''
          } teacher-main-board`}
        ></div>
        <OperationBar
          userId={userId}
          autoTranscribe={autoTranscribe}
          isPlaying={isPlaying}
          replayState={replayState}
          setIsPlaying={setIsPlaying}
          setIsScreening={setIsScreening}
          setAutoTranscribe={setAutoTranscribe}
          setCameraState={setCameraState}
          startPublish={startPublish}
          stopPublish={stopPublish}
          setReplayState={setReplayState}
          role="teacher"
        />
      </div>
      <div className="teacher-chatarea">
        <div
          className="teacher-chatarea-btn"
          onClick={() => {
            windowChange();
          }}
        >
          <svg className="icon window-change-icon" aria-hidden="true">
            <use xlinkHref="#icon-shichuangqiehuan1"></use>
          </svg>
        </div>
        <div className="teacher-chatarea-video">
          <div className="teacher-chatarea-video-board"></div>
        </div>
        <Chatlist
          userId={userId}
          isPlaying={isPlaying}
          role="teacher"
          onSend={onSend}
          catchCommit={catchCommit}
        />
      </div>
      <div className="teacher-change-area">
        <div
          className={
            windowChangeData
              ? 'teacher-change-area-main'
              : 'teacher-change-area-second'
          }
        >
          <WhiteBoard
            windowChangeData={windowChangeData}
            replayState={replayState}
            role="teacher"
          />
        </div>
        <div
          className={
            windowChangeData
              ? 'teacher-change-area-second'
              : 'teacher-change-area-main'
          }
        >
          {cameraState ? (
            <Video
              role="teacher"
              userId={userId}
              isPlaying={isPlaying}
              isScreening={isScreening}
              replayState={replayState}
              windowChangeData={windowChangeData}
              setWindowChangeData={setWindowChangeData}
            />
          ) : (
            <div className="teacher-video-waitting">
              <div className="teacher-video-waitting-text">
                <svg
                  className="icon teacher-video-waitting-icon-font-size"
                  aria-hidden="true"
                >
                  <use xlinkHref="#icon-dibulan-kaiqishexiangtou"></use>
                </svg>
                摄像头已关闭
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
