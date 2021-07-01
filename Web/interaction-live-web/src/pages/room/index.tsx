import { useRef, useState, useEffect, useMemo, useReducer } from 'react';
import { IRouteComponentProps, Router } from 'umi';
import { useHistory } from 'react-router-dom';
import { createDom } from '@/utils';
import { message } from 'antd';
import update from 'immutability-helper';
import styles from './index.less';

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
  const roomId = props.location.query.roomId;
  const nickname = window.localStorage.getItem('nickname');
  const animeContainerEl = useRef(null);
  const [state, dispatch] = useReducer(reducer, initialState);
  const [likeCount, setLikeCount] = useState(0);
  const [showNotice, setShowNotice] = useState(false);
  const [roomDetail, setRoomDetail] = useState<any>({});
  const [isLiving, setIsLiving] = useState(false);
  const [messageValue, setMessageValue] = useState('');
  // 获取channel实例
  const roomChannel = useMemo(() => {
    if (!window.roomEngine) {
      props.history.push('/');
      window.location.reload();
    }
    return window.roomEngine.getRoomChannel(roomId);
  }, [roomId]);
  const liveService = roomChannel.getPluginService('live');
  const chatService = roomChannel.getPluginService('chat');
  const likeClickHandler = () => {
    if (!animeContainerEl || !animeContainerEl.current) return;
    const bubble = createDom('div', {
      class: `bubble anime`,
      id: `bubble-${likeBubbleCount}`,
    });
    let nowCount = likeBubbleCount;
    likeBubbleCount++;
    animeContainerEl.current.append(bubble);
    setTimeout(() => {
      if (!animeContainerEl.current) return;
      animeContainerEl.current.removeChild(
        document.querySelector(`#bubble-${nowCount}`),
      );
    }, 1500);
    setLikeCount(likeCount + 1);
    chatService.sendLike(1);
  };
  const noticeClickHandler = () => {
    setShowNotice(!showNotice);
  };
  const sendComment = async (e: any) => {
    if (e.keyCode !== 13) return;
    if (!messageValue) return;
    try {
      await chatService.sendComment(messageValue);
      setMessageValue('');
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
    props.history.push('/roomList');
  };
  const createPlayer = () => {
    // 创建播放器
    // 设置player
    liveService.setPlayerConfig({
      container: '#player',
    });
    // 开启拉流播放
    liveService.tryPlayLive();
  };
  const addMsgItem = (messageItem: Message) => {
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
  const bindEvents = () => {
    roomChannel.on(EventNameEnum.PaaSRoomEnter, (eventData: any) => {
      const messageItem = {
        nickname: '',
        content:
          eventData.data.nick +
          `${eventData.data.enter ? '进入了房间。' : '离开了房间。'}`,
      };
      addMsgItem(messageItem);
    });
    chatService.on(EventNameEnum.PaaSChatReciveLike, (eventData: any) => {
      const { likeCount = 0 } = eventData.data || {};
      setLikeCount(likeCount);
    });
    chatService.on(EventNameEnum.PaaSChatReciveComment, (eventData: any) => {
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
      createPlayer();
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
        // 3. 绑定事件
        bindEvents();
        const messages: Message[] = [];
        res.commentModelList.forEach((item: any) => {
          messages.push({
            nickname: item.creatorNick,
            content: item.content,
          });
        });
        setMessageArray(messages);
      })
      .then(() => {
        return liveService.getLiveDetail();
      })
      .then((res: any) => {
        if (res.status === 1) {
          setIsLiving(true);
          createPlayer();
        }
      });
  }, []);
  return (
    <div className={styles['room-page']}>
      <div className={styles['player-container']}>
        <div
          className={`${styles.player} ${!isLiving ? 'hidden' : ''}`}
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
                  <span>{roomDetail.uv}观看</span>
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
    </div>
  );
}
