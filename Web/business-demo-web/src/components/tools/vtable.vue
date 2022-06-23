<template>
  <div class="table-container">
    <el-table
      :data="paginationData"
      :border="border"
      :stripe="stripe"
      style="width: 100%"
      v-loading="loading"
      @selection-change="handleSelectionChange"
      @filter-change="handleFilter"
    >
      <el-table-column
        v-for="(column) in columns"
        :column-key="column.key"
        :prop="column.key"
        :label="column.label"
        :width="column.width"
        :sortable="column.sortable"
        :key="column.key"
        :align="column.align || 'center'"
        :filters="column.filters || null"
        :filter-multiple="!!column.multiple"
      >
        <template slot-scope="scope">
          <img v-if="column.type === 'image'" :src="scope.row[column.key]" :alt="scope.row[column.key]">
          <div class="action-con" v-else-if="column.type === 'action'">
            <!-- anchorId(创建直播间的用户) === userId(登录的用户) 可以进入中控台-->
            <el-button
              v-for="button in column.buttonInfos"
              v-show="column.selectButton ? scope.row.btnList.some((item) => {return item === button.name}) : true"
              :key="button.name"
              size="mini"
              :type="button.color"
              :class="[column.multiActions ? 'multi-actions' : '']"
              @click="handleButtonClick(button.name, scope.row)">{{ button.label }}</el-button>
          </div>
          <span v-else-if="column.type === 'html'" v-html="scope.row[column.key]"></span>
          <span v-else>{{ scope.row[column.key] | filter(column.filter, column.filterProps) }}</span>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination">
      <el-pagination
        @current-change="handleCurrentChange"
        @size-change="handleSizeChange"
        :current-page="currentPage"
        layout="total, prev, pager, next, sizes"
        :page-sizes="[10, 20, 50]"
        :page-size="pageSize"
        :total="total">
      </el-pagination>
    </div>
  </div>
</template>

<script>
import { mapActions } from 'vuex'
import { ROW_SELECT } from '@/store/tableActions'

export default {
  props: {
    isBackPage: {
      type: Boolean,
      required: false,
      default: false
    },

    totalCount: {
      type: Number,
      required: false,
      default: 0
    },

    selection: {
      type: Boolean,
      required: false,
      default: false
    },

    data: {
      type: Array,
      required: true,
      default: () => ([])
    },

    columns: {
      type: Array,
      required: true,
      default: () => ([])
    },

    border: {
      type: Boolean,
      required: false,
      default: true
    },

    stripe: {
      type: Boolean,
      required: false,
      default: true
    },

    defaultPageSize: {
      type: Number,
      required: false,
      default: 10
    },

    loading: {
      type: Boolean,
      required: false,
      default: false
    }
  },

  data() {
    return {
      currentPage: 1,
      pageSize: this.defaultPageSize,
    }
  },

  computed: {
    paginationData() {
      let tempData = []
      if (this.isBackPage) {
        tempData = this.data
      } else {
        let pageTotal = this.data.length,
          pageFirstCount = this.pageSize * (this.currentPage - 1),
          maxCount = pageTotal - pageFirstCount < this.pageSize ? pageTotal : (pageFirstCount + this.pageSize)

        for (let i = pageFirstCount; i < maxCount; i++) {
          tempData.push(this.data[i])
        }
      }
      return tempData
    },

    total: function() {
      return this.isBackPage ? this.totalCount : this.data.length
    }
  },

  methods: {
    ...mapActions([ROW_SELECT]),

    handleCurrentChange(currentPage) {
      this.currentPage = currentPage
      if (this.isBackPage) {
        this.$emit('handleCurrentChange', currentPage)
      }
    },

    handleSizeChange(pageSize) {
      this.pageSize = pageSize
      if (this.isBackPage) {
        this.$emit('handleSizeChange', pageSize)
      }
    },

    handleButtonClick(button, row) {
      this.$emit('buttonClick', {
        button: button,
        data: Object.assign({}, row)
      })
    },

    handleSelectionChange(val) {
      this.rowSelections = val
      this.ROW_SELECT(val) // save row info in an Array into vuex.state called `rowAction`
      this.$emit('selected')
    },

    handleFilter(val) {
      this.$emit('filter', val)
    },

    setCurrentPage(page) {
      this.currentPage = page
    }
  }
}
</script>

<style lang="less">
.table-container{
  .pagination{
    padding: 20px 0 0;
    text-align: right;
  }
  .action-con{
    display: flex;
    justify-content: space-around;
    flex-wrap: wrap;
    button {
      margin: 2px 0;
    }
  }
  .multi-actions{
    margin: 0 0 5px 0;
  }
}
</style>
