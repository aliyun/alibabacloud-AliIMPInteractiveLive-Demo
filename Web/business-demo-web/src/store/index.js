import Vue from 'vue'
import Vuex from 'vuex'
import User from './user'
import Table from './tableActions'
import Cache from './cache'
import Bread from './bread'

Vue.use(Vuex)

const store = new Vuex.Store({
  modules: {
    User,
    Table,
    Cache,
    Bread
  }
})

export default store
