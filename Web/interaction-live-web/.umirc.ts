import { defineConfig } from 'umi';
import fs from 'fs';
import path from 'path';

const packagejson = fs.readFileSync(path.resolve('./package.json'));
const json = JSON.parse(packagejson.toString());

export default defineConfig({
  history: {
    type: 'hash',
  },
  publicPath:
    process.env.NODE_ENV === 'production'
      ? `/room-paas/${json.name}/${json.version}/`
      : '/',
  nodeModulesTransform: {
    type: 'none',
  },
  favicon:
    process.env.NODE_ENV === 'production'
      ? `/room-paas/${json.name}/${json.version}/favicon.ico`
      : '/favicon.ico',
  routes: [
    {
      exact: true,
      path: '/doLogin',
      component: '@/pages/common/doLogin',
      title: '互动直播DEMO',
    },
    {
      path: '/m',
      component: '@/layouts/mobile/index',
      routes: [
        {
          exact: true,
          path: '/m/room',
          component: '@/pages/room/mobile',
          title: '直播间',
          wrappers: ['@/wrapper/loginWrapper', '@/wrapper/uaWrapper'],
        },
      ],
    },
    {
      path: '/',
      component: '@/layouts/pc/index',
      routes: [
        {
          path: '/',
          component: '@/pages/login',
          title: '互动直播DEMO',
        },
        {
          path: '/room',
          component: '@/pages/room/pc',
          title: '直播间',
          wrappers: ['@/wrapper/loginWrapper', '@/wrapper/uaWrapper'],
        },
        {
          path: '/roomList',
          component: '@/pages/roomList',
          title: '房间列表',
          wrappers: ['@/wrapper/loginWrapper'],
        },
      ],
    },
  ],
  fastRefresh: {},
  devServer: {
    contentBase: path.join(__dirname, 'public'),
  },
});
