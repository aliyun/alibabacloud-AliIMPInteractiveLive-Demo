import Vue from 'vue'
import Router from 'vue-router'

const Frame = () => import('@/components/frames/frame')
// const Layout = () => import('@/components/frames/layout')

const Login = () => import('@/views/common/login')
const Share = () => import('@/views/common/share')
const ErrorPage = () => import('@/views/common/404')

const LiveList = () => import('@/views/live/liveList')
const LiveDetail = () => import('@/views/live/liveDetail')

const EnterpriseLiveList = () => import('@/views/enterprise/liveList')
const EnterpriseLiveDetail = () => import('@/views/enterprise/liveDetail')

const SelectScene = () => import('@/views/selectScene')

Vue.use(Router)

export const routerMap = [{
  path: '/live',
  name: 'frame',
  component: Frame,
  children: [
    {
      path: '/live/liveList',
      name: 'liveList',
      component: LiveList,
      meta: {
        auth: true,
        title: '直播列表',
        icon: 'el-icon-setting',
        scene: '直播-通用'
      }
    }, {
      path: '/live/liveDetail/:liveId',
      name: 'liveDetail',
      component: LiveDetail,
      meta: {
        auth: true,
        title: '直播详情',
        notMenu: true,
        scene: '直播-通用'
      }
    }
  ],
  redirect: '/live/liveList'
}, {
  path: '/enterprise',
  name: 'frame',
  component: Frame,
  children: [
    {
      path: '/enterprise/liveList',
      name: 'EliveList',
      component: EnterpriseLiveList,
      meta: {
        auth: true,
        title: '直播列表',
        icon: 'el-icon-setting',
        scene: '企业直播'
      }
    }, {
      path: '/enterprise/liveDetail/:liveId',
      name: 'EliveDetail',
      component: EnterpriseLiveDetail,
      meta: {
        auth: true,
        title: '直播详情',
        notMenu: true,
        scene: '企业直播'
      }
    }
  ],
  redirect: '/enterprise/liveList'
}, {
  path: '/',
  name: 'frame',
  component: Frame,
  children: [
    {
      path: '/selectScene',
      name: 'selectScene',
      component: SelectScene,
      meta: {
        auth: true,
        title: '场景选择',
        notMenu: true,
        hideMenu: true
      }
    },
  ],
  redirect: '/selectScene'
}]

const router = new Router({
  mode: 'hash',
  linkActiveClass: 'is-active',
  routes: [{
    path: '/login',
    name: 'login',
    component: Login
  },
  {
    path: '/share',
    name: 'share',
    component: Share,
    meta: {
      title: '阿里云低代码音视频工厂'
    }
  },
  {
    path: '*',
    name: 'error',
    component: ErrorPage
  }].concat(routerMap)
})

export default router
