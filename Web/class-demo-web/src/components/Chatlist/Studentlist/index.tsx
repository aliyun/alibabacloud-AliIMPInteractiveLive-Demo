import { Button, message, Modal } from 'antd';
import { Fragment, useState, useEffect, useReducer } from 'react';
import { sort } from '@/utils/sort';
import { initHashList } from '@/utils/hashList';
import { BasicMap } from '@/utils/utils';
import './studentlist.less';
import Emitter from '../../../utils/emitter';

const { EventNameEnum } = window.RoomPaasSdk;
const { confirm } = Modal;
let timerMap: BasicMap<any> = {};
const emitter = Emitter.getInstance();

interface studentItem {
  userId: string;
  state: number;
}
interface ListStore {
  studentObj: BasicMap<studentItem>;
}
interface ListAction {
  type: string;
  payload: any;
}
const initialState: ListStore = {
  studentObj: {},
};
const actionTypes = {
  addStu: 'addStu',
  delStu: 'delStu',
  updateStu: 'updateStu',
  initStu: 'initStu',
};
const studentListReducer = (
  state: ListStore,
  action: ListAction,
): ListStore => {
  switch (action.type) {
    case actionTypes.addStu:
      Object.assign(state.studentObj, action.payload);
      return { ...state };
    case actionTypes.delStu:
      delete state.studentObj[action.payload];
      return { ...state };
    case actionTypes.updateStu:
      const updateUserId = Object.keys(action.payload)[0];
      const updateState = Object.values(action.payload)[0];
      if (!state.studentObj[updateUserId]) return { ...state };
      Object.assign(state.studentObj[updateUserId], updateState);
      return { ...state };
    case actionTypes.initStu:
      state.studentObj = {};
      Object.assign(state.studentObj, action.payload);
      return { ...state };
    default:
      return state;
  }
};

