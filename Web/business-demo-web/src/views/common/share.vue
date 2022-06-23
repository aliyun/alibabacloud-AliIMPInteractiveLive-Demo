<template>
  <div class="share">
    <div class="login-container">
        <div class="logo"></div>
        <div class="login-form-container">
          <div class="login-form-main">
            <div class="title">
              阿里云低代码音视频工厂
            </div>
            <div class="intro">
              <p>{{sharer}} 邀请您观看直播：</p>
              <p>{{title}}</p>
            </div>
            <div class="form">
              <input type="userNick" name="userNick" v-model="nick" id="userNick" placeholder="请输入任意昵称">
              <button @click="login">进入</button>
            </div>
          </div>
        </div>
      </div>
  </div>
</template>

<script>
import { liveApi } from '@/api'
import MD5 from 'crypto-js/md5'

export default {
  data: () => ({
    nick: '',
    title: '',
    sharer: '',
    liveId: '',
  }),

  methods: {
    login() {
      if (!this.nick) {
        this.$message.error('请输入任意昵称')
        return
      }
      const userId = MD5(this.nick).toString().substr(0, 20)
      liveApi.getStandardRoomJumpUrl({
        bizId: this.liveId,
        userId: userId,
        userNick: this.nick,
      }).then((res) => {
        window.open(res.data.result.standardRoomJumpUrl)
      })
    }
  },

  created() {
    const { data } = this.$route.query
    const json = window.decodeURIComponent(window.atob(data))
    try {
      const res = JSON.parse(json)
      this.title = res.title
      this.sharer = res.sharer
      this.liveId = res.liveId
    } catch (err) {
      this.$message.error('数据有误，请检查链接')
    }
  }
}
</script>

<style lang="less" scoped>
.share{
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(29,33,34,1);
  .intro {
    color: #fff;
    text-align: center;
    margin: 50px 0;
    p {
      font-size: 18px;
    }
  }
  .login-container {
    display: flex;
    width: 1200px;
    height: 700px;
    position: absolute;
    border-radius: 8px;
    background-color: #42454B;
    .logo {
      width: 600px;
      background-image: url('https://img.alicdn.com/imgextra/i4/O1CN014swlH31dJW6OCYjvC_!!6000000003715-2-tps-600-648.png');
      background-size: cover;
      background-position: center;
      border-radius: 8px 0 0 8px;
    }
    .login-form-container {
      padding: 100px 0 90px;
      display: flex;
      justify-content: center;
      flex-grow: 1;
      border-radius: 0 8px 8px 0;
      .login-form-main {
        width: 400px;
        .title {
          font-size: 30px;
          font-family: PingFangSC-Light;
          background-image: url('https://img.alicdn.com/imgextra/i1/O1CN01CQ4ImC1uxDtOFDJPQ_!!6000000006103-2-tps-112-112.png');
          background-size: auto 100%;
          background-repeat: no-repeat;
          background-position: left center;
          padding-left: 53px;
          margin-bottom: 20px;
          user-select: none;
          color: #fff;
        }
      }
    }
    .form {
      .publicInput{
        display: block;
        width: 400px;
        height: 52px;
        margin: 0 0 15px;
        border-radius: 2px;
      }
      input{
        .publicInput;
        border: 1px solid #fff;
        background: transparent;
        color: #fff;
        padding: 0 18px;
      }
      button{
        .publicInput;
        background: #0093AD;
        color: #fff;
        border: none;
        text-align: center;
      }
    }
  }
}
</style>
