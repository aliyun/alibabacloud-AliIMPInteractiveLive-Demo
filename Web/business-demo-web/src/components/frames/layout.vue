<template>
  <div class="page-layout">
    <el-breadcrumb separator="/" style="margin: 15px 0 25px;">
      <el-breadcrumb-item v-for="item in bread" :key="item.title">
        {{ item.title }}
      </el-breadcrumb-item>
    </el-breadcrumb>
    <router-view></router-view>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
export default {
  computed: {
    ...mapGetters(['bread'])
  }
}
</script>
