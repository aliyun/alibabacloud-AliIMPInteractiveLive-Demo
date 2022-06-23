<template>
  <div class="page">
    <div class="component-container">
      <h2 class="header">场景选择</h2>
      <div class="scene-container">
        <div class="scene">
          <div class="thumbnail" style="background-image: url('https://img.alicdn.com/imgextra/i1/O1CN01OchAjp1wAKuB5YBlH_!!6000000006267-2-tps-170-112.png')"></div>
          <div class="scene-info">
            <div class="name">互动直播-通用</div>
            <div class="info">互动直播主要应用于互联网与泛娱乐行业新增直播玩法、电商行业。</div>
          </div>
          <el-button type="primary" size="small" class="btn" @click="enterLiveList">进入</el-button>
        </div>
        <div class="scene">
          <div class="thumbnail" style="background-image: url('https://img.alicdn.com/imgextra/i1/O1CN01D9PQbf1VNcltGptR1_!!6000000002641-2-tps-170-112.png')"></div>
          <div class="scene-info">
            <div class="name">互动课堂</div>
            <div class="info">互动课堂，用户可以快速搭建专属的线上互动课堂能力，帮助客户展开自有平台的教学任务。</div>
          </div>
          <el-button type="primary" size="small" class="btn" @click="enterClassList">进入</el-button>
        </div>
      </div>
      <div class="scene-container">
        <div class="scene">
          <div class="thumbnail" style="background-image: url('https://img.alicdn.com/imgextra/i4/O1CN01hGZOvH1EnrzwSHUJf_!!6000000000397-2-tps-170-112.png')"></div>
          <div class="scene-info">
            <div class="name">互动直播-企业直播</div>
            <div class="info">企业直播主要用于企业内部培训，外部推广等场景，用户可快速建设专属企业直播平台。</div>
          </div>
          <el-button type="primary" size="small" class="btn" @click="enterEnterpriseList">进入</el-button>
        </div>
        <div class="scene">
          <div class="thumbnail" style="background-image: url('https://img.alicdn.com/imgextra/i4/O1CN01wPYNSW1saCoS34QUQ_!!6000000005782-2-tps-170-114.png')"></div>
          <div class="scene-info">
            <div class="name">互动直播-电商带货</div>
            <div class="info">电商直播适用于在线商城业务中加入直播带货功能，可以快速支持直播间与原有业务系统打通。</div>
          </div>
          <el-button type="primary" size="small" class="btn" @click="enterCommerceList">进入</el-button>
        </div>
      </div>
    </div>
    <div class="component-container">
      <h2 class="header">使用指引</h2>
      <Guide />
    </div>
  </div>
</template>

<script>
import Guide from '@/components/functional/guide'
export default {
  data: () => ({
  }),

  methods: {
    enterLiveList() {
      this.$router.push({ path: '/live/liveList' })
    },

    enterClassList() {
      window.open('https://market.m.taobao.com/app/room-paas/standard-classroom-web/index.html')
    },

    enterEnterpriseList() {
      this.$router.push({ path: '/enterprise/liveList' })
    },

    enterCommerceList() {
      // this.$router.push({ path: '/live/liveList' })
      this.$message.info('敬请期待')
    },
  },

  created() {
  },

  components: {
    Guide
  }
}
</script>

<style scoped lang="less">
  .component-container {
    background-color: #fff;
    width: 100%;
    min-width: 1200px;
    padding: 20px 0;
    margin-bottom: 20px;
    .header {
      text-align: center;
      font-size: 28px;
      font-weight: 500;
      margin: 10px 0 20px;
    }
  }
  .scene-container {
    display: flex;
    justify-content: center;
    max-width: 1600px;
    width: 100%;
    min-width: 1200px;
    margin: 10px auto;
    padding: 0 100px;
    .scene {
      position: relative;
      border: 1px solid #DCDFE6;
      border-radius: 4px;
      display: flex;
      padding: 20px;
      flex-basis: 50%;
      &:last-child {
        margin-left: 20px;
      }
      .thumbnail {
        width: 195px;
        height: 124px;
        background-repeat: no-repeat;
        background-size: cover;
        background-position: center;
        margin-right: 15px;
        flex-shrink: 0;
      }
      .scene-info {
        .name {
          font-size: 16px;
          font-weight: 500;
          margin-bottom: 5px;
        }
        .info {
          color: #909399;
        }
      }
      .btn {
        position: absolute;
        right: 20px;
        bottom: 20px;
      }
      &:hover {
        border: 1px solid #1989FA;
      }
    }
  }
</style>
