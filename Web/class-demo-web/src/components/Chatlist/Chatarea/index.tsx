import { Input, Button, message, Modal } from 'antd';
import { useState, useEffect, useRef, useReducer } from 'react';
import { throttle } from '@/utils/utils';
import update from 'immutability-helper';
import './chatarea.less';

const { EventNameEnum } = window.RoomPaasSdk;
const { confirm } = Modal;
let tabbarState = 0;

interface MessageStore {
  messageArray: MessageItem[];
}
interface MessageItem {
  nick: string;
  content: string;
  userId: string;
}
interface MessageAction {
  type: string;
  payload: any;
}
const initialState: MessageStore = {
  messageArray: [],
};
const actionTypes = {
  addMsg: 'addMsg',
};
const messageReducer = (
  state: MessageStore,
  action: MessageAction,
): MessageStore => {
  switch (action.type) {
    case actionTypes.addMsg:
      return Array.isArray(action.payload)
        ? update(state, {
            messageArray: { $unshift: action.payload },
          })
        : update(state, {
            messageArray: { $push: [action.payload] },
          });
    default:
      return state;
  }
};
export default function Chatarea(props: any) {
  tabbarState = props.tabbarState;
  const [state, dispatch] = useReducer(messageReducer, initialState);
  const scrollRef = useRef<HTMLDivElement>(null);
  const [inputMessage, setInputMessage] = useState('');
  const [nowPage, setNowPage] = useState(1);
  const [hasMore, setHasMore] = useState(true);
  const [commentState, setCommentState] = useState(false);
  const userId = props.userId || '';
  const addMsgItem = (messageItem: MessageItem) => {
    dispatch({
      type: actionTypes.addMsg,
      payload: messageItem,
    });
  };
  const setMessageArr = (msgs: MessageItem[]) => {
    dispatch({
      type: actionTypes.addMsg,
      payload: msgs,
    });
  };
  const bindEvent = () => {
    window.chatService.getChatDetail().then((res: any) => {
      setCommentState(res.mute);
    });
    window.chatService.on(EventNameEnum.PaaSChatReciveComment, (d: any) => {
      if (d.data.creatorNick === userId) return;
      const messageItem = {
        nick: d.data.creatorNick,
        content: d.data.content,
        userId: d.data.creatorNick,
      };
      if (tabbarState !== 1) {
        props.setNewMessage(true);
      }
      addMsgItem(messageItem);
    });
    window.chatService.on(EventNameEnum.PaaSChatMuteAll, (d: any) => {
      setCommentState(d.data.mute);
    });
  };
  const getCommit = () => {
    props.catchCommit(nowPage).then((data: any) => {
      const addMessage: MessageItem[] = [];
      setHasMore(data.hasMore);
      data.commentModelList.forEach((item: any) => {
        const messageItem = {
          nick: item.creatorNick,
          content: item.content,
          userId: item.creatorNick,
        };
        addMessage.push(messageItem);
      });
      addMessage.reverse();
      setMessageArr([...addMessage]);
      setNowPage(nowPage + 1);
    });
  };
  const handleBanToPost = () => {
    if (!window.roomChannel) return;
    if (!commentState) {
      confirm({
        title: '是否开启全员禁言？',
        cancelText: '取消',
        okText: '确认',
        onOk() {
          window.chatService
            .banAllComment()
            .then(() => {
              setCommentState(true);
              message.info('全员禁言已开启');
            })
            .catch((err: any) => {
              console.log(err);
            });
        },
        onCancel() {
          return;
        },
      });
    } else {
      window.chatService
        .cancelBanAllComment()
        .then(() => {
          setCommentState(false);
          message.info('全员禁言已关闭');
        })
        .catch((err: any) => {
          console.log(err);
        });
    }
  };
  const scrollDown = () => {
    if (!scrollRef.current) return;
    scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
  };
  const scrollEvent = () => {
    // if (!hasMore) {
    //   message.warning('已加载全部');
    //   return;
    // }
    if (!hasMore) return;
    if (scrollRef.current && scrollRef.current.scrollTop > 0) return;
    throttle(() => {
      getCommit();
    }, 500);
  };
  const onSend = async () => {
    // 发送弹幕
    if (commentState && props.role === 'student') return;
    if (!inputMessage) return;
    await props
      .onSend(inputMessage)
      .then(() => {
        const messageItem = {
          nick: userId,
          content: inputMessage,
          userId: userId,
        };
        addMsgItem(messageItem);
        setTimeout(() => {
          scrollDown();
        }, 0);
        setInputMessage('');
      })
      .catch((err: any) => {
        console.log(err);
        message.error(`发送失败`);
      });
  };
  useEffect(() => {
    if (!window.roomChannel) return;
    getCommit();
    bindEvent();
  }, []);
  return (
    <div
      className={`chatarea ${props.tabbarState === 1 ? 'chatarea-active' : ''}`}
    >
      <div
        className={`chatarea-body ${
          commentState && props.role === 'student' ? 'chatarea-body-mute' : ''
        }`}
        ref={scrollRef}
        onScroll={() => {
          scrollEvent();
        }}
      >
        {state.messageArray.length > 0 ? (
          state.messageArray.map((data: any, index: any) => {
            return (
              <div key={index} className="chat">
                <span
                  className={
                    userId === data.userId ? 'chat-owner' : 'chat-default'
                  }
                >
                  {userId === data.userId ? data.nick + '（我）' : data.nick}:{' '}
                </span>
                <span>{data.content}</span>
              </div>
            );
          })
        ) : (
          <div className="chatarea-body-none"></div>
        )}
      </div>
      {props.role === 'teacher' ? (
        <div
          className="chatarea-bantopost-btn"
          onClick={() => {
            handleBanToPost();
          }}
        >
          <svg
            className={`icon chatatea-bantopost-btn-class ${
              commentState ? 'chatarea-bantopost-btn-active' : ''
            }`}
            aria-hidden="true"
          >
            <use xlinkHref="#icon-quanyuanjinyan1"></use>
          </svg>
        </div>
      ) : (
        ''
      )}
      {props.role === 'student' && commentState ? (
        <div className="chatarea-mute-comment">
          <svg className="icon chatarea-mute-comment-icon" aria-hidden="true">
            <use xlinkHref="#icon-tixing"></use>
          </svg>
          已开启全员禁言
        </div>
      ) : (
        ''
      )}
      <div className="chatarea-submit">
        <Input
          placeholder={`${
            commentState && props.role === 'student'
              ? '全员禁言中'
              : '请在此输入文字'
          }...`}
          value={inputMessage}
          disabled={commentState && props.role === 'student' ? true : false}
          onChange={(e) => {
            setInputMessage(e.target.value);
          }}
          onPressEnter={onSend}
        />
        {/* <div className="chatarea-submit-emoji">
          <svg className="icon" aria-hidden="true">
            <use xlinkHref="#icon-biaoqing"></use>
          </svg>
        </div> */}
        <Button
          type="primary"
          disabled={commentState && props.role === 'student' ? true : false}
          onClick={() => {
            onSend();
          }}
        >
          发送
        </Button>
      </div>
    </div>
  );
}
