import { defineConfig } from 'umi'
import fs from 'fs'
import path from 'path'

const packagejson = fs.readFileSync(path.resolve('./package.json'))
const json = JSON.parse(packagejson.toString())

export default defineConfig({
  nodeModulesTransform: {
    type: 'all',
  },
  history: {
    type: 'hash',
  },
  publicPath:
    process.env.NODE_ENV === 'production'
      ? `https://g.alicdn.com/room-paas/${
          json.name
        }/${json.version}/`
      : '/',
  externals: {
    react: 'window.React',
    'react-dom': 'window.ReactDOM',
  },
  routes: [
    {
      path: '/',
      routes: [
        {
          exact: true,
          path: '/login',
          component: '@/pages/login/login',
          title: '阿里云互动课堂',
        },
        {
          exact: true,
          path: '/doLogin',
          component: '@/pages/login/demoDoLogin',
          title: '阿里云互动课堂',
        },
        {
          path: '/class',
          component: '@/layouts/index',
          title: '阿里云互动课堂',
          routes: [
            {
              exact: true,
              path: '/class/teacher',
              component: '@/pages/teacher',
              title: '阿里云互动课堂-老师端',
            },
            {
              exact: true,
              path: '/class/student',
              component: '@/pages/student',
              title: '阿里云互动课堂-学生端',
            },
            {
              exact: true,
              path: '/class/recorder',
              component: '@/pages/recorder',
              title: '阿里云互动课堂',
            },
          ],
        },
      ],
    },
  ],
  dva: {
    immer: true,
    hmr: true,
  },
  fastRefresh: {},
  devServer: {
    port: 443,
    https: true,
    proxy: {
      '/api': {
        target: 'https://a.b.com/',
        secure: false,
        changeOrigin: true,
      },
    },
  },
})
