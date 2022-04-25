import { useState, useEffect } from 'react';
import { useHistory } from 'react-router-dom';
import { message } from 'antd';
import styles from './index.less';
import { doLogin } from '../../biz/doLogin';
// 0. ts需要定义Window上的全局变量
declare global {
  interface Window {
    RoomPaasSdk: any;
    roomEngine: any;
    roomChannel: any;
    chatService: any;
    liveService: any;
  }
}

export default function IndexPage() {
  const history = useHistory();
  const [nickname, setNickname] = useState('');

  const loginClickHandler = async () => {
    if (!nickname) {
      message.error('请输入昵称');
      return;
    }
    try {
      await doLogin(nickname);
      message.success('登录成功');
      history.push('/roomList');
    } catch (err) {
      message.error('登录失败');
    }
  };
  const logout = () => {
    // 进入login页面时删除保存的token
    window.localStorage.removeItem('token');
    window.localStorage.removeItem('authTimeStamp');
    // 如果在登录状态
    window.roomEngine && window.roomEngine.logout();
  };

  useEffect(() => {
    logout();
    const savedNickname = window.localStorage.getItem('nickname');
    if (savedNickname) setNickname(savedNickname);
  }, []);
  return (
    <div className={styles['login-page']}>
      <div className={styles['login-form']}>
        <div className={styles['login-input']}>
          <label htmlFor="login-input">昵称</label>
          <input
            type="text"
            id="login-input"
            placeholder="请输入您的昵称..."
            value={nickname}
            onChange={(e) => setNickname(e.target.value)}
          />
        </div>
        <div className={styles['login-btn']} onClick={loginClickHandler}>
          登录
        </div>
      </div>
    </div>
  );
}
