<template>
  <div class="login">
    <div class="login-form" @keyup.enter="login">
      <div class="login-title">
        <h1>低代码音视频工厂</h1>
      </div>
      <label for="userNick">账号</label>
      <input type="userNick" name="userNick" v-model="form.userNick" id="userNick" placeholder="请输入中文字符、字母或数字，32位以内">
      <button class="login-btn" @click="login">登录</button>
      <div class="link">
        <el-button type="text" @click="openDoc">如何对接你的账号，进行登录？</el-button>
      </div>
    </div>
  </div>
</template>
<script>
import { mapActions } from 'vuex'
import { USER_SIGNIN, USER_SIGNOUT } from '@/store/user'
import MD5 from 'crypto-js/md5'

const userReg = /^[\u4e00-\u9fa5a-zA-Z0-9]+$/

export default {
  data: () => ({
    form: {
      userNick: '',
      userId: ''
    }
  }),

  methods: {
    ...mapActions([USER_SIGNIN, USER_SIGNOUT]),
    login() {
      if (!this.form.userNick) return this.$message.error('请输入账号昵称')
      if (!userReg.test(this.form.userNick) || this.form.userNick.length > 32) return this.$message.error('请输入中英文与数字，32位以内')
      this.form.userId = MD5(this.form.userNick).toString().substr(0, 20)
      this.USER_SIGNIN(this.form)
      this.$message.success('登录成功')
      this.$router.replace({ path: '/selectScene' })
    },
    openDoc() {
      this.$message.info('敬请期待')
    }
  },

  mounted() {
    if (this.$store.state.User.userNick) {
      this.USER_SIGNOUT()
    }
  }
}
</script>

<style lang="less" scoped>
.publicInput{
  display: block;
  width: 400px;
  height: 45px;
  margin: 0 0 15px;
  border-radius: 5px;
}
.login{
  width: 100%;
  height: 100%;
  background: #324057;
  .login-title{
    margin-bottom: 60px;
    h1{
      color: #fff;
      font-size: 40px;
      text-align: center;
    }
  }
  .login-form{
    position: absolute;
    top: 40%;
    left: 50%;
    transform: translate(-50%,-50%);
    -webkit-transform: translate(-50%,-50%);
    -ms-transform: translate(-50%,-50%);
    label {
      color: #fff;
      font-size: 15px;
      line-height: 2.2;
    }
    input{
      .publicInput;
      border: 1px solid #fff;
      background: transparent;
      color: #fff;
      padding: 0 18px;
    }
    .login-btn{
      .publicInput;
      background: #fff;
      color: #324057;
      border: none;
      text-align: center;
    }
    .link {
      text-align: center;
    }
  }
}
</style>
