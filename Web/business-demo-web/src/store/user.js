import Vue from 'vue'

export const USER_SIGNIN = 'USER_SIGNIN'
export const USER_SIGNOUT = 'USER_SIGNOUT'

export default {
  state: JSON.parse(localStorage.getItem('user')) || {},
  getters: {
    userNick: state => {
      return state.userNick || '游客'
    },
    userId: state => {
      return state.userId || '0'
    },
    menus: state => {
      return state.menus || {}
    }
  },
  mutations: {
    [USER_SIGNIN](state, user) {
      localStorage.setItem('user', JSON.stringify(user))
      Object.assign(state, user)
    },
    [USER_SIGNOUT](state) {
      localStorage.removeItem('user')
      Object.keys(state).forEach(k => Vue.delete(state, k))
    }
  },
  actions: {
    [USER_SIGNIN]({commit}, user) {
      commit(USER_SIGNIN, user)
    },
    [USER_SIGNOUT]({commit}) {
      commit(USER_SIGNOUT)
    }
  }
}
