import './layoutone.less';
import { useEffect } from 'react';

export default function LayoutFive() {
  const setSize = () => {
    const bodyElement = document.querySelector('.layout-five');
    const navElement = document.querySelector(
      '.layout-five-nav',
    ) as HTMLElement;
    const fatherElement = document.querySelector(
      '.layout-five-main',
    ) as HTMLElement;
    const element = document.querySelector('.layout-five-nav-item');
    const videoElement = document.querySelector('#preview') as HTMLElement;
    if (
      !bodyElement ||
      !navElement ||
      !fatherElement ||
      !videoElement ||
      !element
    )
      return;
    const bodyHeight = bodyElement.clientHeight;
    fatherElement.style.top = bodyHeight * 0.31 + 'px';
    navElement.style.bottom = bodyHeight * 0.71 + 'px';
    const width = fatherElement.clientWidth;
    const height = fatherElement.clientHeight;
    const littleWidth = navElement.clientWidth * 0.25;
    const littleHeight = navElement.clientHeight;
    videoElement.style.width = width + 'px';
    videoElement.style.height = height + 'px';
    for (let i = 1; i < 5; i++) {
      const nodeId = `#video${i}`;
      const videoNode = document.querySelector(nodeId) as HTMLElement;
      videoNode.style.width = littleWidth + 'px';
      videoNode.style.height = littleHeight + 'px';
    }
  };
  const init = () => {
    window.rtcService.startRtcPreview(document.getElementById('preview'));
    const userList = window.rtcService.getUserList();
    const max = userList.length > 4 ? 4 : userList.length;
    for (let i = 0; i < max; i++) {
      const userId = userList[i].userId;
      window.rtcService.setDisplayRemoteVideo(
        document.getElementById('video' + (i + 1)),
        userId,
        1,
      );
    }
  };
  useEffect(() => {
    if (!window.roomChannel) return;
    setSize();
    init();
    window.onresize = () => {
      setSize();
    };
    return () => {
      window.onresize = null;
    };
  }, []);
  return (
    <div className="layout-five">
      <div className="layout-five-nav">
        <div className="layout-five-nav-item">
          <video id="video1"></video>
        </div>
        <div className="layout-five-nav-item">
          <video id="video2"></video>
        </div>
        <div className="layout-five-nav-item">
          <video id="video3"></video>
        </div>
        <div className="layout-five-nav-item">
          <video id="video4"></video>
        </div>
      </div>
      <div className="layout-five-main">
        <video id="preview"></video>
      </div>
    </div>
  );
}
