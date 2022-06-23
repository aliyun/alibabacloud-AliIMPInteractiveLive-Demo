<template>
  <nav class="header">
    <div class="collapse-btn" @click="handleSidebar">
      <i class="el-icon-menu"></i>
    </div>
    <div class="logo">低代码音视频工厂{{$route.meta.scene ? '-' + $route.meta.scene : ''}}</div>
    <div class="header-right">
      <!-- <el-tooltip class="item" effect="dark" content="系统消息" placement="bottom">
        <el-badge is-dot class="badge">
          <i class="el-icon-bell"></i>
        </el-badge>
      </el-tooltip> -->
      <el-dropdown trigger="click" @command="handleCommand">
        <span class="el-dropdown-link">
          {{userNick}}<i class="el-icon-arrow-down el-icon--right"></i>
        </span>
        <el-dropdown-menu slot="dropdown">
          <!-- <el-dropdown-item command="password">修改密码</el-dropdown-item> -->
          <el-dropdown-item command="exit">退出登录</el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
    </div>
  </nav>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  data: () => ({
    isCollapse: false
  }),

  methods: {
    handleCommand(command) {
      this.$message.success(command)
      if (command === 'exit') {
        this.$router.replace({
          name: 'login'
        })
      }
    },

    handleSidebar() {
      console.log(this.$route)
      this.isCollapse = !this.isCollapse
      this.$emit('sidebar', this.isCollapse)
    }
  },

  computed: {
    ...mapGetters(['userNick']),
  },
}
</script>

<style lang="less" scoped>
.header{
  .collapse-btn{
    float: left;
    padding: 2px 20px 0;
    cursor: pointer;
    height: 60px;
    &:hover{
      background-color: #283446;
    }
    i{
      font-size: 20px;
    }
  }
  .logo{
    float: left;
    font-size: 21px;
  }
  .header-right{
    float: right;
    padding-right: 30px;
    .item{
      margin-right: 25px;
      i{
        font-size: 21px;
      }
    }
    .el-dropdown {
      color: #fff;
      .el-dropdown-link{
        cursor: pointer;
      }
    }
  }
}
</style>
