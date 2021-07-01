import { useState, useEffect } from 'react';
import { useHistory } from 'react-router-dom';
import { message } from 'antd';
import roomEngineConfig from '../../constants/config';
import styles from './index.less';

export default function IndexPage() {
  const history = useHistory();
  const [roomList, setRoomList] = useState([]);
  const { appId } = roomEngineConfig;
  const enterRoom = (roomId: string) => {
    history.push(`/room?roomId=${roomId}`);
  };
  useEffect(() => {
    if (!window.roomEngine) {
      history.replace(`/`);
      message.info('登录状态失效，请重新登录');
      return;
    }
    window.roomEngine
      .getRoomList(appId, 'business', 1, 50)
      .then((res: any) => setRoomList(res.roomInfoList))
      .catch((err: any) => {
        console.error(err);
        message.error('获取房间列表失败');
      });
  }, []);
  return (
    <div className={styles['room-list-page']}>
      <div className={styles['room-list']}>
        {roomList.map((item: any) => (
          <div
            className={styles['list-item']}
            key={item.roomId}
            onClick={() => enterRoom(item.roomId)}
          >
            <div className={styles['item-inner']}>
              <div className={styles.online}>0人在线</div>
              <div className={styles['info-container']}>
                <div className={styles.avatar}></div>
                <div className={styles.info}>
                  <div className={styles.title}>{item.title}</div>
                  <div className={styles.id}>ID：{item.ownerId}</div>
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
