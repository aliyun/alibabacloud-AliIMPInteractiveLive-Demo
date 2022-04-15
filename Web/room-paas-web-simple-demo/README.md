# 低代码音视频工厂标准接入简单demo

### 开始运行
1. 进入相应的项目目录
2. 复制目录内的`config.js.default`到`config.js`文件：`cp config.js.default config.js`
3. 修改`config.js`文件的配置信息，填写您的appId、appKey和您自己服务端的地址origin
4. 修改`index.js`文件，按照主播端的数据，设置已存在的`roomId`和任意`userId`，修改您服务上获取token的api的`path`
5. 执行`npm install`后就可以`npm start`，在浏览器中打开`127.0.0.1:9999` 查看页面

### js-sdk-demo-withchat&live 使用方法
> 此demo为搭载**chatService及liveService**的简单示例

操作方法如下：
1. 您需要首先点击登录按钮进行登录操作
2. 事件监听为可选操作，您可根据需求选择是否需要或者需要监听哪些部分
3. 在进行其他操作前您需要获取房间频道，即创建`roomchannel` 的实例，并在此注册需要的服务
4. 完成后即可进入房间场景，点击**进入房间**
5. 创建播放器即为`liveService`的简单展示，在这里您可以拉取到直播画面
6. 评论与点赞等是`chatService`的功能，您也可以进行简单尝试
7. 最后离开时需要点击离开房间，当然由于`beforeunload`的存在，直接关闭页面也将会调用离开房间

您可根据需求对模块内功能进行扩展与优化，本demo仅作开发引导作用。


### js-sdk-demo-withrtc 使用方法
> 此demo为搭载**rtcService**的简单示例

操作方法如下：
1. 您需要首先点击登录按钮进行登录操作
2. 事件监听为可选操作，您可根据需求选择是否需要或者需要监听哪些部分
3. 在进行其他操作前您需要获取房间频道，即创建`roomchannel` 的实例，并在此注册需要的服务
4. 完成后即可进入房间场景，点击**进入房间**
5. 如果您为该房间的**owner**，您可选择加入`channel`开始旁路推流等
6. 您可开启预览打开摄像头预览，也可以关闭摄像头预览(`initrtc`之后即可进行此操作，在**demo**中完成获取房间频道时会自动`initrtc`)，此操作仅影响本地页面展示图像与否，不会影响您的远端推流图像
7. 若您需要操作远端推流的图像及音频，可选择开启/关闭摄像头、开启音频/静音操作
8. 若您需要进行连麦操作，请保证已经开启了监听连麦事件，代码中设置为自动接收连麦，您在开发时可更改逻辑实现自己需求
9. 如果您是房间的**owner**(即已经在`channel`中)，请选用邀请连麦实现连麦操作，目前demo仅支持两人连麦，您可根据需求自行扩展
10. 若您以访客身份进入房间(即您不是房间的**owner**)，请勿使用`joinchannel`，访客权限仅限于被邀请连麦及**申请连麦**，请确保您已经开启了监听再进行连麦操作
11. 访客完成连麦后同样可以进行音视频的控制等操作
12. 最后离开时需要点击离开房间，当然由于`beforeunload`的存在，直接关闭页面也将会调用离开房间

测试用例：当您需要测试连麦功能时，可以采用url设置userId实现多用户进入房间的效果，即在url后面添加`'?userId=***'`，此时userId将被设置为您在url中传入的数据。

### 关于时序图
时序图使用PlantUML绘制，若您需要在文件内观看，请安装相应插件运行

若您使用vscode，可直接在插件库中安装PlantUML(要求具备java运行环境，请确保您已经安装java)，使用`alt+d`(mac系统为`option+d`)打开预览。