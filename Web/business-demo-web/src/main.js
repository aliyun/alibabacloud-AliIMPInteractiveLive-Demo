import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import filters from './libs/filters'
import { sync } from 'vuex-router-sync-previous'
import ElementUI from 'element-ui'
import VClipboard from 'clipboard-vue'
import './libs/validator'
import '@/assets/styles/reset.less'
import '@/assets/styles/frame.less'
import 'element-ui/lib/theme-chalk/index.css'
import './mock'

sync(store, router)

Vue.use(ElementUI)
Vue.use(VClipboard)

Vue.config.productionTip = false

Object.keys(filters).forEach((item) => { Vue.filter(item, filters[item]) })

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')
