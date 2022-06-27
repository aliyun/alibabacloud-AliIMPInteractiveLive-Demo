# 互动直播 h5淘宝同款最佳实践 

## 初始化配置

* 修改`src/constants/config.js`，将您低代码音视频工厂控制台中的appId、appKey填入相应位置。
* origin字段填写的是客户自己的服务端地址，用于在`src/App.js`中获取WebSDK建立长连接Token。注意，此处属于直接向此域名进行请求，在浏览器中会跨域，需要服务端对接口配置跨源资源共享（CORS）。
* 如果需要本地开发服务代理跨域，修改`webpackDevServer.config.js`文件中的proxy配置项，即可在开发时通过代理进行跨域。注意，此种方法仅适用于开发环境，线上部署请使用其他方法解决跨域问题。

## 开始

* 安装依赖
  ```bash
  $ npm install
  ```
* 开始
  ```bash
  $ npm start
  ```
* 在浏览器中打开localhost:3001/?nick=***&liveId=******&userId=**#/(注：nick、liveId、userId、需要在地址中拼接传入)