import Vue from 'vue'
import router from './routerMap.js'
import store from '@/store'

const setTitle = (route) => {
  document.title = route.meta.title ? route.meta.title : 'element-spa2'
}

router.beforeEach((to, from, next) => {
  let auth = to.meta.auth
  let isLogin = Boolean(store.state.User.userNick)

  if (auth && !isLogin) {
    Vue.prototype.$message.info('尚未登录')
    return next({ name: 'login' })
  }

  if (!to.meta.noBread) store.dispatch('BREAD', to.matched)
  next()
})

router.afterEach(route => {
  setTitle(route)
})

export default router
