export const ROW_SELECT = 'ROW_SELECT'

export default {
  state: {
    rowAction: []
  },
  mutations: {
    [ROW_SELECT](state, data) {
      state.rowAction = data.slice()
    }
  },
  actions: {
    [ROW_SELECT]({commit}, data) {
      commit(ROW_SELECT, data)
    }
  }
}
