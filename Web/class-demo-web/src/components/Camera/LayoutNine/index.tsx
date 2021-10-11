import './layoutone.less';
import { useEffect } from 'react';

export default function LayoutNine() {
  const setSize = () => {
    const wholeElement = document.querySelector('.layout-nine');
    const fatherElement = document.querySelector(
      '.layout-nine-line',
    ) as HTMLElement;
    const itemElement = document.querySelector(
      '.layout-nine-item',
    ) as HTMLElement;
    const videoElement = document.querySelector('#preview') as HTMLElement;
    if (!wholeElement || !fatherElement || !itemElement || !videoElement)
      return;
    fatherElement.style.width = wholeElement.clientWidth + 'px';
    fatherElement.style.height = wholeElement.clientHeight * 0.3333 + 'px';
    const width = fatherElement.clientWidth * 0.3333;
    const height = fatherElement.clientHeight;
    itemElement.style.width = width + 'px';
    itemElement.style.height = height + 'px';
    const littleWidth = itemElement.clientWidth;
    const littleHeight = itemElement.clientHeight;
    videoElement.style.width = littleWidth + 'px';
    videoElement.style.height = littleHeight + 'px';
    for (let i = 1; i < 9; i++) {
      const nodeId = `#video${i}`;
      const videoNode = document.querySelector(nodeId) as HTMLElement;
      videoNode.style.width = littleWidth + 'px';
      videoNode.style.height = littleHeight + 'px';
    }
  };
  const init = () => {
    window.rtcService.startRtcPreview(document.getElementById('preview'));
    const userList = window.rtcService.getUserList();
    const max = userList.length > 8 ? 8 : userList.length;
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
    <div className="layout-nine">
      <div className="layout-nine-line">
        <div className="layout-nine-item">
          <video id="preview"></video>
        </div>
        <div className="layout-nine-item">
          <video id="video1"></video>
        </div>
        <div className="layout-nine-item">
          <video id="video2"></video>
        </div>
      </div>
      <div className="layout-nine-line">
        <div className="layout-nine-item">
          <video id="video3"></video>
        </div>
        <div className="layout-nine-item">
          <video id="video4"></video>
        </div>
        <div className="layout-nine-item">
          <video id="video5"></video>
        </div>
      </div>
      <div className="layout-nine-line">
        <div className="layout-nine-item">
          <video id="video6"></video>
        </div>
        <div className="layout-nine-item">
          <video id="video7"></video>
        </div>
        <div className="layout-nine-item">
          <video id="video8"></video>
        </div>
      </div>
    </div>
  );
}
