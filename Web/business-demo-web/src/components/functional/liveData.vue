<template>
  <div class="live-data">
    <div class="item">
      <span class="name">直播时长</span>
      <span class="detail">{{generateCountTime(countTime)}}</span>
    </div>
    <div class="item">
      <span class="name">在线人数</span>
      <span class="detail">{{liveDetail.onlineCount}}</span>
    </div>
    <div class="item">
      <span class="name">PV</span>
      <span class="detail">{{liveDetail.pv}}</span>
    </div>
    <div class="item">
      <span class="name">UV</span>
      <span class="detail">{{liveDetail.uv}}</span>
    </div>
  </div>
</template>

<script>
import { formatDate } from '@/libs/utils'

export default {
  props: {
    liveDetail: {
      type: Object,
      require: true,
      default: () => ({})
    },

    countTime: {
      type: Number,
      require: true,
      default: 0
    }
  },

  methods: {
    formatDate(date) {
      return date ? formatDate(date, 'yyyy-MM-dd hh:mm:ss') : 0
    },

    generateCountTime(ms) {
      if (ms < 0) ms = 0
      const timeSecond = Math.floor(ms / 1000)
      const s = timeSecond % 60
      const m = Math.floor(timeSecond / 60) % 60
      const h = Math.floor(timeSecond / 3600)
      return `${this.fillZero(h)}:${this.fillZero(m)}:${this.fillZero(s)}`
    },

    fillZero(n) {
      return n < 10 ? '0' + n : n
    },
  }
}
</script>

<style scoped lang="less">
.live-data {
  display: flex;
  border-top: 3px solid #f2f2f2;
  border-bottom: 3px solid #f2f2f2;
  .item {
    position: relative;
    flex-grow: 1;
    padding: 10px 28px;
    &::after {
      content: '';
      display: block;
      position: absolute;
      right: 0;
      top: 22%;
      height: 56%;
      width: 1px;
      background: #ddd;
    }
    &:last-child {
      &::after {
        content: '';
        display: none;
      }
    }
    .name {
      display: block;
      font-size: 13px;
      color: #888;
    }
    .detail {
      display: block;
      font-size: 26px;
    }
  }
}
</style>
