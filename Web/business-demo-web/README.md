# 低代码音视频工厂直播管理后台demo

## 开始

* 安装依赖
  ```bash
  $ npm install
  ```
* 开始
  ```bash
  $ npm start
  ```
* 浏览器中打开`localhost:8000`即可查看。

## 自定义服务端代理

进入`vue.config.js`文件，将`proxy`中的`target`改为要代理的地址即可。
> 注：此处设置了`pathRewrite`，会将前缀为`/api`的url全部替换掉，需要修改的话，可以修改该配置，也可以到`src/api/axios.js`中修改默认的前缀。
>进入`main.js`文件注释mock的引入，进入`src/api/axios.js`中取消baseURL的注释


## 其他说明

* 此项目的目的为展示如何通过openApi实现直播管理后台的业务逻辑。
* 此项目将appId放在前端，目的是为了方便开发调试，**在真实项目中请不要如此，应该由服务端存储此数据并加密**。

