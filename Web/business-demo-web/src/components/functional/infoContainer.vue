<template>
  <div class="info-container">
    <div class="dark-header">
      {{title}}
      <i class="el-icon-refresh refresh" v-show="showRefresh" @click="handleClickRefresh"></i>
    </div>
    <slot></slot>
  </div>
</template>

<script>
import { formatDate } from '@/libs/utils'

export default {
  props: {
    title: {
      type: String,
      require: true,
      default: '信息'
    },
    showRefresh: {
      type: Boolean,
      require: false,
      default: false
    }
  },

  methods: {
    formatDate(date) {
      return date ? formatDate(date, 'yyyy-MM-dd hh:mm:ss') : 0
    },

    handleClickRefresh () {
      this.$emit('handleClickRefresh')
    }
  }
}
</script>

<style scoped lang="less">
.info-container {
  margin-top: 10px;
  background-color: #fff;
  .dark-header {
    position: relative;
    background: #242f42;
    color: #fff;
    font-size: 14px;
    text-align: center;
    padding: 10px 0;
    .refresh {
      position: absolute;
      right: 15px;
      font-size: 28px;
      top: 50%;
      transform: translateY(-50%);
      cursor: pointer;
    }
  }
}
</style>
