export const PRIVILEGE_GROUP = 'PRIVILEGE_GROUP'

export default {
  state: {
    privilegeGroupCache: []
  },
  mutations: {
    [PRIVILEGE_GROUP](state, data) {
      state.privilegeGroupCache = data.slice()
    }
  },
  actions: {
    [PRIVILEGE_GROUP]({commit}, data) {
      commit(PRIVILEGE_GROUP, data)
    }
  }
}
