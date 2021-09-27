import { useRef, useState, useEffect, useMemo, useReducer } from 'react';
import { IRouteComponentProps } from 'umi';
import { createDom, randomNum, setPre, getUrlWithString } from '@/utils';
import { message, Input } from 'antd';
import update from 'immutability-helper';
import Clipboard from 'clipboard';
import styles from './index.less';
import '../../room/anime.less';

const { TextArea } = Input;

const maxAnameCount = 6;
const { EventNameEnum } = window.RoomPaasSdk;
let likeBubbleCount = 0;
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
let currentRoomDetail = {};
const clipboard = new Clipboard('.share');
clipboard.on('success', () => {
  message.success('播放地址已复制到剪贴板');
});

export default function IndexPage(props: IRouteComponentProps) {
  let userId = '';
  const roomId = props.location.query.roomId;
  const nickname = window.localStorage.getItem('nickname') || '游客';
  const animeContainerEl = useRef<HTMLDivElement>(null);
  const chatMainEl = useRef<HTMLDivElement>(null);
  const [state, dispatch] = useReducer(reducer, initialState);
  const [likeCount, setLikeCount] = useState(0);
  const [isLiving, setIsLiving] = useState(false);
  const [isPlayback, setIsPlayback] = useState(false);
  const [isMuteAll, setIsMuteAll] = useState(false);
  const [isMuteSelf, setIsMuteSelf] = useState(false);
  const [messageValue, setMessageValue] = useState('');
  const roomChannel = useMemo(() => {
    if (!window.roomEngine) {
      props.history.replace('/m');
      window.location.reload();
    }
    return window.roomEngine.getRoomChannel(roomId);
  }, [roomId]);
  window.roomChannel = roomChannel;
  const liveService = roomChannel.getPluginService('live');
  const chatService = roomChannel.getPluginService('chat');
  // 在localstorage里获取userId
  const savedUserList = window.localStorage.getItem('userList');
  const userList = savedUserList ? JSON.parse(savedUserList) : {};
  userId = userList[nickname || ''].toString();
  const likeClickHandler = () => {
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
    if (!messageValue) return;
    const msg = messageValue.trim();
    try {
      let res = await chatService.sendComment(msg);
      setMessageValue('');
      const messageItem = {
        nickname,
        content: msg,
      };
      addMsgItem(messageItem);
    } catch (err) {
      message.error('发送失败');
    }
  };
  const leaveRoom = () => {
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
  const startPlayback = async () => {
    try {
      if (liveService.playerInstance) {
        liveService.destroy();
      }
      const liveDetail = await liveService.getLiveDetail();
      if (liveDetail.playUrl) {
        liveService.setPlayerConfig({
          container: '#player',
          width: '100%',
          height: '100%',
          isLive: false,
          autoplay: false,
          source: liveDetail.playUrl,
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
    liveService.setPlayerConfig({
      container: '#player',
      width: '100%',
      height: '100%',
      isLive: true,
      autoplay: false,
      controlBarVisibility: 'hover',
    });
    liveService.tryPlayLive();
  };
  const bindEvents = () => {
    roomChannel.on(EventNameEnum.PaaSRoomEnter, (e: any) => {
      if (userId === e.userId) return;
      const messageItem = {
        nickname: '',
        content: e.data.nick + `${e.data.enter ? '进入了房间' : '离开了房间'}`,
      };
      const roomDetail = {
        ...currentRoomDetail,
        onlineCount: e.data.onlineCount,
      };
      currentRoomDetail = { ...roomDetail };
      dispatch({
        type: actionTypes.setRoomDetail,
        payload: {
          ...roomDetail,
        },
      });
      addMsgItem(messageItem);
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
      if (userId === e.data.creatorOpenId) return;
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
    });
    liveService.on(EventNameEnum.PaaSLiveStop, (e: any) => {
      const messageItem = {
        nickname: '',
        content: '直播结束',
      };
      addMsgItem(messageItem);
      setIsLiving(false);
      startPlayback();
    });
  };
  useEffect(() => {
    document.documentElement.style.background = '#161719';
    const hide = message.loading('正在加载中', 0);
    roomChannel
      .enterRoom(nickname)
      .then((roomDetail: any) => {
        dispatch({
          type: actionTypes.setRoomDetail,
          payload: roomDetail,
        });
        currentRoomDetail = roomDetail;
        return chatService.listComment(0, 1, 10);
      })
      .then((res: any) => {
        const messageItem = {
          nickname: '',
          content: nickname + '进入了房间',
        };
        addMsgItem(messageItem);
        const messages: Message[] = [];
        res.commentModelList.forEach((item: any) => {
          messages.unshift({
            nickname: item.creatorNick,
            content: getUrlWithString(item.content),
          });
        });
        setMessageArray(messages);
        return Promise.all([
          liveService.getLiveDetail(),
          chatService.getChatDetail(),
        ]);
      })
      .then((res: any) => {
        if (res[0].status === 1) {
          // 正在直播中
          setIsLiving(true);
          // 开启拉流播放
          startLive();
        } else if (res[0].status === 2) {
          setIsPlayback(true);
          startPlayback();
        }
        liveService.on(EventNameEnum.PaaSPlayerEvent, (data: any) => {
          if (data.eventName === 'error') console.log(data);
        });
        setLikeCount(res[1].likeCount || 0);
        setIsMuteAll(res[1].mute);
        // 绑定事件
        bindEvents();
      })
      .catch((err: any) => {
        console.error(err);
        message.error('加载失败，请检查参数！');
      })
      .finally(() => {
        hide();
      });
    window.onbeforeunload = () => {
      leaveRoom();
    };
    return () => {
      document.documentElement.style.background = '#fff';
      leaveRoom();
    };
  }, []);
  return (
    <div className={styles['room-page']}>
      <div className={styles.container}>
        <div className={styles.player_container}>
          <div className={styles.player_header}>
            <div className={styles.live_info}>
              <div className={styles.avatar} onClick={setPre}>
                <img src="//img.alicdn.com/imgextra/i1/O1CN01CQ4ImC1uxDtOFDJPQ_!!6000000006103-2-tps-112-112.png" />
              </div>
              <div className={styles.info_main}>
                <div className={styles.title}>
                  {state.roomDetail.title || '直播间'}
                </div>
                <div className={styles.data}>
                  <div className={styles.data_item}>
                    <svg className="icon" aria-hidden="true">
                      <use xlinkHref="#icon-ic_header_guankan"></use>
                    </svg>
                    {state.roomDetail.onlineCount}
                  </div>
                  <div className={styles.data_item}>
                    <svg
                      className="icon"
                      aria-hidden="true"
                      style={{ fontSize: '20px' }}
                    >
                      <use xlinkHref="#icon-ic_header_dianzan"></use>
                    </svg>
                    {likeCount}
                  </div>
                  <div
                    className={`${styles.data_item} share`}
                    style={{ cursor: 'pointer' }}
                    data-clipboard-text={window.location.href}
                  >
                    <svg
                      className="icon"
                      aria-hidden="true"
                      style={{ fontSize: '20px' }}
                    >
                      <use xlinkHref="#icon-ic_header_fenxiang"></use>
                    </svg>
                    分享
                  </div>
                </div>
              </div>
            </div>
            {/* <div className={styles.player_header_custom}>
              <span>自定义拓展区域</span>
            </div> */}
          </div>
          <div className={styles.player_main}>
            {!isLiving && !isPlayback && <div className={styles.nolive}></div>}
            <div
              id="player"
              className={`prism-player ${
                !isLiving && !isPlayback ? 'hidden' : ''
              } ${styles.player}`}
            ></div>
          </div>
          <div className={styles.player_footer}>
            {/* <div className={styles.player_footer_custom}>
              <span>自定义拓展区域</span>
            </div> */}
          </div>
        </div>
        <div className={styles.chat_container}>
          {/* <div className={styles.chat_header}>
            <div className={styles.chat_header_custom}>
              <span>自定义拓展区域</span>
            </div>
          </div> */}
          <div className={styles.chat_main} ref={chatMainEl}>
            <div
              className={`${styles['chat-item']} ${styles['chat-item-notice']}`}
            >
              欢迎大家来到直播间！直播间内严禁出现违法违规、低俗色情、吸烟酗酒等内容，若有违规行为请及时举报。
            </div>
            {state.messageArray.map((data, index) => (
              <div
                className={`${styles['chat-item']} ${
                  data.nickname ? '' : styles['chat-item-notice']
                }`}
                key={index}
              >
                <span className={styles.emphasize}>{data.nickname ? data.nickname + '：' : ''}</span>
                <span dangerouslySetInnerHTML={{ __html: data.content }}></span>
              </div>
            ))}
          </div>
          <div className={styles.chat_footer}>
            <TextArea
              className={styles.chat_textarea}
              autoSize={true}
              placeholder={
                !isMuteAll && !isMuteSelf
                  ? '和主播说点什么...'
                  : `${isMuteAll ? '主播已开启全员禁言...' : '您已被禁言...'}`
              }
              disabled={isMuteAll || isMuteSelf}
              onKeyDown={sendComment}
              value={messageValue}
              onChange={(e) => setMessageValue(e.target.value)}
            ></TextArea>
            <div className={styles.like_btn} onClick={likeClickHandler}>
              <svg className="icon" aria-hidden="true">
                <use xlinkHref="#icon-ic_dialog_dianzan1"></use>
              </svg>
              <div
                className={styles['like-anime-container']}
                ref={animeContainerEl}
              ></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
