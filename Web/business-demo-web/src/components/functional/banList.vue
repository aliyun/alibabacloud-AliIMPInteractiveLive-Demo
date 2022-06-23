<template>
  <div class="user-list">
    <div class="list-item" v-for="item in banList" :key="item.userId">
      <div class="user-info-container">
        <div class="avatar">{{item.nick.substr(0, 1).toUpperCase()}}</div>
        <div class="user-info">
          <div class="name">{{item.nick}}</div>
          <div class="status">{{item.muted && '禁言中'}}</div>
        </div>
      </div>
      <i class="el-icon-more" @click="clickMore"></i>
    </div>
  </div>
</template>

<script>

export default {
  data: () => ({
    banList: []
  }),
  methods: {
    clickMore() {
      this.$message.info('敬请期待')
    },
  }
}
</script>

<style scoped lang="less">
.user-list {
  .list-item {
    font-size: 16px;
    margin: 10px 0 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    .user-info-container {
      display: flex;
      .avatar {
        width: 38px;
        height: 38px;
        border-radius: 50%;
        overflow: hidden;
        margin-right: 8px;
        text-align: center;
        background-color: #4facff;
        line-height: 38px;
        font-size: 16px;
        color: #fff;
      }
      .user-info{
        display: flex;
        flex-direction: column;
        justify-content: center;
        font-size: 14px;
        .name {
          color: #303133
        }
        .status {
          color: #909399;
        }
      }
    }
    i {
      cursor: pointer;
    }
  }
}
</style>
