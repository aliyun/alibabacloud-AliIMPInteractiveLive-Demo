import { Effect, ImmerReducer, Subscription } from 'umi';

export interface Message {
  name: string;
  content: string;
  isOwner: boolean;
  isMe: boolean;
}

export interface ChatModelState {
  messages: Message[];
}

export interface ChatModelType {
  namespace: 'chat';
  state: ChatModelState;
  effects: {
    query: Effect;
  };
  reducers: {
    addMsg: ImmerReducer<ChatModelState>;
  };
  //   subscriptions: { setup: Subscription };
}

const chatGoBottom = () => {
  const chatContainer = document.getElementById('chat-container');
  const scrollHeight = chatContainer?.scrollHeight || 0;
  const clientHeight = chatContainer?.clientHeight || 0;
  chatContainer?.scrollTo(0, scrollHeight - clientHeight);
};

const ChatModel: ChatModelType = {
  namespace: 'chat',

  state: {
    messages: [],
  },

  effects: {
    *query({ payload }, props) {
      console.log(props);
    },
  },

  reducers: {
    addMsg(state, action) {
      if (Array.isArray(action.payload)) {
        state.messages = action.payload;
      } else {
        state.messages.push(action.payload);
      }
      setTimeout(() => {
        chatGoBottom();
      }, 0);
    },
  },
};

export default ChatModel;
