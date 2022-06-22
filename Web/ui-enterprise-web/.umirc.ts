import { defineConfig } from 'umi';

export default defineConfig({
  nodeModulesTransform: {
    type: 'none',
  },
  history: { type: 'hash' },
  publicPath: '/',
  routes: [
    {
      path: '/',
      component: '@/layouts/index',
      title: '企业直播Demo',
      routes: [
        {
          path: '/',
          component: '@/pages/room/index',
          title: '企业直播Demo',
        },
      ],
    },
  ],
  fastRefresh: {},
  devServer: {
    proxy: {
      '/api': {
        target: 'https://example.com/',
        secure: false,
        changeOrigin: true,
      },
    },
  },
});
