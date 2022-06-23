<template>
  <v-table
    :data="tableData"
    :columns="columns"
    :loading="loading"
    isBackPage
    @handleCurrentChange="handleCurrentChange"
    @handleSizeChange="handleSizeChange"
    @buttonClick="handleTableButtonClick"
    @filter="handleFilter"
    :defaultPageSize="10"
    :totalCount="totalCount"
    ref="table"
  ></v-table>
</template>

<script>
import vTable from '@/components/tools/vtable'
import { mapGetters } from 'vuex'
import { liveApi } from '@/api'

const statusFilters = [{
  text: '未开始',
  value: 0,
}, {
  text: '直播中',
  value: 1,
}, {
  text: '已结束',
  value: 2,
}]

export default {
  props: {
    from: {
      type: String,
      require: true,
      default: 'live'
    }
  },

  data: () => ({
    columns: [{
      label: '直播标题',
      key: 'title',
      // filter: 'formatDate',
      // filterProps: ['yyyy/MM/dd hh:mm'],
      // sortable: true
    }, {
      label: '状态',
      key: 'status',
      filter: 'status',
      filters: statusFilters,
      multiple: false
    }, {
      label: '主播昵称',
      key: 'anchorNick',
    }, {
      label: '直播ID',
      key: 'liveId',
    }, {
      label: 'UV',
      key: 'uv',
      filter: 'number'
    }, {
      label: 'PV',
      key: 'pv',
      filter: 'number'
    }, {
      label: '在线人数',
      key: 'onlineCount',
      filter: 'number'
    }, {
      label: '操作',
      type: 'action',
      width: '100',
      selectButton: true,
      buttonInfos: [{
        name: 'detail',
        label: '进入中控台',
        color: 'primary'
      }, {
        name: 'view',
        label: '观看',
        color: 'success'
      }]
    }],
    tableData: [],
    loading: false,
    pageNumber: 1,
    pageSize: 10,
    status: 3,
    totalCount: 0,
  }),

  methods: {
    changeStatus(e) {
      this.status = e
      this.pageNumber = 1
      this.$refs.table.setCurrentPage(1)
      this.listLiveRooms()
    },

    async listLiveRooms() {
      this.loading = true
      try {
        const res = await liveApi.listLiveRooms({
          status: this.status,
          pageNumber: this.pageNumber,
          pageSize: this.pageSize
        })
        this.totalCount = res.data.result.totalCount
        this.tableData = res.data.result.liveList.map(item => {
          item.btnList = item.anchorId === this.userId ? ['detail'] : ['view']
          return item
        })
      } catch (err) {
        console.error(err)
      } finally {
        this.loading = false
      }
    },

    jumpToStandard(liveId) {
      liveApi.getStandardRoomJumpUrl({
        bizId: liveId,
        userId: this.userId,
        userNick: this.userNick,
      }).then((res) => {
        window.open(res.data.result.standardRoomJumpUrl)
        // console.log(res.data.result.standardRoomJumpUrl)
      })
    },

    handleCurrentChange(e) {
      this.pageNumber = e
      this.listLiveRooms()
    },

    handleSizeChange(e) {
      this.pageSize = e
      this.listLiveRooms()
    },

    handleFilter(e) {
      if (e.status) {
        const [status] = e.status
        this.changeStatus(status || status === 0 ? status : 3)
      }
    },

    handleTableButtonClick(e) {
      const { button, data } = e
      if (button === 'detail') {
        this.$router.push({
          path: `/${this.from}/liveDetail/${data.liveId}`,
        })
      }
      if (button === 'view') {
        this.jumpToStandard(data.liveId)
      }
    },
  },

  created() {
    this.listLiveRooms()
  },

  computed: {
    ...mapGetters(['userId', 'userNick'])
  },

  components: {
    vTable
  }
}
</script>

<style scoped lang="less">
</style>
