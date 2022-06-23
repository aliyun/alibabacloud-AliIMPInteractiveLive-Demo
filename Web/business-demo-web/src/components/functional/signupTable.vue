<template>
  <v-table
    :data="tableData"
    :columns="columns"
    :loading="loading"
    @buttonClick="handleTableButtonClick"
    :defaultPageSize="10"
    :totalCount="totalCount"
    ref="table"
  ></v-table>
</template>

<script>
import vTable from '@/components/tools/vtable'
const mockData = {
  userId: '55667980.@wqq',
  mobile: '152XXXX8585',
  time: '2022.06.01',
  status: '已通过'
}

export default {
  data: () => ({
    columns: [{
      label: '用户ID',
      key: 'userId',
    }, {
      label: '手机号',
      key: 'mobile',
    }, {
      label: '报名时间',
      key: 'time',
      filter: 'number'
    }, {
      label: '审核情况',
      key: 'status',
      filter: 'number'
    }, {
      label: '详情',
      type: 'action',
      buttonInfos: [{
        name: 'detail',
        label: '查看详情',
        color: 'text'
      }]
    }],
    tableData: new Array(20).fill(mockData),
    loading: false,
    pageNumber: 1,
    pageSize: 10,
    status: 3,
    totalCount: 0,
  }),

  methods: {
    handleTableButtonClick(e) {
      const { button } = e
      if (button === 'detail') {
        this.$message.info('敬请期待')
      }
    },
  },

  components: {
    vTable
  }
}
</script>

<style scoped lang="less">
</style>
