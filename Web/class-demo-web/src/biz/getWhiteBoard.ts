import Emitter from '../utils/emitter';
declare global {
  interface Window {
    aliyunBoard: any;
    replayAliyunBoard: any;
  }
}
const { AliyunBoard } = window.aliyunBoardSDK;
const emitter = Emitter.getInstance();

export const getWhiteBoard = (userId: string) => {
  if (!window.wbService) return;
  const docKey = window.sessionStorage.getItem('docKey');
  const getDocumentData = () => {
    const url = `https://***.***.com?userId=${userId}&docKey=${docKey}`;
    return fetch(url, {
      method: 'POST',
    })
      .then((res: any) => res.json())
      .then((res: any) => res.result.documentAccessInfo)
      .catch((err: any) => {
        console.log(err);
      });
  };
  window.aliyunBoard = new AliyunBoard({
    getDocumentData,
    docKey,
    syncModel: 1,
    maxSceneCount: 1000,
    fitMode: 0,
    forceSync: true,
    pointerMultiSelect: false,
    schema: {
      size: {
        width: 40,
        height: 22.5,
        dpi: 144,
      },
      slides: [
        {
          id: '1',
          background: 'rgba(255, 255, 255, 1)',
        },
      ],
    },
  });
  window.aliyunBoard.on('ALIYUNBOARD_READY', () => {
    emitter.emit('boardReady', '');
  });
  window.aliyunBoard.on('ALIYUNBOARD_FIT', () => {
    window.aliyunBoard.setScale(1);
  });
  emitter.emit('initBoard', '');
};

export const getReplayWhiteBoard = () => {
  if (!window.wbService) return;
  const docKey = window.sessionStorage.getItem('docKey');
  const getDocumentData = () => {
    const url = `https://***.***.com?userId=${window.sessionStorage.getItem(
      'userId',
    )}&docKey=${docKey}`;
    return fetch(url, {
      method: 'POST',
    })
      .then((res: any) => res.json())
      .then((res: any) => res.result.documentAccessInfo)
      .catch((err: any) => {
        console.log(err);
      });
  };
  window.replayAliyunBoard = new AliyunBoard({
    getDocumentData,
    docKey,
    replay: true,
    fitMode: 0,
    schema: {
      size: {
        width: 40,
        height: 22.5,
        dpi: 144,
      },
    },
  });
  window.replayAliyunBoard.on('ALIYUNBOARD_FIT', () => {
    window.replayAliyunBoard.setScale(1);
  });
  emitter.emit('initBoard', '');
};
