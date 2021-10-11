import './header.less';
import { calcTime } from '@/utils/getTime';
import { useState, useEffect } from 'react';
import { Popover, Switch, message, Modal } from 'antd';
import Clipboard from 'clipboard';
import Emitter from '@/utils/emitter';

interface Header {
  props: any;
}
const emitter = Emitter.getInstance();
let stopFlag = false;
let timer = 0;
let startTime = 0;
let k = 0;
const createTimer = (setUpdateTime: any) => {
  const timeOut = (setUpdateTime: any) => {
    timer = +new Date() - startTime;
    if (stopFlag) return;
    setTimeout(() => {
      k += 1;
      if (k === 30) {
        timer = +new Date() - startTime;
        k = 0;
        setUpdateTime(timer);
        timeOut(setUpdateTime);
      } else {
        timer += 1000;
        setUpdateTime(timer);
        timeOut(setUpdateTime);
      }
    }, 1000);
  };
  timeOut(setUpdateTime);
};
const onChange = (checked: any) => {
  emitter.emit('setTrans', checked);
};
const content = () => {
  return (
    <div className="switch-body">
      <span className="switch-text">自动录制</span>
      <Switch defaultChecked onChange={onChange} />
    </div>
  );
};
const clipboard = new Clipboard('.page-header-info-item');
clipboard.on('success', () => {
  message.success('教室号已复制到剪贴板');
});
const { confirm } = Modal;
export default function Header(props: any) {
  const [timeState, setTimeState] = useState(0);
  const [updateTime, setUpdateTime] = useState(0);
  const changeTimer = () => {
    switch (timeState) {
      case 0:
        return (
          <>
            <span>未上课:</span>
            <span>00: 00</span>
          </>
        );
      case 1:
        return (
          <>
            <span>上课中:</span>
            <span>{calcTime(timer)}</span>
          </>
        );
      case 2:
        return (
          <>
            <span>已下课:</span>
            <span>{calcTime(timer)}</span>
          </>
        );
      default:
        break;
    }
  };
  const leaveRoom = () => {
    confirm({
      title: '是否离开房间？',
      cancelText: '取消',
      okText: '确认',
      onOk() {
        emitter.emit('leaveRoom', '');
      },
      onCancel() {
        return;
      },
    });
  };
  useEffect(() => {
    emitter.on('timeStart', (startCountTime: any) => {
      timer = 0;
      stopFlag = false;
      startTime = startCountTime || +new Date();
      setTimeState(1);
      createTimer(setUpdateTime);
    });
    emitter.on('timeFinish', () => {
      stopFlag = true;
      setTimeState(2);
    });
  }, []);
  return (
    <div className="page-header">
      <div className="page-header-left">
        <span className="page-header-left-title">互动课堂DEMO</span>
        {/* <span className="page-header-left-tips">
          内存:10G/18G 网络延迟：800ms
        </span> */}
      </div>
      <div className="page-header-info">
        <div className="page-header-info-item">
          <span>教室号：</span>
          <span className="page-header-info-item-value">{props.roomId}</span>
        </div>
        <div
          className="page-header-info-item page-header-info-item-point"
          data-clipboard-text={props.roomId}
        >
          <svg className="icon" aria-hidden="true">
            <use xlinkHref="#icon-jiaoshihao2"></use>
          </svg>
        </div>
        <div className="page-header-info-item page-header-info-item-background">
          {changeTimer()}
        </div>
      </div>
      <div className="page-header-right">
        {props.role === 'teacher' ? (
          <div className="page-header-right-item" title="自动录制">
            {timeState === 1 ? (
              <svg className="icon" aria-hidden="true">
                <use xlinkHref="#icon-dingbulan-shezhi"></use>
              </svg>
            ) : (
              <Popover
                placement="bottomRight"
                content={content}
                trigger="click"
              >
                <svg className="icon" aria-hidden="true">
                  <use xlinkHref="#icon-dingbulan-shezhi"></use>
                </svg>
              </Popover>
            )}
          </div>
        ) : (
          ''
        )}
        <div className="page-header-right-item" title="离开房间">
          <svg
            className="icon"
            aria-hidden="true"
            onClick={() => {
              leaveRoom();
            }}
          >
            <use xlinkHref="#icon-likaijiaoshi"></use>
          </svg>
        </div>
      </div>
    </div>
  );
}
