<template>
  <div class="login">
    <div class="login-form">
      <div class="login-title">
        <router-link to="/"><h1>DEAD END</h1></router-link>
      </div>
    </div>
  </div>
</template>

<style lang="less" scoped>
.login{
  width: 100%;
  height: 100%;
  background: #324057;
  .login-title{
    margin-bottom: 60px;
    h1{
      color: #fff;
      font-size: 120px;
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
  }
}
</style>
