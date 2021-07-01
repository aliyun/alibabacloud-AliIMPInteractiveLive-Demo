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
  routes: [
    {
      path: '/',
      component: '@/layouts/index',
      routes: [
        {
          exact: true,
          path: '/',
          component: '@/pages/index',
          title: '阿里云互动直播',
        },
        {
          exact: true,
          path: '/room',
          component: '@/pages/room',
          title: '直播间',
        },
        {
          exact: true,
          path: '/roomList',
          component: '@/pages/roomList',
          title: '房间列表',
        },
      ],
    },
  ],
  fastRefresh: {},
});
