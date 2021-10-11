import { useState, useEffect } from 'react';
import { message } from 'antd';
import Emitter from '@/utils/emitter';
import './whiteboard.less';
declare global {
  interface Window {
    aliyunBoardSDK: any;
  }
}
const { Canvas, OPReplayer } = window.aliyunBoardSDK;
export default function WhiteBoard(props: any) {
  const [toolSelect, setToolSelect] = useState(1);
  const [scaleData, setScaleData] = useState(1);
  const [nowPage, setNowPage] = useState(0);
  const [allPage, setAllPage] = useState(1);
  const [wbState, setWbState] = useState(!!window.aliyunBoard);
  const emitter = Emitter.getInstance();
  const toolsList = [
    {
      key: 1,
      icon: '#icon-zhizhen',
      tips: '指针',
      attr: {
        type: 'setToolType',
        value: 'pointer',
      },
    },
    {
      key: 2,
      icon: '#icon-kuangxuan',
      tips: '框选',
      attr: {
        type: 'setToolType',
        value: 'multiSelect',
      },
    },
    {
      key: 3,
      icon: '#icon-huabi',
      tips: '涂鸦',
      attr: {
        type: 'setToolType',
        value: 'pen',
      },
    },
    {
      key: 4,
      icon: '#icon-wenben1',
      tips: '文本',
      attr: {
        type: 'setToolType',
        value: 'text',
      },
    },
    {
      key: 5,
      icon: '#icon-jiantou',
      tips: '箭头',
      attr: {
        type: false,
        value: undefined,
      },
    },
    {
      key: 6,
      icon: '#icon-yanse',
      tips: '颜料桶',
      attr: {
        type: false,
        value: undefined,
      },
    },
    {
      key: 7,
      icon: '#icon-tupian',
      tips: '图片',
      attr: {
        type: 'addImage',
        value: undefined,
      },
    },
    {
      key: 8,
      icon: '#icon-tianjiawenjian',
      tips: '背景图',
      attr: {
        type: 'addFile',
        value: undefined,
      },
    },
    {
      key: 9,
      icon: '#icon-xiangpi',
      tips: '橡皮擦',
      attr: {
        type: 'setToolType',
        value: 'eraser',
      },
    },
    {
      key: 10,
      icon: '#icon-qingchu',
      tips: '清屏',
      attr: {
        type: 'clearBoard',
        value: undefined,
      },
    },
  ];
  const handleChangeTool = async (key: number) => {
    setToolSelect(key);
    const attr = toolsList.filter((data) => {
      return data.key === key;
    })[0].attr;
    if (!attr.type) {
      message.info('尚未开放，敬请期待');
      return;
    }
    switch (attr.type) {
      case 'setToolType':
        window.aliyunBoard.setToolType(attr.value);
        break;
      case 'clearBoard':
        window.aliyunBoard.clearBoard();
        break;
      case 'addImage':
        message.info('尚未开放，敬请期待');
        return;
      // window.aliyunBoard.addImage();
      // break;
      case 'addFile':
        /* Todo: 1. oss上传
               2. LWP接口获得urlList，此处使用mock数据*/

        const urlLists = [
          [
            'https://paas-sdk.oss-cn-shanghai.aliyuncs.com/paas/room/android/resources/sampleppt/1.png',
            'https://paas-sdk.oss-cn-shanghai.aliyuncs.com/paas/room/android/resources/sampleppt/2.png',
            'https://paas-sdk.oss-cn-shanghai.aliyuncs.com/paas/room/android/resources/sampleppt/3.png',
            'https://paas-sdk.oss-cn-shanghai.aliyuncs.com/paas/room/android/resources/sampleppt/4.png',
            'https://paas-sdk.oss-cn-shanghai.aliyuncs.com/paas/room/android/resources/sampleppt/5.png',
            'https://paas-sdk.oss-cn-shanghai.aliyuncs.com/paas/room/android/resources/sampleppt/6.png',
            'https://paas-sdk.oss-cn-shanghai.aliyuncs.com/paas/room/android/resources/sampleppt/7.png',
            'https://paas-sdk.oss-cn-shanghai.aliyuncs.com/paas/room/android/resources/sampleppt/8.png',
            'https://paas-sdk.oss-cn-shanghai.aliyuncs.com/paas/room/android/resources/sampleppt/9.png',
            'https://paas-sdk.oss-cn-shanghai.aliyuncs.com/paas/room/android/resources/sampleppt/10.png',
            'https://paas-sdk.oss-cn-shanghai.aliyuncs.com/paas/room/android/resources/sampleppt/11.png',
            'https://paas-sdk.oss-cn-shanghai.aliyuncs.com/paas/room/android/resources/sampleppt/12.png',
            'https://paas-sdk.oss-cn-shanghai.aliyuncs.com/paas/room/android/resources/sampleppt/13.png',
            'https://paas-sdk.oss-cn-shanghai.aliyuncs.com/paas/room/android/resources/sampleppt/14.png',
            'https://paas-sdk.oss-cn-shanghai.aliyuncs.com/paas/room/android/resources/sampleppt/15.png',
            'https://paas-sdk.oss-cn-shanghai.aliyuncs.com/paas/room/android/resources/sampleppt/16.png',
            'https://paas-sdk.oss-cn-shanghai.aliyuncs.com/paas/room/android/resources/sampleppt/17.png',
            'https://paas-sdk.oss-cn-shanghai.aliyuncs.com/paas/room/android/resources/sampleppt/18.png',
            'https://paas-sdk.oss-cn-shanghai.aliyuncs.com/paas/room/android/resources/sampleppt/19.png',
            'https://paas-sdk.oss-cn-shanghai.aliyuncs.com/paas/room/android/resources/sampleppt/20.png',
            'https://paas-sdk.oss-cn-shanghai.aliyuncs.com/paas/room/android/resources/sampleppt/21.png',
            'https://paas-sdk.oss-cn-shanghai.aliyuncs.com/paas/room/android/resources/sampleppt/22.png',
            'https://paas-sdk.oss-cn-shanghai.aliyuncs.com/paas/room/android/resources/sampleppt/23.png',
            'https://paas-sdk.oss-cn-shanghai.aliyuncs.com/paas/room/android/resources/sampleppt/24.png',
            'https://paas-sdk.oss-cn-shanghai.aliyuncs.com/paas/room/android/resources/sampleppt/25.png',
            'https://paas-sdk.oss-cn-shanghai.aliyuncs.com/paas/room/android/resources/sampleppt/26.png',
            'https://paas-sdk.oss-cn-shanghai.aliyuncs.com/paas/room/android/resources/sampleppt/27.png',
            'https://paas-sdk.oss-cn-shanghai.aliyuncs.com/paas/room/android/resources/sampleppt/28.png',
            'https://paas-sdk.oss-cn-shanghai.aliyuncs.com/paas/room/android/resources/sampleppt/29.png',
            'https://paas-sdk.oss-cn-shanghai.aliyuncs.com/paas/room/android/resources/sampleppt/30.png',
            'https://paas-sdk.oss-cn-shanghai.aliyuncs.com/paas/room/android/resources/sampleppt/31.png',
            'https://paas-sdk.oss-cn-shanghai.aliyuncs.com/paas/room/android/resources/sampleppt/32.png',
            'https://paas-sdk.oss-cn-shanghai.aliyuncs.com/paas/room/android/resources/sampleppt/33.png',
            'https://paas-sdk.oss-cn-shanghai.aliyuncs.com/paas/room/android/resources/sampleppt/34.png',
            'https://paas-sdk.oss-cn-shanghai.aliyuncs.com/paas/room/android/resources/sampleppt/35.png',
            'https://paas-sdk.oss-cn-shanghai.aliyuncs.com/paas/room/android/resources/sampleppt/36.png',
            'https://paas-sdk.oss-cn-shanghai.aliyuncs.com/paas/room/android/resources/sampleppt/37.png',
            'https://paas-sdk.oss-cn-shanghai.aliyuncs.com/paas/room/android/resources/sampleppt/38.png',
            'https://paas-sdk.oss-cn-shanghai.aliyuncs.com/paas/room/android/resources/sampleppt/39.png',
            'https://paas-sdk.oss-cn-shanghai.aliyuncs.com/paas/room/android/resources/sampleppt/40.png',
            'https://paas-sdk.oss-cn-shanghai.aliyuncs.com/paas/room/android/resources/sampleppt/41.png',
            'https://paas-sdk.oss-cn-shanghai.aliyuncs.com/paas/room/android/resources/sampleppt/42.png',
          ],
          [
            'https://thumbs.dreamstime.com/b/event-curtain-3514076.jpg',
            'https://thumbs.dreamstime.com/b/hand-open-stage-red-curtain-black-background-75322755.jpg',
          ],
          [
            'https://thumbs.dreamstime.com/b/sunset-beach-tropical-palm-tree-over-beautiful-sky-palms-background-tourism-vacation-concept-backdrop-silhouettes-orange-110749931.jpg',
            'https://thumbs.dreamstime.com/b/sunset-beach-sunrays-133301221.jpg',
            'https://thumbs.dreamstime.com/b/couple-enjoy-luxury-sunset-beach-happy-summer-vacations-61373709.jpg',
            'https://thumbs.dreamstime.com/b/yoga-sunset-beach-woman-doing-yoga-performing-asanas-enjoying-life-sea-53788457.jpg',
            'https://thumbs.dreamstime.com/b/sunset-beach-thailand-43233574.jpg',
          ],
          [
            'https://thumbs.dreamstime.com/b/sun-rays-mountain-landscape-5721010.jpg',
            'https://thumbs.dreamstime.com/b/idyllic-summer-landscape-clear-mountain-lake-alps-panoramic-view-fresh-green-pastures-61422408.jpg',
            'https://thumbs.dreamstime.com/b/panorama-mountain-landscape-sunset-slovakia-vrsatec-40370262.jpg',
            'https://thumbs.dreamstime.com/b/mountains-flowers-blossom-sunrise-mountain-hills-beautiful-natural-landscape-summer-time-background-136448735.jpg',
          ],
        ];
        const urlList = urlLists[Math.floor(Math.random() * 4)];
        const index = window.aliyunBoard.getCurrentSceneIndex();
        for (let i in urlList) {
          await window.aliyunBoard.addScene();
          await window.aliyunBoard.addBackgroundImage(urlList[i]);
        }
        window.aliyunBoard.gotoScene(index + 1);
        setNowPage(index + 1);
        setAllPage(allPage + urlList.length);
        setToolSelect(-1);
        break;
      default:
        break;
    }
  };
  const handleReduce = () => {
    if (scaleData <= 0.1) return;
    const scale = Number(window.aliyunBoard.getScale().toFixed(2));
    window.aliyunBoard.setScale(scale - 0.1);
    setScaleData(scaleData - 0.1);
  };
  const handleIncrease = () => {
    const scale = Number(window.aliyunBoard.getScale().toFixed(2));
    window.aliyunBoard.setScale(scale + 0.1);
    setScaleData(scaleData + 0.1);
  };
  const handlePageUp = () => {
    if (nowPage === 0) return;
    window.aliyunBoard.preScene();
    setNowPage(nowPage - 1);
  };
  const handlePageDown = () => {
    if (nowPage === allPage - 1) return;
    window.aliyunBoard.nextScene();
    setNowPage(nowPage + 1);
  };
  const init = () => {
    if (!window.aliyunBoard) return;
    window.aliyunBoard.setToolType('point');
    if (props.role === 'student') {
      window.aliyunBoard.setReadOnly(true);
      return;
    }
    const nowAtPage = window.aliyunBoard.getCurrentSceneIndex();
    const allOfPage = window.aliyunBoard.getScenesCount();
    setNowPage(nowAtPage);
    setAllPage(allOfPage);
  };
  const changeWhiteBoard = () => {
    if (props.replayState) {
      return (
        <OPReplayer
          model={window.replayAliyunBoard}
          docKey={window.sessionStorage.getItem('docKey')}
          accessToken={window.sessionStorage.getItem('accessToken')}
          recordId={window.sessionStorage.getItem('recordId')}
          // style={{ height: '100%', width: '100%' }}
          containerStyle={{ height: '100%' }}
        />
      );
    }
    if (wbState) {
      return (
        <Canvas
          model={window.aliyunBoard}
          style={{ height: '100%', width: '100%' }}
        />
      );
    }
    return <></>;
  };
  const usePage = () => {
    return (
      <div className="whiteboard-page-ctrl">
        <div className="whiteboard-page-ctrl-flip">
          <div
            className="whiteboard-page-ctrl-flip-item"
            onClick={() => {
              window.aliyunBoard.gotoScene(0);
              setNowPage(0);
            }}
          >
            <svg className="icon" aria-hidden="true">
              <use xlinkHref="#icon-xiangzuofanye"></use>
            </svg>
          </div>
          <div
            className="whiteboard-page-ctrl-flip-item"
            onClick={handlePageUp}
          >
            <svg className="icon" aria-hidden="true">
              <use xlinkHref="#icon-fanye1"></use>
            </svg>
          </div>
          <div className="whiteboard-page-ctrl-flip-text">
            {nowPage + 1}/{allPage}
          </div>
          <div
            className="whiteboard-page-ctrl-flip-item"
            onClick={handlePageDown}
          >
            <svg className="icon" aria-hidden="true">
              <use xlinkHref="#icon-fanye"></use>
            </svg>
          </div>
          <div
            className="whiteboard-page-ctrl-flip-item"
            onClick={() => {
              window.aliyunBoard.gotoScene(allPage - 1);
              setNowPage(allPage - 1);
            }}
          >
            <svg className="icon" aria-hidden="true">
              <use xlinkHref="#icon-xiangyoufanye"></use>
            </svg>
          </div>
        </div>
        <div
          className="whiteboard-page-ctrl-addpage"
          onClick={() => {
            window.aliyunBoard.addScene();
            setNowPage(nowPage + 1);
            setAllPage(allPage + 1);
          }}
        >
          <svg className="icon" aria-hidden="true">
            <use xlinkHref="#icon-tianjiawenjian"></use>
          </svg>
        </div>
      </div>
    );
  };
  const useZoom = () => {
    return (
      <div className="whiteboard-size-ctrl">
        <div className="whiteboard-size-ctrl-allscreen">
          <svg className="icon" aria-hidden="true">
            <use xlinkHref="#icon-kuangxuan2"></use>
          </svg>
        </div>
        <div className="whiteboard-size-ctrl-zoom">
          <div
            className="whiteboard-size-ctrl-zoom-item"
            onClick={handleReduce}
          >
            <svg className="icon" aria-hidden="true">
              <use xlinkHref="#icon-jian"></use>
            </svg>
          </div>
          <div className="whiteboard-size-ctrl-zoom-text">
            {(scaleData * 100).toFixed(0)}%
          </div>
          <div
            className="whiteboard-size-ctrl-zoom-item"
            onClick={handleIncrease}
          >
            <svg className="icon" aria-hidden="true">
              <use xlinkHref="#icon-jia"></use>
            </svg>
          </div>
        </div>
        <div className="whiteboard-size-ctrl-shot">
          <svg className="icon" aria-hidden="true">
            <use xlinkHref="#icon-jiandao"></use>
          </svg>
        </div>
      </div>
    );
  };
  const useOpration = () => {
    return (
      <div className="whiteboard-option-ctrl">
        <div
          className="whiteboard-option-ctrl-item"
          onClick={() => {
            window.aliyunBoard.undo();
          }}
        >
          <svg className="icon" aria-hidden="true">
            <use xlinkHref="#icon-chehui"></use>
          </svg>
        </div>
        <div
          className="whiteboard-option-ctrl-item"
          onClick={() => {
            window.aliyunBoard.redo();
          }}
        >
          <svg className="icon" aria-hidden="true">
            <use xlinkHref="#icon-zhongzuo"></use>
          </svg>
        </div>
      </div>
    );
  };
  const useTool = () => {
    return (
      <div className="whiteboard-tools">
        {toolsList.map((data) => {
          return (
            <div
              key={data.key}
              className={`whiteboard-tools-item ${
                data.key === toolSelect ? 'active' : ''
              }`}
              onClick={() => {
                handleChangeTool(data.key);
              }}
              title={data.tips}
            >
              <svg className="icon" aria-hidden="true">
                <use xlinkHref={data.icon}></use>
              </svg>
            </div>
          );
        })}
      </div>
    );
  };
  const whiteboardTool = () => {
    if (props.windowChangeData) {
      window.aliyunBoard.setScale(scaleData);
      return (
        <>
          {props.role === 'teacher' ? useTool() : ''}
          <div className="whiteboard-ctrl">
            {props.role === 'teacher' ? useOpration() : ''}
            {useZoom()}
            {props.role === 'teacher' ? usePage() : ''}
          </div>
        </>
      );
    }
    window.aliyunBoard.setScale(scaleData * 0.3);
    return <></>;
  };
  useEffect(() => {
    emitter.on('initBoard', () => {
      setWbState(!!window.aliyunBoard);
    });
    emitter.on('boardReady', () => {
      init();
    });
  }, []);
  return (
    <div className="whiteboard">
      {changeWhiteBoard()}
      {wbState && !props.replayState ? whiteboardTool() : null}
    </div>
  );
}
