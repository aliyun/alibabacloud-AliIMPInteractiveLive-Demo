import { useState, useEffect } from 'react';
import { history } from 'umi';
import { Form, Select, Input, Button, Radio, message } from 'antd';
import { doCreateRoom, doLogin, getRoomEngine } from '@/biz/doLogin';
import './index.less';

const { Option } = Select;
function IndexPage() {
  const [role, setRole] = useState(0);
  const [classType, setClassType] = useState(0);
  const [roomId, setRoomId] = useState('');
  const [userId, setUserId] = useState('');
  const roleOptions = [
    {
      label: '老师',
      value: 0,
    },
    {
      label: '学生',
      value: 1,
    },
  ];
  const classOptions = [
    {
      label: '低延时大班课',
      value: 0,
    },
    {
      label: '互动小课堂',
      value: 1,
    },
  ];
  const doCreate = (): Promise<void> => {
    return new Promise((resolve, reject) => {
      doCreateRoom(userId, roomId)
        .then((res: any) => {
          if (res) {
            resolve();
          } else {
            throw '';
          }
        })
        .catch(() => {
          window.roomEngine
            .getRoomDetail(roomId)
            .then((res: any) => {
              if (res.ownerId === userId) {
                resolve();
                return;
              }
              reject('教室号已存在！');
            })
            .catch((err: any) => {
              console.log(err);
              reject('数据请求错误');
            });
        });
    });
  };
  const handleEnterRoom = () => {
    // todo: loading
    window.sessionStorage.setItem('userId', userId);
    getRoomEngine(userId)
      .then(() => {
        if (role === 0) {
          doCreate()
            .then(() => {
              return doLogin(userId, roomId);
            })
            .then(() => {
              history.push(`/teacher?roomId=${roomId}&userId=${userId}`);
              return;
            })
            .catch((err: any) => {
              window.roomEngine.logout();
              window.sessionStorage.removeItem('userId');
              const reason =
                (err && err.body && err.body.reason) ||
                (typeof err === 'string' && err) ||
                '';
              message.error(`登录失败：${reason || '未知错误'}`);
            });
        } else {
          window.roomEngine
            .getRoomDetail(roomId)
            .then((res: any) => {
              if (res.ownerId === userId) {
                message.info('检测到您是老师，请以老师身份进入房间');
                return;
              }
              doLogin(userId, roomId).then(() => {
                history.push(`/student?roomId=${roomId}&userId=${userId}`);
              });
            })
            .catch((err: any) => {
              console.log(err);
              message.error('教室不存在，请检查输入！');
            });
        }
      })
      .catch((err) => {
        console.log(err);
      });
  };
  return (
    <div className="login">
      <div className="login-container">
        <div className="login-title">
          <h1>互动课堂DEMO</h1>
        </div>
        <Form className="login-form">
          <Form.Item
            className="input-item"
            name="roomId"
            rules={[
              { required: true, message: '请输入教室号' },
              { pattern: /^(\d){6}$/, message: '只能输入六位数字' },
            ]}
          >
            <Input
              prefix={
                <svg className="icon site-form-item-icon" aria-hidden="true">
                  <use xlinkHref="#icon-jiaoshihao1"></use>
                </svg>
              }
              value={roomId}
              placeholder="请输入您的教室号"
              onChange={(data) => {
                setRoomId(data.target.value);
              }}
            />
          </Form.Item>
          <Form.Item
            className="input-item"
            name="userId"
            rules={[
              { required: true, message: '请输入用户名' },
              { pattern: /^(\w)+$/, message: '只能输入字母或数字' },
            ]}
          >
            <Input
              prefix={
                <svg className="icon site-form-item-icon" aria-hidden="true">
                  <use xlinkHref="#icon-yonghuming1"></use>
                </svg>
              }
              value={userId}
              placeholder="请输入您的用户名"
              onChange={(data) => {
                setUserId(data.target.value);
              }}
            />
          </Form.Item>
          <Form.Item className="input-item">
            <Select
              defaultValue={0}
              size="large"
              onChange={(val) => setClassType(val)}
              value={classType}
            >
              {classOptions.map((data) => {
                return (
                  <Option value={data.value} key={data.value}>
                    {data.label}
                  </Option>
                );
              })}
            </Select>
          </Form.Item>
          <Form.Item className="input-item">
            <Radio.Group
              options={roleOptions}
              onChange={(val) => setRole(val.target.value)}
              value={role}
            />
          </Form.Item>
          <Button type="primary" onClick={handleEnterRoom}>
            {'进入教室'}
          </Button>
        </Form>
      </div>
    </div>
  );
}
export default IndexPage;
