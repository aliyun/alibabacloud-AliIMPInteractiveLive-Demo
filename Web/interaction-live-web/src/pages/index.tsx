import { useState, useEffect } from 'react';
import { useHistory } from 'react-router-dom';
import roomEngineConfig from '../constants/config';
import { message } from 'antd';
import styles from './index.less';

// 0. ts需要定义Window上的全局变量
declare global {
  interface Window {
    RoomPaasSdk: any;
    roomEngine: any;
    roomChannel: any;
  }
}

// 1. 获取engine
const { RoomEngine } = window.RoomPaasSdk;

export default function IndexPage() {
  const history = useHistory();
  const [nickname, setNickname] = useState('');
  // 2. 获取engine实例
  const roomEngineInstance = RoomEngine.getInstance();
  window.roomEngine = roomEngineInstance;
  // 3. 配置登录用的config
  const { appKey, appId, debug } = roomEngineConfig;
  const init = (userId: string) => {
    const deviceId = encodeURIComponent(roomEngineInstance.getDeviceId()); // 获取deviceId
    const origin = 'https://server.xxx.com/'; // 自己的server地址，配跨域，也可以代理
    const config = {
      // login需要的config
      appKey,
      appId,
      debug,
      deviceId,
      authTokenCallback: async () => {
        // 返回promise的函数，获取authToken用
        const path = 'api/login/getToken';
        return fetch(
          `${origin}/${path}?appKey=${appKey}&appUid=${userId}&deviceId=${deviceId}&domain=${appId}`,
        )
          .then((res) => res.json())
          .then((res) => {
            if (res) {
              const authToken = res.result;
              return { ...authToken, uid: userId };
            }
            throw new Error('没有获取到Token');
          })
          .catch((err) => {
            console.error(err);
          });
      },
    };
    // 4. 初始化engine的config
    roomEngineInstance.init(config);
  };
  const doLogin = async () => {
    if (!nickname) {
      message.error('请输入昵称');
      return;
    }
    try {
      // 5. 获取nickname和userId
      // demo通过昵称判断是否登录过，信息存在localStorage中，没有的话就随机个数字当userId
      const savedUserList = window.localStorage.getItem('userList');
      const userList = savedUserList ? JSON.parse(savedUserList) : {};
      const userId =
        userList[nickname] || Math.floor(Math.random() * 1000000).toString();
      window.localStorage.setItem(
        'userList',
        JSON.stringify(
          Object.assign(userList, {
            [nickname]: userId,
          }),
        ),
      );
      window.localStorage.setItem('nickname', nickname);
      // 6. 调用初始化方法
      init(userId);
      // 7. 登录
      await roomEngineInstance.auth(userId);
      message.success('登录成功');
      history.push('/roomList');
    } catch (err) {
      console.error(err);
      message.error('登录失败');
    }
  };
  useEffect(() => {
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
        <div className={styles['login-btn']} onClick={doLogin}>
          登录
        </div>
      </div>
    </div>
  );
}
