import { useRef, useState, useEffect, useReducer } from 'react';
import { IRouteComponentProps } from 'umi';
import {
  createDom,
  randomNum,
  splitSearch,
  UA,
  BasicMap,
  getUrlWithString,
} from '@/utils';
import { message } from 'antd';
import { doLogin } from '@/biz/doLogin';
import update from 'immutability-helper';
import Clipboard from 'clipboard';
import '@/styles/anime.less';
import PC from '@/components/pc';
import Mobile from '@/components/mobile';

declare global {
  interface Window {
    RoomPaasSdk: any;
    roomEngine: any;
    roomChannel: any;
    chatService: any;
    liveService: any;
  }
}
const maxAnameCount = 6;
const { EventNameEnum } = window.RoomPaasSdk;
let likeBubbleCount = 0;
let sendingCommentFlag = false;

interface Message {
  nickname: string;
  content: string;
}
interface RoomDetail {
  title: string;
  onlineCount: number;
  ownerId: string;
  pluginInstanceModelList: any[];
  roomId: string;
  uv: number;
  pv: number;
  notice: string;
}
interface IStore {
  messageArray: Message[];
  roomDetail: RoomDetail;
}
interface IAction {
  type: string;
  payload: any;
}
const initialState: IStore = {
  messageArray: [],
  roomDetail: {
    title: '直播间',
    onlineCount: 0,
    ownerId: '',
    pluginInstanceModelList: [],
    roomId: '',
    uv: 0,
    notice: '',
    pv: 0,
  },
};
const actionTypes = {
  addMsg: 'addMsg',
  setRoomDetail: 'setRoomDetail',
};
const reducer = (state: IStore, action: IAction): IStore => {
  switch (action.type) {
    case actionTypes.addMsg:
      return update(state, {
        messageArray: {
          $push: Array.isArray(action.payload)
            ? action.payload
            : [action.payload],
        },
      });
    case actionTypes.setRoomDetail:
      console.log(
        update(state, {
          $set: {
            ...action.payload,
          },
        }),
      );
      return update(state, {
        roomDetail: {
          $set: {
            ...action.payload,
          },
        },
      });
    default:
      return state;
  }
};
let roomEngine: any, roomChannel: any, liveService: any, chatService: any;
let nickname: string, userId: string;
let liveDetail: any, liveId: string;
let currentRoomDetail = {};
const clipboard = new Clipboard('.share');
clipboard.on('success', () => {
  message.success('播放地址已复制到剪贴板');
});
export default function IndexPage(props: IRouteComponentProps) {
  const animeContainerEl = useRef<HTMLDivElement>(null);
  const chatMainEl = useRef<HTMLDivElement>(null);
  const [state, dispatch] = useReducer(reducer, initialState);
  const [likeCount, setLikeCount] = useState(0);
  const [isLiving, setIsLiving] = useState(false);
  const [isNotStart, setIsNotStart] = useState(false);
  const [isPlayback, setIsPlayback] = useState(false);
  const [isStopped, setIsStopped] = useState(false);
  const [isOnError, setIsOnError] = useState(false);
  const [isMuteAll, setIsMuteAll] = useState(false);
  const [isMuteSelf, setIsMuteSelf] = useState(false);
  const [messageValue, setMessageValue] = useState('');
  const [showNotice, setShowNotice] = useState(false);
  const [needUserClick, setNeedUserClick] = useState(true);
  const [preStartTime, setPreStartTime] = useState(0);
  const [anchorNick, setAnchorNick] = useState('');
  const [anchorInfo, setAnchorInfo] = useState({
    anchorAvatarURL: '',
    anchorIntroduction: '',
    liveIntroduction: '',
  });
  const waitingTimeout = useRef<any>(null);

  const noticeClickHandler = () => {
    setShowNotice(!showNotice);
  };
  const userStartPlay = () => {
    setNeedUserClick(false);
    liveService.startPlay();
  };
  const likeClickHandler = () => {
    if (isNotStart) return; // 未开始前不可用
    const animeRdm = randomNum(maxAnameCount, 1);
    const bubble = createDom('div', {
      class: `bubble anime-${animeRdm}`,
      id: `bubble-${likeBubbleCount}`,
    });
    let nowCount = likeBubbleCount;
    likeBubbleCount++;
    (animeContainerEl.current as HTMLDivElement).append(bubble);
    setTimeout(() => {
      (animeContainerEl.current as HTMLDivElement).removeChild(
        document.querySelector(`#bubble-${nowCount}`) as HTMLDivElement,
      );
    }, 1500);
    setLikeCount(likeCount + 1);
    chatService.sendLike();
  };
  const sendComment = async (e: any) => {
    if (e.keyCode !== 13) return;
    e.preventDefault();
    if (!messageValue) return;
    if (sendingCommentFlag) return;
    sendingCommentFlag = true;
    const msg = messageValue.trim();
    if (!msg) return;
    try {
      let res = await chatService.sendComment(msg);
      setMessageValue('');
      const messageItem = {
        nickname,
        content: msg,
      };
      addMsgItem(messageItem);
    } catch (err: any) {
      if (
        err &&
        err.body &&
        err.body.reason &&
        err.body.reason === 'BAN_COMMENT'
      )
        message.error('您已被禁言');
      else message.error('发送失败');
    } finally {
      sendingCommentFlag = false;
    }
  };
  const leaveRoom = () => {
    if (!roomChannel) return;
    roomChannel.leaveRoom();
    if (isLiving) {
      liveService.stopPlay();
    }
  };
  const addMsgItem = (messageItem: Message) => {
    messageItem.content = getUrlWithString(messageItem.content);
    dispatch({
      type: actionTypes.addMsg,
      payload: messageItem,
    });
    chatGoBottom();
  };
  const setMessageArray = (msgs: Message[]) => {
    dispatch({
      type: actionTypes.addMsg,
      payload: msgs,
    });
    chatGoBottom();
  };
  const chatGoBottom = () => {
    const scrollHeight = chatMainEl.current?.scrollHeight || 0;
    const clientHeight = chatMainEl.current?.clientHeight || 0;
    chatMainEl.current?.scrollTo(0, scrollHeight - clientHeight);
  };
  const stopPlay = () => {
    if (liveService.playerInstance) {
      liveService.stopPlay();
    }
  };
  const startPlayback = async () => {
    try {
      stopPlay();
      liveDetail = await roomEngine.getLiveDetail(liveId);
      if (liveDetail.playUrl) {
        liveService.setPlayerConfig({
          container: '#player',
          width: '100%',
          height: '100%',
          isLive: false,
          autoplay: false,
          source: liveDetail.playUrl,
          aliplayerSdkVer: '2.9.14',
        });
        await liveService.livePlayerManager.createPlayer(
          liveService.playerConfig,
        );
        setIsPlayback(true);
      }
    } catch (err) {
      message.error('开始回放时出错');
    }
  };
  const startLive = () => {
    if (liveService.playerInstance) {
      liveService.destroy();
    }
    const confg: BasicMap<any> = {
      container: '#player',
      width: '100%',
      height: '100%',
      isLive: true,
      autoplay: true,
      controlBarVisibility: UA.isPC ? 'hover' : 'never',
    };
    if (!UA.isPC) {
      confg.skinLayout = [
        { name: 'bigPlayButton', align: 'cc' },
        {
          name: 'H5Loading',
          align: 'cc',
        },
        { name: 'tooltip', align: 'blabs', x: 0, y: 56 },
        { name: 'thumbnail' },
        {
          name: 'controlBar',
          align: 'blabs',
          x: 0,
          y: 0,
          children: [
            { name: 'liveDisplay', align: 'tlabs', x: 15, y: 6 },
            { name: 'fullScreenButton', align: 'tr', x: 10, y: 12 },
            { name: 'timeDisplay', align: 'tl', x: 10, y: 7 },
            // { name: 'volume', align: 'tr', x: 5, y: 10 },
          ],
        },
      ];
    }
    liveService.setPlayerConfig(confg);
    liveService.tryPlayLive();
  };
  const refreshPlayer = () => {
    setIsLiving(true);
    setIsOnError(false);
    stopPlay();
    startLive();
  };
  const requestFullScreen = () => {
    if (!liveService.playerInstance) return;
    liveService.playerInstance.fullscreenService.requestFullScreen();
  };
  function getControlBar() {
    if (!liveService.playerInstance) return;
    return liveService.playerInstance._children.filter(
      (item: any) => item._options.name === 'controlBar',
    )[0];
  }
  const bindEvents = () => {
    roomChannel.on(EventNameEnum.PaaSRoomEnter, (e: any) => {
      console.log(e);
      if (userId.toString() === e.data.userId.toString()) return;
      const roomDetail = {
        ...currentRoomDetail,
        onlineCount: e.data.onlineCount,
        pv: e.data.pv,
      };
      currentRoomDetail = { ...roomDetail };
      dispatch({
        type: actionTypes.setRoomDetail,
        payload: {
          ...roomDetail,
        },
      });
    });
    chatService.on(EventNameEnum.PaaSChatMuteAll, (e: any) => {
      const messageItem = {
        nickname: '',
        content: `${e.data.mute ? '主播已开启全员禁言' : '主播已解除全员禁言'}`,
      };
      setIsMuteAll(e.data.mute);
      addMsgItem(messageItem);
    });
    chatService.on(EventNameEnum.PaaSChatMuteUser, (e: any) => {
      const messageItem = {
        nickname: '',
        content: `${
          e.data.mute
            ? `${e.data.muteUserNick}已被禁言`
            : `${e.data.muteUserNick}已被解除禁言`
        }`,
      };
      addMsgItem(messageItem);
      if (e.data.muteUserOpenId === userId) {
        setIsMuteSelf(e.data.mute);
      }
    });
    chatService.on(EventNameEnum.PaaSChatReciveLike, (e: any) => {
      const { likeCount = 0 } = e.data || {};
      setLikeCount(likeCount);
    });
    chatService.on(EventNameEnum.PaaSChatReciveComment, (e: any) => {
      if (userId.toString() === e.data.creatorOpenId.toString()) return;
      const messageItem = {
        nickname: e.data.creatorNick,
        content: e.data.content,
      };
      addMsgItem(messageItem);
    });
    liveService.on(EventNameEnum.PaaSLivePublish, (e: any) => {
      const messageItem = {
        nickname: '',
        content: '直播开始',
      };
      addMsgItem(messageItem);
      setIsLiving(true);
      startLive();
      setIsNotStart(false);
    });
    liveService.on(EventNameEnum.PaaSLiveStop, (e: any) => {
      const messageItem = {
        nickname: '',
        content: '直播结束',
      };
      addMsgItem(messageItem);
      setIsOnError(false);
      setIsStopped(true);
    });
    liveService.on(EventNameEnum.PaaSLiveStreamStart, (e: any) => {
      console.log('推流', e);
      setIsLiving(true);
      startLive();
    });
    liveService.on(EventNameEnum.PaaSLiveStreamStop, (e: any) => {
      console.log('断流', e);
      setIsLiving(false);
      stopPlay();
    });
    liveService.on(EventNameEnum.PaaSPlayerEvent, (data: any) => {
      // 单独处理android端全屏的情况
      if (data.eventName === 'requestFullScreen' && UA.isAndroid) {
        const cb = getControlBar();
        cb && cb.show();
      }
      if (data.eventName === 'cancelFullScreen' && UA.isAndroid) {
        const cb = getControlBar();
        cb && cb.hide();
      }
      // ios全屏切回时只会触发pause，自动触发下播放
      if (data.eventName === 'pause' && UA.isiPhone) {
        liveService.startPlay();
      }
      // aliplayer bug，可能会出现无限加载状态
      if (data.eventName === 'waiting') {
        waitingTimeout.current = setTimeout(() => {
          // 防止无限加载
          setIsOnError(true);
        }, 5000);
      }
      if (data.eventName === 'timeupdate') {
        // 触发后说明播放了，cleartimeout
        if (waitingTimeout.current) {
          clearTimeout(waitingTimeout.current);
          waitingTimeout.current = null;
        }
      }
      // 错误处理
      if (data.eventName === 'error') {
        setIsOnError(true);
        message.error('播放错误，请刷新重试');
      }
      // 触发liveStreamStop说明直播流断了
      if (data.eventName === 'liveStreamStop') {
        setIsLiving(false);
      }
      // playing等于开始正常播放，触发次数少于timeupdate
      if (data.eventName === 'playing') {
        setNeedUserClick(false);
        setIsLiving(true);
        setIsOnError(false);
      }
    });
    chatService.on(EventNameEnum.PaaSChatCustomMessage, (e: any) => {
      // 用来监听推流/断流消息
      if (e.data.EventType === 'system.live.publishDone') {
        // 断流
        stopPlay();
        setIsLiving(false);
        return;
      }
      if (e.data.EventType === 'system.live.publish') {
        // 推流
        setIsLiving(true);
        startLive();
        return;
      }
    });
  };
  useEffect(() => {
    let query;
    // hash模式下表现很诡异，#在前面时算hash，在后面算search，这里适配两种情况
    const search = splitSearch(window.location.search);
    search.nick ? (query = search) : (query = props.location.query);
    nickname = decodeURI(query.nick?.toString()) || '';
    userId = query.userId?.toString() || '';
    liveId = query.liveId?.toString() || '';
    document.documentElement.style.background = '#161719';
    const hide = message.loading('正在加载中', 0);
    // 登录
    doLogin(nickname, userId)
      .then((engineInstance: any) => {
        roomEngine = engineInstance;
        // 获取直播详情
        return roomEngine.getLiveDetail(liveId);
      })
      .then((liveDetailRes: any) => {
        liveDetail = liveDetailRes;
        // 获取roomChannel实例和service实例
        roomChannel = roomEngine.getRoomChannel(liveDetail.roomId);
        chatService = roomChannel.getPluginService('chat');
        liveService = roomChannel.getPluginService('live');
        setIsNotStart(liveDetail.status === 0);
        // 进入房间
        return roomChannel.enterRoom(nickname);
      })
      .then((roomDetail: any) => {
        dispatch({
          type: actionTypes.setRoomDetail,
          payload: {
            ...roomDetail,
            notice: getUrlWithString(roomDetail.notice),
          },
        });
        document.title = roomDetail.title;
        setPreStartTime(Number(roomDetail.extension.preStartTime) || 0);
        setAnchorInfo({
          anchorAvatarURL:
            roomDetail.extension.anchorAvatarURL ||
            '//img.alicdn.com/imgextra/i3/O1CN01oUc6fp20jNfrutiBF_!!6000000006885-2-tps-102-102.png',
          anchorIntroduction:
            roomDetail.extension.anchorIntroduction || '暂无简介',
          liveIntroduction: roomDetail.extension.liveIntroduction || '暂无简介',
        });
        setAnchorNick(roomDetail.extension.anchorNick || liveDetail.anchorId);
        currentRoomDetail = roomDetail;
        // 拉流
        if (liveDetail.status === 1) {
          // 正在直播中
          setIsLiving(true);
          // 开启拉流播放
          startLive();
        } else if (liveDetail.status === 2) {
          setIsStopped(true);
          // startPlayback();
        }
        // 获取互动详情
        return chatService.getChatDetail();
      })
      .then((chatDetail: any) => {
        setLikeCount(chatDetail.likeCount || 0);
        setIsMuteSelf(chatDetail.mute);
        setIsMuteAll(chatDetail.muteAll);
        // 绑定事件
        bindEvents();
        chatService.listComment(0, 1, 20).then((res: any) => {
          const messages: Message[] = [];
          res.commentModelList.forEach((item: any) => {
            messages.push({
              nickname: item.creatorNick,
              content: getUrlWithString(item.content),
            });
          });
          setMessageArray(messages);
        });
      })
      .catch((err) => {
        console.error(err);
        message.error('加载失败，请检查参数！');
      })
      .finally(() => {
        hide();
      });
    window.addEventListener(
      'beforeunload',
      () => {
        leaveRoom();
      },
      true,
    );
    return () => {
      document.documentElement.style.background = '#fff';
      leaveRoom();
    };
  }, []);
  return UA.isPC ? (
    <PC
      state={state}
      chatMainEl={chatMainEl}
      likeCount={likeCount}
      isLiving={isLiving}
      isPlayback={isPlayback}
      isStopped={isStopped}
      isMuteAll={isMuteAll}
      isMuteSelf={isMuteSelf}
      sendComment={sendComment}
      messageValue={messageValue}
      setMessageValue={setMessageValue}
      likeClickHandler={likeClickHandler}
      animeContainerEl={animeContainerEl}
      refreshPlayer={refreshPlayer}
      preStartTime={preStartTime}
      isOnError={isOnError}
      isNotStart={isNotStart}
    />
  ) : (
    <Mobile
      state={state}
      chatMainEl={chatMainEl}
      likeCount={likeCount}
      isLiving={isLiving}
      isPlayback={isPlayback}
      isStopped={isStopped}
      isMuteAll={isMuteAll}
      isMuteSelf={isMuteSelf}
      sendComment={sendComment}
      messageValue={messageValue}
      setMessageValue={setMessageValue}
      likeClickHandler={likeClickHandler}
      animeContainerEl={animeContainerEl}
      noticeClickHandler={noticeClickHandler}
      showNotice={showNotice}
      needUserClick={needUserClick}
      userStartPlay={userStartPlay}
      refreshPlayer={refreshPlayer}
      preStartTime={preStartTime}
      isOnError={isOnError}
      isNotStart={isNotStart}
      chatGoBottom={chatGoBottom}
      requestFullScreen={requestFullScreen}
      anchorNick={anchorNick}
      anchorInfo={anchorInfo}
    />
  );
}
