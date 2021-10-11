import Chatarea from './Chatarea';
import { useState } from 'react';
import './chatlist.less';
import Studentlist from './Studentlist/index';

export default function Chatlist(props: any) {
  const [studentCount, setStudentCount] = useState(0);
  const [tabbarState, setTabbarState] = useState(0);
  const [newMessage, setNewMessage] = useState(false);
  return (
    <div className="chatlist">
      <div className="chatlist-tabbar">
        <div
          className={`chatlist-tabbar-item ${
            tabbarState === 0 ? 'active' : ''
          }`}
          onClick={() => setTabbarState(0)}
        >
          学员 ({studentCount})
        </div>
        <div
          className={`chatlist-tabbar-item ${
            tabbarState === 1 ? 'active' : ''
          }`}
          onClick={() => {
            setNewMessage(false);
            setTabbarState(1);
          }}
        >
          <span
            className={`chatlist-tabbar-item-talk ${
              newMessage ? 'chatlist-tabbar-item-newmessage' : ''
            }`}
          >
            讨论
          </span>
        </div>
      </div>
      <Studentlist
        setStudentCount={setStudentCount}
        isPlaying={props.isPlaying}
        userId={props.userId}
        tabbarState={tabbarState}
        role={props.role}
      />
      <Chatarea
        catchCommit={props.catchCommit}
        userId={props.userId}
        tabbarState={tabbarState}
        onSend={props.onSend}
        role={props.role}
        setNewMessage={setNewMessage}
      />
    </div>
  );
}
