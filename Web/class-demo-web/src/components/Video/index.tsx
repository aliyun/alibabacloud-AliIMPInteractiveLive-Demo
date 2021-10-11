import { useState, useEffect } from 'react';
import { message } from 'antd';
import LayoutOne from '@/components/Camera/LayoutOne';
import LayoutFive from '@/components/Camera/LayoutFive';
import LayoutNine from '@/components/Camera/LayoutNine';
import Emitter from '../../utils/emitter';
import Player from '@/components/Camera/Player';
import './video.less';

let emitter = Emitter.getInstance();
let publishList: Array<any> = [];
let screenState = 1;

export default function Video(props: any) {
  const [operationState, setOperationState] = useState(0);
  const [videoState, setVideoState] = useState(0);
  const [showCtrl, setShowCtrl] = useState(true);
  const Operation = async (state: number) => {
    if (!window.roomChannel || props.role === 'student' || !props.isPlaying)
      return;
    if (operationState === state) return;
    const userList = window.rtcService.getUserList();
    if (userList.length === 0) {
      message.info('当前无人连麦');
      setOperationState(0);
      screenState = 1;
      window.rtcService.setLayout([props.userId], 1);
      return;
    }
    await window.rtcService.stopRtcPreview();
    setOperationState(state);
    switch (state) {
      case 0:
        screenState = 1;
        publishList = [props.userId];
        break;
      case 1:
        if (userList.length > 4) {
          const userIdList = userList.map((data: any) => {
            return data.userId;
          });
          const sendFiveList = [...userIdList.slice(0, 4), props.userId];
          publishList = sendFiveList;
          screenState = 2;
          break;
        }
        const sendFiveList = new Array(5).fill('');
        for (let i = 0; i < userList.length; i++) {
          sendFiveList[i] = userList[i].userId;
        }
        sendFiveList[4] = props.userId;
        publishList = sendFiveList;
        screenState = 2;
        window.rtcService.setLayout(publishList, screenState);
        break;
      case 2:
        if (userList.length > 8) {
          const userIdList = userList.map((data: any) => {
            return data.userId;
          });
          const sendNineList = [...userIdList.slice(0, 8), props.userId];
          publishList = sendNineList;
          screenState = 3;
          break;
        }
        const sendNineList = new Array(9).fill('');
        for (let i = 0; i < userList.length; i++) {
          sendNineList[i + 1] = userList[i].userId;
        }
        sendNineList[0] = props.userId;
        publishList = sendNineList;
        screenState = 3;
        break;
      default:
        break;
    }
    if (props.isScreening) return;
    window.rtcService.setLayout(publishList, screenState);
  };
  const pageChange = () => {
    if (props.windowChangeData || !props.isPlaying) {
      return <></>;
    }
    return (
      <div className="video-operationbar">
        <div
          className={`video-operationbar-page ${
            showCtrl ? '' : 'video-operationbar-page-hide'
          }`}
        >
          <div
            className={`${
              operationState === 0 ? 'video-active' : ''
            } video-operationbar-page-item`}
            onClick={() => {
              Operation(0);
            }}
          >
            <svg className="icon" aria-hidden="true">
              <use xlinkHref="#icon-bujuqiehuan1"></use>
            </svg>
          </div>
          <div
            className={`${
              operationState === 1 ? 'video-active' : ''
            } video-operationbar-page-item`}
            onClick={() => {
              Operation(1);
            }}
          >
            <svg className="icon" aria-hidden="true">
              <use xlinkHref="#icon-bujuqiehuan21"></use>
            </svg>
          </div>
          <div
            className={`${
              operationState === 2 ? 'video-active' : ''
            } video-operationbar-page-item`}
            onClick={() => {
              Operation(2);
            }}
          >
            <svg className="icon" aria-hidden="true">
              <use xlinkHref="#icon-bujuqiehuan3"></use>
            </svg>
          </div>
        </div>
        <div
          className={`video-operationbar-ctrl ${
            showCtrl ? '' : 'video-operationbar-ctrl-turn'
          }`}
          onClick={() => {
            setShowCtrl(!showCtrl);
          }}
        >
          <svg className="icon" aria-hidden="true">
            <use xlinkHref="#icon-bujuqiehuanjiantou"></use>
          </svg>
        </div>
      </div>
    );
  };
  const videoChange = () => {
    switch (operationState) {
      case 0:
        return (
          <LayoutOne
            windowChangeData={props.windowChangeData}
            isScreening={props.isScreening}
            role={props.role}
          />
        );
      case 1:
        return <LayoutFive />;
      case 2:
        return <LayoutNine />;
      default:
        break;
    }
  };
  useEffect(() => {
    emitter.on('resetWindow', () => {
      setOperationState(0);
      emitter.emit('windowChange', '');
      props.setWindowChangeData(true);
    });
    emitter.on('stopScreen', () => {
      window.rtcService.setLayout(publishList, screenState);
    });
  }, []);
  useEffect(() => {
    if (props.windowChangeData) {
      setVideoState(operationState);
      Operation(0);
      return;
    }
    Operation(videoState);
  }, [props.windowChangeData]);
  return (
    <div className="video">
      {(props.role === 'student' && !props.isMicing) || props.replayState ? (
        <Player windowChangeData={props.windowChangeData} />
      ) : (
        <>
          {videoChange()}
          {props.role === 'teacher' ? pageChange() : ''}
        </>
      )}
    </div>
  );
}
