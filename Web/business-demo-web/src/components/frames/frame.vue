<template>
  <el-container>
    <el-header>
      <head-bar v-on:sidebar="handleSidebar"></head-bar>
    </el-header>
    <el-container>
      <el-aside :width="isCollapse ? '64px' : '200px'" v-show="!route.meta.hideMenu">
        <side-bar :is-collapse="isCollapse"></side-bar>
      </el-aside>
      <el-main>
        <router-view/>
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import headBar from './headbar'
import sideBar from './sidebar'
import { mapGetters } from 'vuex'

export default {
  data: () => ({
    isCollapse: false
  }),

  methods: {
    handleSidebar(isCollapse) {
      this.isCollapse = isCollapse
    }
  },

  computed: {
    ...mapGetters(['bread']),
    route() {
      return this.$route
    }
  },

  components: {
    headBar, sideBar
  }
}
</script>

<style>
</style>