export default function Studentlist(props: any) {
  const userId = window.sessionStorage.getItem('userId');
  const [state, dispatch] = useReducer(studentListReducer, initialState);
  const [speakState, setSpeakState] = useState(true);
  const addStudentItem = (studentItem: any) => {
    dispatch({
      type: actionTypes.addStu,
      payload: studentItem,
    });
  };
  const delStudentItem = (studentItem: any) => {
    dispatch({
      type: actionTypes.delStu,
      payload: studentItem,
    });
  };
  const updateStudentItem = (studentItem: any) => {
    dispatch({
      type: actionTypes.updateStu,
      payload: studentItem,
    });
  };
  const setStudentList = (studentList: any) => {
    dispatch({
      type: actionTypes.initStu,
      payload: studentList,
    });
  };
  const acceptApply = (data: any) => {
    window.rtcService
      .handleApplyJoinChannel(data.userId, true)
      .then(() => {
        const userInfo = {
          ...data,
          tenantId: '',
        };
        return window.rtcService.inviteJoinChannel([userInfo]);
      })
      .then(() => {
        const key = data.userId;
        updateStudentItem({ [key]: { status: 2 } });
      })
      .catch((err: any) => {
        console.log(err);
      });
    clearTimeout(timerMap[data.userId]);
    delete timerMap[data.userId];
  };
  const refuseApply = (data: any) => {
    window.rtcService
      .handleApplyJoinChannel(data.userId, false)
      .then(() => {
        const key = data.userId;
        updateStudentItem({ [key]: { status: 0 } });
      })
      .catch((err: any) => {
        console.log(err);
      });
    clearTimeout(timerMap[data.userId]);
    delete timerMap[data.userId];
  };
  const hangUp = (data: any) => {
    window.rtcService
      .kickUserFromChannel([data.userId])
      .then(() => {
        const key = data.userId;
        updateStudentItem({ [key]: { status: 0 } });
      })
      .catch((err: any) => {
        console.log(err);
      });
  };
  const inviteCall = (data: any) => {
    if (!props.isPlaying) {
      message.info('课程未开始');
      return;
    }
    const userInfo = {
      ...data,
      tenantId: '',
    };
    window.rtcService
      .inviteJoinChannel([userInfo])
      .then(() => {
        const key = data.userId;
        updateStudentItem({ [key]: { status: 2 } });
      })
      .catch((err: any) => {
        console.log(err);
      });
  };
  const muteAll = () => {
    if (!window.rtcService) return;
    if (!props.isPlaying) {
      message.info('课程未开始');
      return;
    }
    if (speakState) {
      confirm({
        title: '是否开启全员静音？',
        cancelText: '取消',
        okText: '确认',
        onOk() {
          window.rtcService.muteAllRemoteMic(speakState).then(() => {
            setSpeakState(!speakState);
          });
        },
        onCancel() {
          return;
        },
      });
    } else {
      window.rtcService.muteAllRemoteMic(speakState).then(() => {
        message.info('全员静音已解除！');
        setSpeakState(!speakState);
      });
    }
  };
  const handleMic = (data: any) => {
    if (data.micphoneStatus === 1) {
      window.rtcService.muteRemoteMic(data.userId, true).then(() => {
        const key = data.userId;
        updateStudentItem({ [key]: { micphoneStatus: 0 } });
      });
      return;
    }
    window.rtcService.muteRemoteMic(data.userId, false).then(() => {
      const key = data.userId;
      updateStudentItem({ [key]: { micphoneStatus: 1 } });
    });
  };
  const init = () => {
    if (window.rtcService.isInChannel) {
      Promise.all([
        window.rtcService.listConfUser(1, 50).then((res: any) => res.userList),
        window.rtcService
          .listApplyLinkMicUser(1, 50)
          .then((res: any) => res.userList),
        window.roomChannel.listUser(1, 50).then((res: any) => res.userList),
      ]).then((res: any) => {
        const [confUserList, applyList, userList] = res;
        const studentList = initHashList(
          userList,
          window.sessionStorage.getItem('ownerId'),
          confUserList,
          applyList,
        ) as BasicMap<any>;
        setStudentList(studentList);
      });
    } else {
      window.roomChannel.listUser(1, 50).then((res: any) => {
        const studentList = initHashList(
          res.userList,
          window.sessionStorage.getItem('ownerId'),
        ) as BasicMap<any>;
        setStudentList(studentList);
      });
    }
  };
  const roomEnterHandler = (d: any) => {
    const newUserId = d.data.userId;
    const newObj = {
      [newUserId]: {
        nick: d.data.nick,
        userId: newUserId,
      },
    };
    if (newUserId === window.sessionStorage.getItem('ownerId')) return;
    if (d.data.enter) {
      addStudentItem(newObj);
      return;
    }
    delStudentItem(newUserId);
  };
  const rtcApplyHandler = (d: any) => {
    if (d.data.isApply) {
      const key = d.data.applyUser.userId;
      updateStudentItem({ [key]: { status: 7 } });
      message.info(`${d.data.applyUser.userId}申请连麦`);
      const timer = setTimeout(() => {
        window.rtcService
          .handleApplyJoinChannel(key, false)
          .then(() => {
            updateStudentItem({ [key]: { status: 0 } });
          })
          .catch((err: any) => {
            console.log(err);
          });
      }, 60000);
      timerMap[key] = timer;
    } else {
      const key = d.data.applyUser.userId;
      updateStudentItem({ [key]: { status: 0 } });
      message.info(`${d.data.applyUser.userId}取消连麦申请`);
    }
  };
  const joinSuccessHandler = (d: any) => {
    const userList = d.data.userList;
    for (let i = 0; i < userList.length; i++) {
      if (userList[i].userId === window.sessionStorage.getItem('ownerId'))
        return;
      const key = userList[i].userId;
      updateStudentItem({ [key]: { status: 3, micphoneStatus: 1 } });
      message.success(`和${userList[i].userId}连麦成功`);
    }
  };
  const leaveChannelHandler = (d: any) => {
    const userList = d.data.userList;
    for (let i = 0; i < userList.length; i++) {
      const key = userList[i].userId;
      if (key === window.sessionStorage.getItem('ownerId')) return;
      updateStudentItem({ [key]: { status: 0 } });
      if (userList[i].userId === props.location.query.userId)
        message.info(`${state.studentObj[userList[i]].userId}结束连麦`);
    }
  };
  const joinFailedHandler = (d: any) => {
    const userList = d.data.userList;
    for (let i = 0; i < userList.length; i++) {
      const key = userList[i].userId;
      updateStudentItem({ [key]: { status: 0 } });
      message.info(`${userList[i].userId}拒绝了连麦申请`);
    }
  };
  const bindEvent = () => {
    window.roomChannel.on(EventNameEnum.PaaSRoomEnter, roomEnterHandler);
    window.rtcService.on(EventNameEnum.PaaSRtcApply, rtcApplyHandler);
    window.rtcService.on(EventNameEnum.PaaSRtcJoinSuccess, joinSuccessHandler);
    window.rtcService.on(EventNameEnum.PaaSRtcJoinFailed, joinFailedHandler);
    window.rtcService.on(
      EventNameEnum.PaaSRtcLeaveChannel,
      leaveChannelHandler,
    );
    emitter.on('clearState', () => {
      const clearStudentObj: BasicMap<any> = {};
      const keys = Object.keys(state.studentObj);
      for (let i of keys) {
        const studentItem = {
          userId: state.studentObj[i].userId,
          nick: state.studentObj[i].userId,
          status: 0,
        };
        clearStudentObj[i] = studentItem;
      }
      setStudentList({ ...clearStudentObj });
    });
  };
  const showTeacherStatus = (data: any) => {
    switch (data.status) {
      case 2:
        return (
          <Fragment>
            <span className="studentlist-item-status">呼叫中</span>
            <Button></Button>
            <Button
              type="primary"
              danger
              size="small"
              onClick={() => {
                hangUp(data);
              }}
            >
              撤销
            </Button>
          </Fragment>
        );
      case 3:
        return (
          <Fragment>
            <span className="studentlist-item-status">
              <svg
                className={`icon ${
                  data.micphoneStatus === 1
                    ? 'micphone-color-active'
                    : 'micphone-color'
                }`}
                aria-hidden="true"
                onClick={() => {
                  handleMic(data);
                }}
              >
                <use
                  xlinkHref={`#${
                    data.micphoneStatus === 1
                      ? 'icon-xueshengliebiao-fayanzhong'
                      : 'icon-xueshengliebiao-jingyinzhong'
                  }`}
                ></use>
              </svg>
              已连麦
            </span>
            <Button></Button>
            <Button
              type="primary"
              danger
              size="small"
              onClick={() => {
                hangUp(data);
              }}
            >
              挂断
            </Button>
          </Fragment>
        );
      case 7:
        return (
          <Fragment>
            <span className="studentlist-item-status">申请连麦</span>
            <Button
              type="primary"
              size="small"
              onClick={() => {
                acceptApply(data);
              }}
            >
              连接
            </Button>
            <Button
              type="primary"
              danger
              size="small"
              onClick={() => {
                refuseApply(data);
              }}
            >
              拒绝
            </Button>
          </Fragment>
        );
      default:
        return (
          <Fragment>
            <span className="studentlist-item-status"></span>
            <Button></Button>
            <Button
              type="primary"
              size="small"
              onClick={() => {
                inviteCall(data);
              }}
            >
              连麦
            </Button>
          </Fragment>
        );
    }
  };
  const showStudentStatus = (data: any) => {
    switch (data.status) {
      case 3:
        return (
          <Fragment>
            <span className="studentlist-item-status">已连麦</span>
            <Button></Button>
            <Button></Button>
          </Fragment>
        );
      default:
        return (
          <Fragment>
            <span className="studentlist-item-status"></span>
            <Button></Button>
            <Button></Button>
          </Fragment>
        );
    }
  };
  useEffect(() => {
    if (!window.roomChannel) return;
    init();
    bindEvent();
  }, []);
  useEffect(() => {
    props.setStudentCount(Object.keys(state.studentObj).length);
  }, [state]);
  return (
    <div
      className={`studentlist ${
        props.tabbarState === 0 ? 'studentlist-active' : ''
      } ${
        Object.keys(state.studentObj).length === 0 ? 'studentlist-waitting' : ''
      }`}
    >
      <div className="studentlist-body">
        {sort(Object.values(state.studentObj), 'status').map(
          (data: any, index: any) => {
            if (data.userId === window.sessionStorage.getItem('ownerId'))
              return;
            return (
              <div key={index} className="studentlist-body-item">
                <span title={data.nick}>
                  {data.userId === userId ? `${data.nick} (我)` : data.nick}
                </span>
                {props.role === 'teacher'
                  ? showTeacherStatus(data)
                  : showStudentStatus(data)}
              </div>
            );
          },
        )}
      </div>
      {props.role === 'teacher' ? (
        <div
          className={`studentlist-forbid ${
            speakState ? '' : 'studentlist-forbid-active'
          }`}
          onClick={() => {
            muteAll();
          }}
        >
          <svg className="icon" aria-hidden="true">
            <use xlinkHref="#icon-quanyuanjingyin"></use>
          </svg>
          <span>{`${speakState ? '全员静音' : '解除全员静音'}`}</span>
        </div>
      ) : (
        <></>
      )}
    </div>
  );
}
