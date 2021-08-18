import { useRef, useState, useEffect, useMemo, useReducer } from 'react';
import { IRouteComponentProps } from 'umi';
import { createDom, randomNum } from '@/utils';
import { message } from 'antd';
import update from 'immutability-helper';
import styles from './index.less';
import './anime.less';

const maxAnameCount = 6;
const { EventNameEnum } = window.RoomPaasSdk;
let likeBubbleCount = 0;
interface Message {
  nickname: string;
  content: string;
}
interface IStore {
  messageArray: Message[];
}
interface IAction {
  type: string;
  payload: any;
}
const initialState: IStore = {
  messageArray: [],
};
const actionTypes = {
  addMsg: 'addMsg',
};
const reducer = (state: IStore, action: IAction): IStore => {
  switch (action.type) {
    case actionTypes.addMsg:
      return update(state, {
        messageArray: {
          $unshift: Array.isArray(action.payload)
            ? action.payload
            : [action.payload],
        },
      });
    default:
      return state;
  }
};
export default function IndexPage(props: IRouteComponentProps) {
  let userId = '';
  const roomId = props.location.query.roomId;
  const nickname = window.localStorage.getItem('nickname') || '佚名';
  const animeContainerEl = useRef<HTMLDivElement>(null);
  const [state, dispatch] = useReducer(reducer, initialState);
  const [likeCount, setLikeCount] = useState(0);
  const [showNotice, setShowNotice] = useState(false);
  const [roomDetail, setRoomDetail] = useState<any>({});
  const [isLiving, setIsLiving] = useState(false);
  const [messageValue, setMessageValue] = useState('');
  /**
   * 注：由于大部分浏览器都必须在用户主动触发事件之后才能播媒体
   * 所以这里设置一个需要点击的地方
   * 当正在直播&&刷新进入时（这个应用进入此页面只能push，所以replace说明刷新了）
   * 显示一个按钮让用户点一下
   **/
  const [needUserClick, setNeedUserClick] = useState(
    props.history.action === 'REPLACE',
  );
  // 在localstorage里获取userId
  const savedUserList = window.localStorage.getItem('userList');
  const userList = savedUserList ? JSON.parse(savedUserList) : {};
  userId = userList[nickname || ''].toString();
  // 获取channel实例
  const roomChannel = useMemo(() => {
    if (!window.roomEngine) {
      props.history.replace('/');
      window.location.reload();
    }
    return window.roomEngine.getRoomChannel(roomId);
  }, [roomId]);
  window.roomChannel = roomChannel;
  const liveService = roomChannel.getPluginService('live');
  const chatService = roomChannel.getPluginService('chat');
  // 点击点赞回调，包括动画的js实现
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
  const noticeClickHandler = () => {
    setShowNotice(!showNotice);
  };
  const sendComment = async (e: any) => {
    if (e.keyCode !== 13) return;
    if (!messageValue) return;
    try {
      let res = await chatService.sendComment(messageValue);
      console.log(res);
      setMessageValue('');
      const messageItem = {
        nickname,
        content: messageValue,
      };
      addMsgItem(messageItem);
    } catch (err) {
      message.error('发送失败');
    }
  };
  const onShare = () => {
    // 分享
    alert(`roomId: ${roomId}`);
  };
  const leaveRoom = () => {
    roomChannel.leaveRoom();
    console.log(isLiving);
    if (isLiving) {
      liveService.stopPlay();
    }
    props.history.replace('/roomList');
  };
  const addMsgItem = (messageItem: Message) => {
    console.log(messageItem);
    dispatch({
      type: actionTypes.addMsg,
      payload: messageItem,
    });
  };
  const setMessageArray = (msgs: Message[]) => {
    dispatch({
      type: actionTypes.addMsg,
      payload: msgs,
    });
  };
  const userStartPlay = () => {
    setNeedUserClick(false);
    liveService.startPlay();
  };
  const bindEvents = () => {
    roomChannel.on(EventNameEnum.PaaSRoomEnter, (eventData: any) => {
      if (userId === eventData.userId) return;
      const messageItem = {
        nickname: '',
        content:
          eventData.data.nick +
          `${eventData.data.enter ? '进入了房间' : '离开了房间'}`,
      };
      setRoomDetail({
        ...roomDetail,
        onlineCount: eventData.data.onlineCount,
      });
      addMsgItem(messageItem);
    });
    chatService.on(EventNameEnum.PaaSChatReciveLike, (eventData: any) => {
      const { likeCount = 0 } = eventData.data || {};
      setLikeCount(likeCount);
    });
    chatService.on(EventNameEnum.PaaSChatReciveComment, (eventData: any) => {
      if (userId === eventData.data.creatorOpenId) return;
      const messageItem = {
        nickname: eventData.data.creatorNick,
        content: eventData.data.content,
      };
      addMsgItem(messageItem);
    });
    liveService.on(EventNameEnum.PaaSLivePublish, (eventData: any) => {
      const messageItem = {
        nickname: '',
        content: '直播开始',
      };
      addMsgItem(messageItem);
      setIsLiving(true);
      liveService.tryPlayLive();
    });
    liveService.on(EventNameEnum.PaaSLiveStop, (eventData: any) => {
      const messageItem = {
        nickname: '',
        content: '直播结束',
      };
      addMsgItem(messageItem);
      setIsLiving(false);
    });
  };
  useEffect(() => {
    // 1. 进入房间
    roomChannel
      .enterRoom(nickname)
      .then((roomDetail: any) => {
        // 2. 获取到roomDetail，之后获取评论列表
        setRoomDetail(roomDetail);
        return chatService.listComment(0, 1, 50);
      })
      .then((res: any) => {
        const messageItem = {
          nickname: '',
          content: nickname + '进入了房间。',
        };
        const messages: Message[] = [];
        res.commentModelList.forEach((item: any) => {
          messages.push({
            nickname: item.creatorNick,
            content: item.content,
          });
        });
        setMessageArray(messages);
        addMsgItem(messageItem);
      })
      .then(() => {
        return liveService.getLiveDetail();
      })
      .then((res: any) => {
        // 设置player
        liveService.setPlayerConfig({
          container: '#player',
          width: '100%',
          height: '100%',
          useArtc: true,
        });
        if (res.status === 1) {
          // 正在直播中
          setIsLiving(true);
          // 开启拉流播放
          liveService.tryPlayLive();
        }
        liveService.on(EventNameEnum.PaaSPlayerEvent, (data: any) => {
          if (data.eventName === 'error') console.log(data);
        });
        return chatService.getChatDetail();
      })
      .then((res: any) => {
        setLikeCount(res.likeCount || 0);
        // 绑定事件
        bindEvents();
      });
    window.onbeforeunload = () => {
      leaveRoom();
    };
    return () => {
      leaveRoom();
    };
  }, []);
  return (
    <div className={styles['room-page']}>
      <div className={styles['player-container']}>
        <div
          className={`${styles.player} ${
            !isLiving || needUserClick ? 'hidden' : ''
          }`}
          id="player"
        ></div>
      </div>
      <div className={styles['live-container']}>
        <div className={styles.top}>
          <div className={styles['room-info-container']}>
            <div className={styles['room-info']}>
              <div className={styles.avatar}></div>
              <div className={styles.info}>
                <div className={styles.title}>{roomDetail.title}</div>
                <div className={styles.data}>
                  <span>{roomDetail.onlineCount}观看</span>
                  <span>{likeCount}点赞</span>
                </div>
              </div>
            </div>
            <div className={styles.notice} onClick={noticeClickHandler}>
              <span>公告</span>
              {showNotice && (
                <div className={styles['notice-content']}>
                  {roomDetail.notice}
                </div>
              )}
            </div>
          </div>
          <div className={styles.close} onClick={leaveRoom}>
            X
          </div>
        </div>
        <div className={styles.interaction}>
          <div className={styles['chat-window']}>
            {state.messageArray.map((data, index) => (
              <div className={styles['chat-item']} key={index}>
                <span>{data.nickname ? data.nickname + '：' : ''}</span>
                {data.content}
              </div>
            ))}
            <div
              className={`${styles['chat-item']} ${styles['chat-item-notice']}`}
            >
              欢迎大家来到直播间！直播间内严禁出现违法违规、低俗色情、吸烟酗酒等内容，若有违规行为请及时举报。
            </div>
          </div>
          <div className={styles.operations}>
            <input
              type="text"
              className={styles['chat-input']}
              placeholder="和主播说点什么..."
              onKeyDown={sendComment}
              value={messageValue}
              onChange={(e) => setMessageValue(e.target.value)}
            />
            <div className={styles['operation-btn']} onClick={onShare}>
              <img src="https://img.alicdn.com/imgextra/i2/O1CN01NVOoJY24njpn5Zinn_!!6000000007436-55-tps-37-38.svg" />
            </div>
            <div className={styles['operation-btn']} onClick={likeClickHandler}>
              <img src="https://img.alicdn.com/imgextra/i2/O1CN01FDvTPN1IH84wjF6UD_!!6000000000867-55-tps-37-37.svg" />
              <div
                className={styles['like-anime-container']}
                ref={animeContainerEl}
              ></div>
            </div>
          </div>
        </div>
      </div>
      {!isLiving && (
        <div className={styles.nolive}>
          <img src="https://img.alicdn.com/imgextra/i1/O1CN01pgziS925R7tXtb86t_!!6000000007522-55-tps-238-127.svg" />
          <span>主播正在路上，请稍等～</span>
        </div>
      )}
      {needUserClick && isLiving && (
        <button className={styles['need-click-btn']} onClick={userStartPlay}>
          开始观看
        </button>
      )}
    </div>
  );
}
