import { Popover, message, Modal, Button } from 'antd';
import { useState, useEffect } from 'react';
import { formatDate } from '@/utils/utils';
import {
  startLesson,
  endLesson,
  startRecording,
  listRecords,
  getLessonStartTime,
} from '@/api/apis';
import { getReplayWhiteBoard } from '@/biz/getWhiteBoard';
import Emitter from '@/utils/emitter';
import './operationbar.less';

const { EventNameEnum } = window.RoomPaasSdk;
const { confirm } = Modal;
const emitter = Emitter.getInstance();
let replayOut = false;
let isMicing = false;

export default function OperationBar(props: any) {
  isMicing = props.isMicing;
  const userId = window.sessionStorage.getItem('userId');
  const [voiceCtrl, setVoiceCtrl] = useState(false);
  const [cameraCtrl, setCameraCtrl] = useState(false);
  const [audioDevice, setAudioDevice] = useState<any[]>([]);
  const [videoDevice, setVideoDevice] = useState<any[]>([]);
  const [useVoice, setUseVoice] = useState(true);
  const [useCamera, setUseCamera] = useState(true);
  const [replaying, setReplaying] = useState(false);
  const [radioCtrl, setRadioCtrl] = useState(false);
  const [replayCtrl, setReplayCtrl] = useState(false);
  const [screenShareCtrl, setScreenShareCtrl] = useState(false);
  const [classCtrl, setClassCtrl] = useState(false);
  const [applyCtrl, setApplyCtrl] = useState(false);
  const [playBackList, setPlayBackList] = useState<any[]>([]);
  const [audioPoint, setAudioPoint] = useState(-2);
  const [videoPoint, setVideoPoint] = useState(-2);
  const [handTranscribe, setHandTranscribe] = useState(false);
  const [playbackPoint, setPlaybackPoint] = useState<number>(-1);
  const [isLoading, setIsLoading] = useState(false);
  const bindTeacherEvent = () => {
    if (!window.roomChannel) return;
    window.rtcService.on(EventNameEnum.onError, (d: any) => {
      if (d.errorCode === 10011) {
        window.rtcService.stopPublishScreen().then(() => {
          setScreenShareCtrl(false);
          emitter.emit('stopScreen', '');
        });
      }
      if (d.errorCode === 10012) {
        window.rtcService.stopPublishScreen().then(() => {
          setScreenShareCtrl(false);
          emitter.emit('stopScreen', '');
        });
      }
    });
  };
  const switchCamera = (state: boolean, deviceId?: any) => {
    if (!state) {
      return window.rtcService
        .setMuteCamera(state)
        .then(() => {
          return window.rtcService.switchCamera(deviceId);
        })
        .catch((err: any) => {
          console.log(err);
        });
    } else {
      return window.rtcService.setMuteCamera(state);
    }
  };
  const switchMic = async (state: boolean, deviceId?: any) => {
    if (!state) {
      return window.rtcService
        .setMutePush(state)
        .then(() => {
          return window.rtcService.switchMic(deviceId);
        })
        .catch((err: any) => {
          console.log(err);
        });
    } else {
      return window.rtcService.setMutePush(state);
    }
  };
  const bindStudentEvent = () => {
    if (!window.roomChannel) return;
    window.rtcService.on(EventNameEnum.PaaSRtcInvite, (d: any) => {
      if (!d.data.calleeList) return;
      const applyList = d.data.calleeList;
      let allowSend = false;
      for (let i = 0; i < applyList.length; i++) {
        if (applyList[i].userId === props.userId) {
          allowSend = true;
        }
      }
      if (!allowSend) return;
      confirm({
        title: `你收到了老师的连麦请求，是否接受？`,
        cancelText: '拒绝',
        okText: '接受',
        onOk() {
          props.setIsMicing(true);
          window.rtcService
            .joinChannel()
            .then(() => {
              window.liveService.stopPlay();
              setApplyCtrl(true);
            })
            .catch((err: any) => {
              props.setIsMicing(false);
              message.error(err);
            });
        },
        onCancel() {
          // approve false直接拒绝
          window.rtcService.refuseInvite(
            props.userId,
            d.data.calleeList[0].userId,
          );
        },
      });
      return;
    });
    window.rtcService.on(EventNameEnum.PaaSRtcRefuse, (d: any) => {
      if (!d.data.approve) {
        message.success('老师狠狠地拒绝了你（−＿−；）');
        setApplyCtrl(false);
      }
    });
    window.rtcService.on(EventNameEnum.onSubscribeResult, (d: any) => {
      if (d.userId !== window.sessionStorage.getItem('ownerId')) return;
      window.rtcService.setDisplayRemoteVideo(
        document.getElementById('video1'),
        d.userId,
        d.streamType,
      );
    });
    window.rtcService.on(EventNameEnum.PaaSRtcKickUser, (d: any) => {
      const kickList = d.data.userList;
      for (let i = 0; i < kickList.length; i++) {
        if (kickList[i].userId === userId) {
          if (!isMicing) {
            setApplyCtrl(false);
            return;
          }
          props.setIsMicing(false);
          window.rtcService.leaveRtc();
          window.liveService.setPlayerConfig({
            container: '#J_player',
            videoWidth: '100%',
            videoHeight: '100%',
          });
          window.liveService.tryPlayLive();
          props.applyJoinChannel(false);
          setApplyCtrl(false);
          return;
        }
      }
    });
    window.rtcService.on(EventNameEnum.PaaSRtcScreen, (d: any) => {
      if (d.data.open) {
        window.rtcService.setDisplayRemoteVideo(
          document.getElementById('video1'),
          d.userId,
          2,
        );
      }
    });
    window.rtcService.on(EventNameEnum.PaaSRtcMute, (d: any) => {
      const muteList = d.data.userList;
      const status = d.data.open;
      for (let i = 0; i < muteList.length; i++) {
        if (muteList[i] === userId) {
          switchMic(!status).then(() => {
            setUseVoice(status);
          });
          return;
        }
      }
    });
  };
  const applyJoinChannel = () => {
    if (!props.isPlaying) {
      message.info('老师未上课');
      return;
    }
    props.applyJoinChannel(true);
    setApplyCtrl(true);
  };
  const cancelJoinChannel = () => {
    if (!window.roomChannel) return;
    if (props.isMicing) {
      props.setIsMicing(false);
      window.rtcService.leaveRtc();
      window.liveService.setPlayerConfig({
        container: '#J_player',
        videoWidth: '100%',
        videoHeight: '100%',
      });
      window.liveService.tryPlayLive();
      setApplyCtrl(false);
      return;
    }
    props.applyJoinChannel(false);
    setApplyCtrl(false);
  };
  const handleTranscribe = () => {
    if (!props.isPlaying) {
      message.info('课程未开始');
      return;
    }
    const recordId = window.sessionStorage.getItem('recordId');
    if (radioCtrl) {
      if (!recordId) return;
      window.rtcService.stopRecord();
      window.wbService.pauseWhiteboardRecording(recordId);
    } else {
      window.rtcService.startRecord();
      window.wbService.resumeWhiteboardRecording(recordId);
    }
    setRadioCtrl(!radioCtrl);
  };
  const handleReplay = () => {
    if (replayOut) {
      props.setReplayState(false);
      window.liveService.stopPlay();
      setReplaying(false);
      return;
    }
    listRecords({
      roomId: window.sessionStorage.getItem('roomId'),
    }).then((res) => {
      setPlayBackList(res.result.filter((item: any) => !!item.recordId));
    });
  };
  const record = () => {
    window.rtcService.startRecord();
    return window.wbService
      .startWhiteboardRecording()
      .then((res: any) => {
        window.sessionStorage.setItem('recordId', res.recordId);
        return startRecording({
          roomId: window.sessionStorage.getItem('roomId'),
          lessonId: window.sessionStorage.getItem('lessonId'),
          recordId: res.recordId,
        });
      })
      .then(() => {
        setRadioCtrl(true);
      })
      .catch((err: any) => {
        console.log(err);
        throw {
          err: 'wb',
          errorMsg: err.body.code,
        };
      });
  };
  const handlePlayBack = () => {
    if (props.isPlaying) {
      message.info('上课中，请下课后重试');
      return;
    }
    if (playBackList[playbackPoint].length === 0) return;
    window.sessionStorage.setItem(
      'recordId',
      playBackList[playbackPoint].recordId,
    );
    window.rtcService
      .getConfDetail(playBackList[playbackPoint].conferenceId)
      .then((res: any) => {
        if (!res.confInfoModel.playbackUrl) {
          message.error('播放地址错误');
          return;
        }
        getReplayWhiteBoard();
        props.setReplayState(true);
        window.liveService.setPlayerConfig({
          container: '#J_player',
          videoWidth: '100%',
          videoHeight: '100%',
        });
        const urlList = res.confInfoModel.playbackUrl.split(':');
        urlList[0] = 'https';
        const url = urlList.join(':');
        window.liveService.startPlay(url);
        window.replayAliyunBoard.on('ALIYUNBOARD_REPLAYER_READY', () => {
          window.liveService.pausePlay();
          const timer = window.liveService.getCurrentPDT();
          window.replayAliyunBoard.replayer
            .stepTo(Math.round(timer))
            .then(() => {
              window.replayAliyunBoard.replayer.start();
              window.liveService.startPlay();
            })
            .catch((res: any) => {
              console.log(res);
            });
        });
        setPlaybackPoint(-1);
        setReplayCtrl(false);
        setReplaying(true);
      });
  };
  const getAudioDevice = () => {
    return window.rtcService
      .getDeviceInfo()
      .then((res: any) => {
        if (res.videoDevices.length === 0) {
          message.error('获取设备信息失败，请检查设备接入');
          return;
        }
        setAudioDevice(res.audioDevices || []);
      })
      .catch((err: any) => {
        throw err;
      });
  };
  const getRadioDevice = () => {
    return window.rtcService
      .getDeviceInfo()
      .then((res: any) => {
        if (res.videoDevices.length === 0) {
          message.error('获取设备信息失败，请检查设备接入');
          return;
        }
        setVideoDevice(res.videoDevices || []);
      })
      .catch((err: any) => {
        throw err;
      });
  };
  const changeVoice = (state: boolean, index?: any, deviceId?: any) => {
    if (index === audioPoint) return;
    if (!props.isPlaying) {
      message.info('课程未开始');
      return;
    }
    if (state) {
      switchMic(!state, deviceId)
        .then(() => {
          setUseVoice(state);
          setVoiceCtrl(false);
          setAudioPoint(index);
        })
        .catch((err: any) => {
          message.error(err.message);
        });
    } else {
      switchMic(!state)
        .then(() => {
          setUseVoice(state);
          setVoiceCtrl(false);
          setAudioPoint(index);
        })
        .catch((err: any) => {
          message.error(err.message);
        });
    }
  };
  const changeCamera = (state: boolean, index?: any, deviceId?: any) => {
    if (index === videoPoint) return;
    if (!props.isPlaying) {
      message.info('课程未开始');
      return;
    }
    if (state) {
      switchCamera(!state, deviceId)
        .then(() => {
          if (state) {
            window.rtcService.startRtcPreview(
              document.getElementById('preview'),
            );
          } else {
            window.rtcService.stopRtcPreview();
          }
          setUseCamera(state);
          setCameraCtrl(false);
          setVideoPoint(index);
          if (props.role === 'teacher') {
            props.setCameraState(state);
          }
        })
        .catch((err: any) => {
          message.error(err.message);
        });
    } else {
      switchCamera(!state)
        .then(() => {
          if (state) {
            window.rtcService.startRtcPreview(
              document.getElementById('preview'),
            );
          } else {
            window.rtcService.stopRtcPreview();
          }
          setUseCamera(state);
          setCameraCtrl(false);
          setVideoPoint(index);
          if (props.role === 'teacher') {
            props.setCameraState(state);
          }
        })
        .catch((err: any) => {
          message.error(err.message);
        });
    }
  };
  const startClass = () => {
    if (props.replayState) {
      emitter.emit('stopReplay', '');
    }
    setIsLoading(true);
    props
      .startPublish()
      .then(() => {
        return startLesson({
          roomId: window.sessionStorage.getItem('roomId'),
          lessonTitle: `${props.userId}的房间`,
          conferenceId: window.rtcService.rtcId,
          docKey: window.sessionStorage.getItem('docKey'),
          createTime: +new Date(),
        });
      })
      .then((res: any) => {
        setIsLoading(false);
        setClassCtrl(!classCtrl);
        props.setIsPlaying(true);
        getLessonStartTime({ roomId: window.sessionStorage.getItem('roomId') })
          .then((res) => {
            emitter.emit('timeStart', res.result.createTime);
          })
          .catch((err: any) => {
            message.error('获取时间失败');
            console.log(err);
          });
        window.sessionStorage.setItem('lessonId', res.result || '');
        if (props.autoTranscribe) {
          message.info('自动录制已开启，您可在设置中进行关闭～');
          return record();
        }
      })
      .catch((err: any) => {
        setIsLoading(false);
        switch (err.err) {
          case 'wb':
            props.setAutoTranscribe(false);
            message.error('开始录制失败,请重试');
            return;
          case 'live':
            setClassCtrl(false);
            message.error('创建live失败,请刷新后重试或询问管理员');
            return;
          default:
            break;
        }
      });
  };
  const stopClass = async () => {
    if (!window.roomChannel || !props.isPlaying) return;
    const recordId = window.sessionStorage.getItem('recordId');
    setClassCtrl(!classCtrl);
    try {
      await window.wbService.stopWhiteboardRecording(recordId);
      await window.rtcService.stopRecord();
    } catch (err) {
      message.error('录制失败');
    }
    try {
      await props.stopPublish();
      endLesson({
        roomId: window.sessionStorage.getItem('roomId'),
        lessonId: window.sessionStorage.getItem('lessonId'),
      });
      props.setIsPlaying(false);
      emitter.emit('timeFinish', '');
      emitter.emit('resetWindow', '');
      emitter.emit('clearState', '');
      props.setIsScreening(false);
      setScreenShareCtrl(false);
      setHandTranscribe(false);
      setRadioCtrl(false);
      // 保证存在preview节点时调用startpreview
      setTimeout(() => {
        window.rtcService.startRtcPreview(document.getElementById('preview'));
      }, 2000);
    } catch (err: any) {
      message.error(err);
    }
  };
  const screenShare = () => {
    if (!props.isPlaying) {
      message.info('课程未开始');
      return;
    }
    if (!screenShareCtrl) {
      window.rtcService
        .setLayout([userId], 4)
        .then(() => {
          props.setIsScreening(true);
          window.rtcService.startPublishScreen();
        })
        .catch(() => {
          message.error('屏幕共享失败');
        });
    }
    if (screenShareCtrl) {
      props.setIsScreening(false);
      window.rtcService.stopPublishScreen();
      emitter.emit('stopScreen', '');
    }
    setScreenShareCtrl(!screenShareCtrl);
  };
  const voiceContent = (
    <div className="popover">
      {(audioDevice || []).map((data: any, index: any) => {
        return (
          <div
            className="popover-item"
            key={index}
            onClick={() => {
              changeVoice(true, index, data.deviceId);
            }}
          >
            <p className="popover-item-p">{data.label}</p>
            <svg
              className={`${
                audioPoint === index ? 'popover-item-active' : ''
              } icon`}
              aria-hidden="true"
            >
              <use xlinkHref="#icon-dibulan-gouxuan"></use>
            </svg>
          </div>
        );
      })}
      <div
        className="popover-item"
        onClick={() => {
          changeVoice(false, -1);
        }}
      >
        <p className="popover-item-p">禁用</p>
        <svg
          className={`${audioPoint === -1 ? 'popover-item-active' : ''} icon`}
          aria-hidden="true"
        >
          <use xlinkHref="#icon-dibulan-gouxuan"></use>
        </svg>
      </div>
    </div>
  );
  const cameraContent = (
    <div className="popover">
      {(videoDevice || []).map((data: any, index: any) => {
        return (
          <div
            className="popover-item"
            key={index}
            onClick={() => {
              changeCamera(true, index, data.deviceId);
            }}
          >
            <p className="popover-item-p">{data.label}</p>
            <svg
              className={`${
                videoPoint === index ? 'popover-item-active' : ''
              } icon`}
              aria-hidden="true"
            >
              <use xlinkHref="#icon-dibulan-gouxuan"></use>
            </svg>
          </div>
        );
      })}
      <div
        className="popover-item"
        onClick={() => {
          changeCamera(false, -1);
        }}
      >
        <p className="popover-item-p">禁用</p>
        <svg
          className={`${videoPoint === -1 ? 'popover-item-active' : ''} icon`}
          aria-hidden="true"
        >
          <use xlinkHref="#iicon-dibulan-gouxuan"></use>
        </svg>
      </div>
    </div>
  );
  const playbackContent = (
    <div className="replay-popover">
      <div className="replay-popover-title">选择回放课程</div>
      <div className="replay-popover-tabbar">
        <div className="replay-popover-tabbar-item">课程名称</div>
        <div className="replay-popover-tabbar-item">生成时间</div>
      </div>
      <div className="replay-popover-body">
        {(playBackList || []).map((data: any, index: any) => {
          return (
            <div
              className={`replay-popover-item ${
                playbackPoint === index ? 'replay-popover-item-active' : ''
              }`}
              key={index}
              onClick={() => {
                setPlaybackPoint(index);
              }}
            >
              <p className="replay-popover-item-p">{data.lessonTitle}</p>
              <p className="replay-popover-item-p">
                {formatDate(new Date(data.createTime), 'yyyy-MM-dd hh:mm')}
              </p>
            </div>
          );
        })}
      </div>
      <Button
        onClick={() => {
          handlePlayBack();
        }}
      >
        确定
      </Button>
    </div>
  );
  const AVSetting = () => {
    if (props.role === 'teacher' || applyCtrl) {
      return (
        <div className="operationbar-body">
          <Popover
            content={voiceContent}
            trigger="click"
            visible={voiceCtrl}
            onVisibleChange={() => {
              setVoiceCtrl(!voiceCtrl);
            }}
          >
            <div
              className={`operationbar-body-item ${
                useVoice ? '' : 'setting-color-red operationbar-body-item-red'
              }`}
              onClick={() => {
                getAudioDevice()
                  .then(() => {
                    setVoiceCtrl(!voiceCtrl);
                  })
                  .catch(() => {
                    message.error('获取设备列表失败');
                  });
              }}
            >
              <div className="operationbar-icon-style">
                <svg className="icon" aria-hidden="true">
                  <use
                    xlinkHref={
                      useVoice ? '#icon-jingyin' : '#icon-quxiaojingyin'
                    }
                  ></use>
                </svg>
                <svg
                  className={`icon operationbar-icon-size ${
                    voiceCtrl ? 'operationbar-icon-active' : ''
                  }`}
                  aria-hidden="true"
                >
                  <use xlinkHref="#icon-zhankai2"></use>
                </svg>
              </div>
              <div className="operationbar-font-size">
                <span>{useVoice ? '静音' : '取消静音'}</span>
              </div>
            </div>
          </Popover>
          <Popover
            content={cameraContent}
            trigger="click"
            visible={cameraCtrl}
            onVisibleChange={() => {
              setCameraCtrl(!cameraCtrl);
            }}
          >
            <div
              className={`operationbar-body-item ${
                useCamera ? '' : 'setting-color-red operationbar-body-item-red'
              }`}
              onClick={() => {
                getRadioDevice()
                  .then(() => {
                    setCameraCtrl(!cameraCtrl);
                  })
                  .catch(() => {
                    message.error('获取设备列表失败');
                  });
              }}
            >
              <div className="operationbar-icon-style">
                <svg className="icon" aria-hidden="true">
                  <use
                    xlinkHref={
                      useCamera
                        ? '#icon-guanbishexiangtou'
                        : '#icon-dibulan-kaiqishexiangtou'
                    }
                  ></use>
                </svg>
                <svg
                  className={`icon operationbar-icon-size ${
                    cameraCtrl ? 'operationbar-icon-active' : ''
                  }`}
                  aria-hidden="true"
                >
                  <use xlinkHref="#icon-zhankai2"></use>
                </svg>
              </div>
              <div className="operationbar-font-size">
                <span>{useCamera ? '关闭摄像头' : '开启摄像头'}</span>
              </div>
            </div>
          </Popover>
        </div>
      );
    }
    return <div className="operationbar-body"></div>;
  };
  const mainSetting = () => {
    if (props.role === 'teacher') {
      return (
        <div className="operationbar-body">
          {props.isPlaying ? (
            props.autoTranscribe || handTranscribe ? (
              <div
                className={`operationbar-body-item ${
                  radioCtrl ? 'setting-color-red' : ''
                }`}
                onClick={() => {
                  handleTranscribe();
                }}
              >
                <div className="operationbar-icon-style">
                  <svg className="icon" aria-hidden="true">
                    <use
                      xlinkHref={`#${
                        radioCtrl
                          ? 'icon-zanting1'
                          : 'icon-dibulan-zantingluzhi'
                      }`}
                    ></use>
                  </svg>
                </div>
                <div className="operationbar-font-size">
                  <span>{radioCtrl ? '暂停录制' : '继续录制'}</span>
                </div>
              </div>
            ) : (
              <div
                className={`operationbar-body-item ${
                  radioCtrl ? 'setting-color-red' : ''
                }`}
                onClick={() => {
                  record().then(() => {
                    setHandTranscribe(true);
                  });
                }}
              >
                <div className="operationbar-icon-style">
                  <svg className="icon" aria-hidden="true">
                    <use xlinkHref="#icon-dibulan-zantingluzhi"></use>
                  </svg>
                </div>
                <div className="operationbar-font-size">
                  <span>录制</span>
                </div>
              </div>
            )
          ) : (
            ''
          )}
          <Popover
            content={playbackContent}
            trigger={`${replaying ? '' : 'click'}`}
            visible={replayCtrl}
            onVisibleChange={() => {
              setReplayCtrl(!replayCtrl);
            }}
          >
            <div
              className={`operationbar-body-item ${
                replaying ? 'setting-color-red operationbar-body-item-red' : ''
              }`}
              onClick={() => {
                handleReplay();
              }}
            >
              <div className="operationbar-icon-style">
                <svg className="icon" aria-hidden="true">
                  <use xlinkHref="#icon-luzhihuifang1"></use>
                </svg>
              </div>
              <div className="operationbar-font-size">
                <span>{replaying ? '停止回放' : '回放管理'}</span>
              </div>
            </div>
          </Popover>
          <div
            className={`operationbar-body-item ${
              screenShareCtrl
                ? 'setting-color-red operationbar-body-item-red'
                : ''
            }`}
            onClick={() => {
              screenShare();
            }}
          >
            <div className="operationbar-icon-style">
              <svg className="icon" aria-hidden="true">
                <use xlinkHref="#icon-dibulan-pingmugongxiang"></use>
              </svg>
            </div>
            <div className="operationbar-font-size">
              <span>{screenShareCtrl ? '结束共享' : '屏幕共享'}</span>
            </div>
          </div>
          {/* <div className="operationbar-body-item">
            <svg className="icon" aria-hidden="true">
              <use xlinkHref="#icon-bofangshipin"></use>
            </svg>
            <span className="operationbar-font-size">视频播放</span>
          </div> */}
        </div>
      );
    }
    return (
      <div className="operationbar-body">
        <div
          className={`operationbar-body-item ${
            applyCtrl ? 'setting-color-red' : ''
          }`}
          onClick={() => {
            applyCtrl ? cancelJoinChannel() : applyJoinChannel();
          }}
        >
          <div className="operationbar-icon-style">
            <svg className="icon" aria-hidden="true">
              <use
                xlinkHref={`#${
                  applyCtrl
                    ? 'icon-dibulan-quxiaolianmai'
                    : 'icon-dibulan-shenqinglianmai'
                }`}
              ></use>
            </svg>
          </div>
          <div className="operationbar-font-size">
            <span>{applyCtrl ? '取消连麦' : '申请连麦'}</span>
          </div>
        </div>
        <Popover
          content={playbackContent}
          trigger="click"
          visible={replayCtrl}
          onVisibleChange={() => {
            setReplayCtrl(!replayCtrl);
          }}
        >
          <div
            className={`operationbar-body-item ${
              replaying ? 'setting-color-red operationbar-body-item-red' : ''
            }`}
            onClick={() => {
              handleReplay();
            }}
          >
            <div className="operationbar-icon-style">
              <svg className="icon" aria-hidden="true">
                <use xlinkHref="#icon-luzhihuifang1"></use>
              </svg>
            </div>
            <div className="operationbar-font-size">
              <span>{replaying ? '停止回放' : '回放管理'}</span>
            </div>
          </div>
        </Popover>
      </div>
    );
  };
  const finishSetting = () => {
    if (props.role === 'teacher') {
      return (
        <div
          className={`operationbar-btn ${classCtrl ? 'setting-color-red' : ''}`}
          onClick={() => {
            classCtrl ? stopClass() : startClass();
          }}
        >
          {isLoading ? (
            <div className="operationbar-btn-loading">
              <svg
                className="icon operationbar-btn-loading-animation"
                aria-hidden="true"
              >
                <use xlinkHref="#icon-jiazai"></use>
              </svg>
            </div>
          ) : (
            <div className="operationbar-icon-style">
              <svg className="icon" aria-hidden="true">
                <use xlinkHref="#icon-dibulan-kaishishouke"></use>
              </svg>
            </div>
          )}
          <div className="operationbar-font-size">
            <span>
              {classCtrl ? '下课' : isLoading ? '正在进入' : '开始授课'}
            </span>
          </div>
        </div>
      );
    }
  };
  useEffect(() => {
    if (!window.roomChannel) return;
    switch (props.role) {
      case 'student':
        bindStudentEvent();
        emitter.on('cancelJoinChannel', () => {
          props.setIsMicing(false);
          setApplyCtrl(false);
        });
        break;
      case 'teacher':
        bindTeacherEvent();
        break;
      default:
        break;
    }
    emitter.on('stopReplay', () => {
      if (replayOut) {
        props.setReplayState(false);
        window.liveService.stopPlay();
        setReplaying(false);
        window.replayAliyunBoard.replayer.pause();
      }
    });
    window.liveService.on(EventNameEnum.PaaSPlayerEvent, (data: any) => {
      if (!replayOut) return;
      if (!data.eventName) return;
      switch (data.eventName) {
        case 'pause':
          window.replayAliyunBoard.replayer.pause();
          return;
        case 'play':
          window.replayAliyunBoard.replayer.start();
          return;
        case 'error':
          message.error('播放失败');
          return;
        case 'completeSeek':
          window.liveService.pausePlay();
          const timer = window.liveService.getCurrentPDT();
          window.replayAliyunBoard.replayer
            .stepTo(Math.round(timer))
            .then(() => {
              window.replayAliyunBoard.replayer.start();
              window.liveService.startPlay();
            })
            .catch((res: any) => {
              console.log(res);
            });
          return;
        default:
          break;
      }
    });
  }, []);
  useEffect(() => {
    replayOut = replaying;
  }, [replaying]);
  return (
    <div
      className={`operationbar ${
        props.role === 'teacher' ? 'operationbar-teacher' : ''
      }`}
    >
      <div className="operationbar-body">
        {AVSetting()}
        {mainSetting()}
      </div>
      {finishSetting()}
    </div>
  );
}
