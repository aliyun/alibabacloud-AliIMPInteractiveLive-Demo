import Chatlist from '@/components/Chatlist';
import WhiteBoard from '@/components/WhiteBoard';
import OperationBar from '@/components/OperationBar';
import Emitter from '@/utils/emitter';
import Video from '@/components/Video';
import { useHistory } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { message } from 'antd';
import { getLessonStartTime } from '@/api/apis';
import './index.less';

const { EventNameEnum } = window.RoomPaasSdk;
const emitter = Emitter.getInstance();
let isTalking = false;

export default function Student(props: any) {
  const userId = props.location.query.userId || '';
  const history = useHistory();
  const [windowChangeData, setWindowChangeData] = useState(true);
  const [isPlaying, setIsPlaying] = useState(false);
  const [isScreening, setIsScreening] = useState(false);
  const [replayState, setReplayState] = useState(false);
  const [cameraState, setCameraState] = useState(true);
  const [isMicing, setIsMicing] = useState(false);
  const startLive = () => {
    window.liveService.getLiveDetail().then((res: any) => {
      if (res.status === 1) {
        setIsPlaying(true);
        getLessonStartTime({ roomId: window.sessionStorage.getItem('roomId') })
          .then((res) => {
            emitter.emit('timeStart', res.result.createTime);
          })
          .catch((err: any) => {
            message.error('获取时间失败');
            console.log(err);
          });
        window.liveService.tryPlayLive();
      }
    });
  };
  const bindEvent = () => {
    window.liveService.on(EventNameEnum.PaaSLivePublish, (d: any) => {
      emitter.emit('stopReplay', '');
      setIsPlaying(true);
      setReplayState(false);
      setTimeout(() => {
        window.liveService.tryPlayLive();
      }, 1000);
      getLessonStartTime({ roomId: window.sessionStorage.getItem('roomId') })
        .then((res) => {
          emitter.emit('timeStart', res.result.createTime);
        })
        .catch((err: any) => {
          message.error('获取时间失败');
          console.log(err);
        });
    });
    window.liveService.on(EventNameEnum.PaaSLiveStop, (d: any) => {
      setIsPlaying(false);
      if (isTalking) {
        emitter.emit('cancelJoinChannel', '');
      }
      emitter.emit('timeFinish', '');
      emitter.emit('clearState', '');
    });
    window.rtcService.on(EventNameEnum.PaaSRtcCamera, (d: any) => {
      const status = d.data.open;
      if (status && !cameraState) {
        window.liveService.tryPlayLive();
      }
      if (d.data.userId === window.sessionStorage.getItem('ownerId')) {
        setCameraState(status);
      }
    });
  };
  const catchCommit = (page: any) => {
    return window.chatService.listComment(0, page, 30);
  };
  const sendComment = (message: any) => {
    return window.chatService.sendComment(message);
  };
  const switchCamera = (state: boolean) => {
    return window.rtcService.setMuteCamera(state);
  };
  const switchMic = (state: boolean) => {
    return window.rtcService.setMutePush(state);
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
    window.roomChannel.leaveRoom();
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
  const applyJoinChannel = (state: boolean) => {
    window.rtcService.applyJoinChannel(state);
  };
  const windowChange = () => {
    setWindowChangeData(!windowChangeData);
  };
  useEffect(() => {
    if (!window.roomChannel) {
      message.info('请重新登录');
      history.replace('/login');
      return;
    }
    startLive();
    bindEvent();
    emitter.on('leaveRoom', () => {
      leaveRoom();
    });
    window.liveService.setPlayerConfig({
      container: '#J_player',
      videoWidth: '100%',
      videoHeight: '100%',
    });
    window.addEventListener('beforeunload', () => {
      leaveRoom();
    });
    return () => {
      leaveRoom();
    };
  }, []);
  useEffect(() => {
    isTalking = isMicing;
  }, [isMicing]);
  return (
    <div className="student">
      <div className="student-main">
        <div
          className={`${
            windowChangeData ? 'student-main-board-white' : ''
          } student-main-board`}
        ></div>
        <OperationBar
          userId={userId}
          isMicing={isMicing}
          isPlaying={isPlaying}
          switchCamera={switchCamera}
          leaveRoom={leaveRoom}
          switchMic={switchMic}
          setIsMicing={setIsMicing}
          setIsScreening={setIsScreening}
          setReplayState={setReplayState}
          applyJoinChannel={applyJoinChannel}
          role="student"
        />
      </div>
      <div className="student-chatarea">
        <div
          className="student-chatarea-btn"
          onClick={() => {
            if (!isPlaying) return;
            windowChange();
          }}
        >
          <svg className="icon window-change-icon" aria-hidden="true">
            <use xlinkHref="#icon-shichuangqiehuan1"></use>
          </svg>
        </div>
        <div className="student-chatarea-video">
          <div className="student-chatarea-video-board"></div>
        </div>
        <Chatlist
          userId={userId}
          role="student"
          onSend={sendComment}
          catchCommit={catchCommit}
        />
      </div>
      <div className="student-change-area">
        <div
          className={
            windowChangeData
              ? 'student-change-area-main'
              : 'student-change-area-second'
          }
        >
          {isPlaying || replayState ? (
            <WhiteBoard
              windowChangeData={windowChangeData}
              replayState={replayState}
              role="student"
            />
          ) : (
            <div className="student-whiteboard-waitting"></div>
          )}
        </div>
        <div
          className={
            windowChangeData
              ? 'student-change-area-second'
              : 'student-change-area-main'
          }
        >
          {isPlaying || replayState ? (
            cameraState ? (
              <Video
                role="student"
                isMicing={isMicing}
                isScreening={isScreening}
                replayState={replayState}
                windowChangeData={windowChangeData}
              />
            ) : (
              <div className="student-video-waitting">
                <div className="student-video-waitting-text">
                  <svg
                    className="icon student-video-waitting-icon-font-size"
                    aria-hidden="true"
                  >
                    <use xlinkHref="#icon-dibulan-kaiqishexiangtou"></use>
                  </svg>
                  摄像头已关闭
                </div>
              </div>
            )
          ) : (
            <div className="student-video-waitting">
              <div className="student-video-waitting-text">
                <svg
                  className="icon student-video-waitting-icon-font-size"
                  aria-hidden="true"
                >
                  <use xlinkHref="#icon-dibulan-kaiqishexiangtou"></use>
                </svg>
                课程未开始
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
