# 运行Web Demo

通过阅读本文，您可以了解运行Room-Paas-Sdk Demo的方法。

## 前提条件

开发前的浏览器环境要求如下表所示：

| 主流浏览器 | 最低版本要求 |
| -- | -- |
Chrome|4 ~ 88 +
Firefox|11 ~ 85 +
IE|10 +
Edge|12 ~ 88 +
Opera|12.1 ~ 72 +
Safari|5 ~ 13.1 +
Android|Browser	89 +
Safari for iOS|4.2 ~ 13.7 +
## Demo运行指引

#### 1. 获取appName及相应的appKey
appName、appKey在钉钉群内联系对接人员获取。
#### 2. 下载源码并解压
在技术对接群中提供
#### 3. 修改配置文件
修改 src/constants/config.ts 中相应配置。
```typescript
export default {
  appName: 'xxxxxx', // 应用的appName
  appKey: '468e317**********a64aed8a', // 应用的appKey
  wsUrl: 'wss://wss.im.dingtalk.cn', // 目前默认为此地址，如有需求，可传入其他地址覆盖
  debug: true, // 设置debug模式
  signSecret: 'h9***2' // 对称加密验签的secret，根据实际需求填写
}
```
#### 4. 运行Demo
打开终端，进入到Demo根目录，运行下列命令
```bash
$ npm install # 或 yarn
$ npm start # 或 yarn start
```
之后打开浏览器，进入http://localhost:3000/查看效果
### 5. Demo操作
项目运行后进入login页面，输入userId（openId）后点击登录进入选择房间页面；
输入房间ID及用户昵称并选择身份后进入直播页面；
进入直播间后，根据直播的状态，自动完成视频拉流，可以选择发送弹幕或者点赞等功能测试。

*注：移动端推流不支持flv、rmtp，应使用m3u8。*
## Demo源码解析
#### 1. 项目结构说明
``` bash
├── build                       #打包文件
├── public                      #静态资源
├── src                         #项目文件目录
│   ├── constants               #静态资源
│   │   ├── config.ts           #相关配置参数
│   ├── utils                   #工具类集合
│   ├── pages                   #页面
│   │   ├── login               #登录页面
│   │   │   ├── index.tsx              
│   │   │   ├── login.css              
│   │   ├── enterRoom           #进入房间页面    
│   │   │   ├── index.tsx              
│   │   │   ├── index.css              
│   │   ├── createRoom          #创建房间页面     
│   │   │   ├── index.tsx              
│   │   │   ├── index.css              
│   │   ├── livePage            #观众端直播间页面  
│   │   │   ├── index.tsx              
│   │   │   ├── index.css              
├── index.css                      		 
├── index.tsx                   #入口文件
├── setProxy.js                 #代理配置#
```
#### 2. 功能实现流程
* 获取 RoomEngine 实例
```Typescript
// 获取RoomEngine实例
const roomEngineInstance = RoomEngine.getInstance();
```
* 获取设备号deviceId
```Typescript
// 获取设备号deviceId，唯一标识当前设备
const deviceId = await roomEngineInstance.getDeviceId();
```
* 为 RoomEngine 配置参数
```Typescript
const config = {
  appKey, // 应用key，IM_PAAS分配给应用的key
  appID: appName, // app应用名称
  debug, // debug模式
  wsUrl, // 建立ws链接的域名
  deiceId, // 设备号
  authTokenCallback, // 获取登录token的异步回调函数（自定义）
}

// 获取登录token的异步回调函数（openId是指用户的userId）, 举例:
authTokenCallback: async () => {
  return fetch(
    `https://*.*.*:8080/api/login/getToken?domain=${appName}&appUid=${openId}&deviceId=${encodeURIComponent(deviceId)}&appKey=${appKey}`,
  )
    .then((res) => res.json())
    .then((res) => {
      if (res) {
        try {
          const authToken = res.result;
          return { ...authToken, uid: openId };
        } catch (err) {
          throw new Error(err);
        }
      }
      throw new Error('token is null');
    })
    .catch((err) => {
      console.error(err);
    });
	}
}
```
* 初始化 RoomEngine 的配置
```Typescript
roomEngineInstance.init(config);
```
* RoomEngine 登录，建立ws长连接及注册事件
```Typescript
// openId即用户ID，标识用户身份
roomEngineInstance.login(openId);
```
* 通过 roomId 获取/创建对应的 roomChannel 实例
```Typescript
// roomId标识房间号，方法调用结果获取当前房间roomChannel实例
const roomChannel = roomEngineInstance.getRoomChannel(roomId);
```
* 调用 enterRoom 进入房间，必须先进入房间后才能进行后续操作。
```Typescript
// 必须保证enterRoom完成后才能进行后续操作，nick为用户名
roomChannel.enterRoom(nick)
```
* 进入房间后，调用 roomChannel 的相关方法进行互动和媒体等操作。
```Typescript
const comment = '测试发送评论';
// 评论发送，传入参数为评论内容
roomChannel.sendComment(comment);

// 点赞
roomChannel.sendLike();

/*
拉取历史评论，传入参数分别为
	sortType: 排序方式,0-时间递增顺序，1-时间递减顺序；
  pageNum: 分页拉取的索引下标,第一次调用传1，后续调用+1；
  pageSize: 分页拉取的大小；
*/
roomChannel.listComment(sortType, pageNum, pageSize);
```
* 配置及创建播放器
```Typescript
// 播放器配置及创建
roomChannel.setPlayerConfig({
	container:'#J_player', // DOM容器id
})

// 尝试播放（根据直播状态 决定是否创建播放器）
roomChannel.tryPlayLive();
```
* RoomChannel 监听直播间的事件
```Typescript
// eventData 参考下方事件列表的说明
// 进入房间的事件
roomChannel.on(EventNameEnum.PaaSRoomEnter, (eventData) => {
	console.log(EventNameEnum.PaaSRoomEnter, eventData)
})

// 收到点赞，含点赞数量
roomChannel.on(EventNameEnum.PaaSChatReciveLike, (eventData) => {
	console.log(EventNameEnum.PaaSChatReciveLike, eventData)
})

// 收到评论
roomChannel.on(EventNameEnum.PaaSChatReciveComment, (eventData) => {
	console.log(EventNameEnum.PaaSChatReciveComment, eventData)
})
```
注：RoomChannel是一个extend eventEmitter的类，实例拥有.on.emit.remove方法

* 页面关闭前要离开房间
```Typescript
// 离开房间的时候，必须调用leaveRoom，如在beforeunload设置
window.addEventListener('beforeunload', () => {
	roomChannel.leaveRoom().then((res) => {
		console.log('==leaveRoom==>', res)
	})
});
```
