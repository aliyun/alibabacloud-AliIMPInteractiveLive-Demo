/* eslint-disable */ //Disable for process
import axios from 'axios'
import Vue from 'vue'

function setInterceptors (axiosInstance) {
  // request interceptor
  axiosInstance.interceptors.request.use(req => {
    if (process.env.NODE_ENV !== 'production') {
      console.time(req.method.toUpperCase() + ' ' + '/api' + req.url)
    }
    return req
  }, error => {
    console.error(error)
    return Promise.reject(error)
  })

  // response interceptor
  axiosInstance.interceptors.response.use(res => {
    if (process.env.NODE_ENV !== 'production') {
      console.timeEnd(res.config.method.toUpperCase() + ' ' + res.config.url)
    }
    return res
  }, error => {
    console.error(error)
    if (error.code === 'ECONNABORTED') {
      console.error('网络错误，请检查网络连接刷新后重试')
    } else {
      console.dir(error)
      if (error.response.data.Code && error.response.data.Code.indexOf('SensitiveContent') > -1) {
        Vue.prototype.$message.error('您输入的字段包含敏感词，请检查后重试')
      } else {
        Vue.prototype.$message.error(error.response.data.Message || error.response.data.message || error.message)
      }
    }
    return Promise.reject(error)
  })
}

// create axios instance
function getAxios (settings, timeout = 10000) {
  const axiosInstance = axios.create(settings)
  axios.defaults.timeout = timeout
  setInterceptors(axiosInstance)
  return axiosInstance
}

export const api = getAxios({
  // mock接口 不配置baseURL
  // baseURL: process.env.NODE_ENV === 'development' ? '/v1' : `${window.location.protocol}//demo.pod.imp.aliyuncs.com/v1`,
  withCredentials: false,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})
