import { defineConfig } from 'umi';
import fs from 'fs';
import path from 'path';

const packagejson = fs.readFileSync(path.resolve('./package.json'));
const json = JSON.parse(packagejson.toString());

export default defineConfig({
  title: '互动课堂DEMO',
  favicon: '/favicon.ico',
  base: process.env.NODE_ENV === 'production' ? '/page/standard_class/' : '/',
  publicPath: process.env.NODE_ENV === 'production' ? '/standard_class/' : '/',
  history: { type: 'hash' },
  externals: {
    react: 'window.React',
    'react-dom': 'window.ReactDOM',
  },
  nodeModulesTransform: {
    type: 'none',
  },
  routes: [
    {
      path: '/',
      component: '@/layouts/index',
      title: '互动课堂DEMO',
      routes: [
        {
          path: '/',
          component: '@/pages/index',
          title: '登录中',
        },
        {
          path: '/login',
          component: '@/pages/login',
          title: '互动课堂DEMO',
        },
        {
          path: '/teacher',
          component: '@/pages/teacher',
          title: '互动课堂DEMO-教师端',
        },
        {
          path: '/student',
          component: '@/pages/student',
          title: '互动课堂DEMO-学生端',
        },
      ],
    },
  ],
  fastRefresh: {},
  devServer: {
    port: 443,
    https: true,
    proxy: {
      '/api': {
        target: 'https://***.***.com/',
        secure: false,
        changeOrigin: true,
      },
    },
  },
});
