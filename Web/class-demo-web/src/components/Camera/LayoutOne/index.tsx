import './layoutone.less';
import { useEffect } from 'react';

export default function LayoutOne(props: any) {
  const initChangeSize = () => {
    const fatherElement = document.querySelector('.layout-one');
    const videoElement = document.querySelector(
      props.role === 'teacher' ? '#preview' : '#video1',
    ) as HTMLElement;
    window.onresize = () => {
      if (!fatherElement || !videoElement) return;
      const width = fatherElement.clientWidth;
      const height = fatherElement.clientHeight;
      videoElement.style.width = width + 'px';
      videoElement.style.height = height + 'px';
    };
  };
  const setSize = () => {
    const fatherElement =
      props.role === 'teacher'
        ? document.querySelector('.teacher-change-area-main')
        : document.querySelector('.student-change-area-main');
    const videoElement = document.querySelector(
      props.role === 'teacher' ? '#preview' : '#video1',
    ) as HTMLElement;
    if (!fatherElement || !videoElement) return;
    if (props.windowChangeData) {
      videoElement.style.width = '320px';
      videoElement.style.height = '240px';
      return;
    }
    const width = fatherElement.clientWidth;
    const height = fatherElement.clientHeight;
    videoElement.style.width = width + 'px';
    videoElement.style.height = height + 'px';
  };
  const init = () => {
    if (props.role === 'student') {
      window.rtcService.setDisplayRemoteVideo(
        document.getElementById('video1'),
        window.sessionStorage.getItem('ownerId'),
        props.isScreening ? 2 : 1,
      );
      return;
    }
    window.rtcService.startRtcPreview(document.getElementById('preview'));
  };
  const mainTsx = () => {
    setSize();
    return props.role === 'teacher' ? (
      <div className="layout-one">
        <video id="preview"></video>
      </div>
    ) : (
      <div className="layout-one">
        <video id="video1"></video>
      </div>
    );
  };
  useEffect(() => {
    if (!window.roomChannel) return;
    setSize();
    initChangeSize();
    return () => {
      window.onresize = null;
    };
  }, []);
  useEffect(() => {
    if (!window.roomChannel) return;
    init();
  }, [props.windowChangeData]);
  return mainTsx();
}
