<template>
  <aside class="aside">
    <el-menu :default-active="route.path" class="el-menu-vertical-demo" :collapse="isCollapse" router>
      <template v-for="(item, index) in routers">
        <el-submenu v-if="item.children" :key="index" :index="item.path">
          <template slot="title">
            <i :class="item.meta.icon"></i>
            <span>{{ item.meta.title }}</span>
          </template>
          <el-menu-item v-for="subItem in item.children" v-show="!subItem.meta.notMenu" :index="subItem.path" :key="subItem.path">
            {{ subItem.meta.title }}
          </el-menu-item>
        </el-submenu>
        <el-menu-item v-else-if="!item.meta.notMenu" :index="item.path" :key="index">
          <i :class="item.meta.icon"></i>
          <span slot="title">{{ item.meta.title }}</span>
        </el-menu-item>
      </template>
    </el-menu>
  </aside>
</template>

<script>
import { routerMap } from '@/router/routerMap'

export default {
  props: {
    isCollapse: {
      type: Boolean,
      require: false,
      default: false
    }
  },

  data: () => ({
    routers: []
  }),

  updated() {
    this.generateRoute()
  },

  created() {
    this.generateRoute()
  },

  methods: {
    generateRoute() {
      const from = this.$route.fullPath.split('/')[1]
      let index = 0
      if (from) {
        index = routerMap.findIndex(item => item.path.split('/')[1] === from)
      }
      if (index < 0) index = 0
      this.routers = routerMap[index].children
    }
  },

  computed: {
    route() {
      return this.$route
    }
  }
}
</script>

<style lang="less">
.aside{
  height: 100%;
  overflow-y: scroll;
  .el-menu{
    height: 100%;
    .iconfont{
      vertical-align: middle;
      margin-right: 8px;
      width: 24px;
      text-align: center;
      font-size: 17px;
    }
  }
}
</style>
