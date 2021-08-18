# 运行Web Demo

通过阅读本文，您可以了解运行互动直播Demo的方法。

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

#### 1. 获取appId及相应的appKey
appId、appKey在钉钉群内联系对接人员获取。
#### 2. 下载源码并解压
在技术对接群中提供
#### 3. 修改配置文件
修改 src/constants/config.ts 中相应配置。
```typescript
export default {
  appId: 'xxxxxx', // 应用的appName
  appKey: '468e317**********a64aed8a', // 应用的appKey
  signSecret: 'h9***2' // 对称加密验签的secret，根据实际需求填写
}
```
#### 4. 运行Demo
打开终端，进入到Demo根目录，运行下列命令
```bash
$ npm install # 或 yarn
$ npm start # 或 yarn start
```
之后打开浏览器，进入http://localhost:8000/查看效果
### 5. Demo操作
项目运行后进入登录页面，输入用户昵称后点击登录进入选择房间页面；
点击房间卡片进入直播间；
进入直播间后，根据直播的状态，自动完成视频拉流，可以选择发送弹幕或者点赞等功能测试。

## Demo源码解析
#### 1. 项目结构说明
``` bash
.
├── package.json
├── src
│   ├── biz
│   │   ├── doLogin.ts               # 登录方法封装
│   │   └── getAuthStatus.ts         # 获取登录状态的方法
│   ├── constants
│   │   └── config.ts                # 配置
│   ├── global.css                   # 全局样式
│   ├── layouts                      # 布局样式
│   │   ├── index.less
│   │   └── index.tsx
│   ├── pages
│   │   ├── doLogin.tsx              # 为登录持久化做的登录页，登录态刷新页面会进入这里
│   │   ├── document.ejs             # html模板
│   │   ├── index.less
│   │   ├── index.tsx                # 入口登录页
│   │   ├── room                     # 直播间
│   │   │   ├── anime.less
│   │   │   ├── index.less
│   │   │   └── index.tsx
│   │   └── roomList                 # 房间列表
│   │       ├── index.less
│   │       └── index.tsx
│   ├── utils                        # 工具方法
│   │   ├── SignUtils.ts
│   │   └── index.ts
│   └── wrapper
│       └── loginWrapper.tsx。       # 登录判断包裹层
├── tsconfig.json
```
#### 2. 功能实现流程

请参阅[web端集成文档](https://www.yuque.com/docs/share/17d79539-228e-4b35-8572-38339e04e151)
