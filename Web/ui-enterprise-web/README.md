# 企业直播Demo

使用[Umi框架](https://umijs.org/zh-CN/docs)开发。

## Getting Started

Install dependencies,

```bash
$ yarn
```

Start the dev server,

```bash
$ yarn start
```
1. 修改src/constants/config.js，将您低代码音视频工厂控制台中的appId、appKey填入相应位置。
2. 文件中的origin字段填写的是客户自己的服务端地址，用于在src/biz/doLogin.ts中获取WebSDK建立长连接Token。注意，此处属于直接向此域名进行请求，在浏览器中会跨域，需要服务端对接口配置跨源资源共享（CORS）。如果服务端不方便配置，可以参考第三步。
3. 如果需要本地开发服务代理跨域，可以修改根目录的.umirc.ts文件中的proxy配置项，填入自己的服务端域名，参考文档Umi配置proxy。注意，此种方法仅适用于开发环境，线上部署请使用其他方法解决跨域问题。
4. 将您创建的直播Id、用户Id和用户昵称按照Query的方式传入到页面中，例如：http://localhost:8000?liveId=****&userId=****&nick=****。
