import Emitter from '../../../utils/emitter';
import './player.less';
import { useEffect } from 'react';
const emitter = Emitter.getInstance();

export default function Player(props: any) {
  const initChangeSize = () => {
    const fatherElement = document.querySelector('.player');
    const videoElement = document.querySelector('#J_player') as HTMLElement;
    window.onresize = () => {
      if (!fatherElement || !videoElement) return;
      const width = fatherElement.clientWidth;
      const height = fatherElement.clientHeight;
      videoElement.style.width = width + 'px';
      videoElement.style.height = height + 'px';
    };
  };
  const setSize = () => {
    setTimeout(() => {
      const fatherElement = document.querySelector('.video');
      const videoElement = document.querySelector('#J_player') as HTMLElement;
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
    });
  };
  const mainTsx = () => {
    setSize();
    return (
      <div className="player">
        <div id="J_player"></div>
      </div>
    );
  };
  useEffect(() => {
    setSize();
    initChangeSize();
    emitter.on('windowChange', setSize);
    return () => {
      window.onresize = null;
      emitter.remove('windowChange', setSize);
    };
  }, []);
  return mainTsx();
}
