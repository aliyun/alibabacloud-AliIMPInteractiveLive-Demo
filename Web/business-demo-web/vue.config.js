/*
* Config for project
* Author: Kinice
*/
const fs = require('fs')
const path = require('path')

module.exports = {
  publicPath: '/',
  // All webpack-dev-server are supported
  devServer: {
    port: 8000,
    host: '0.0.0.0',
    hot: true,
    overlay: true,
    openPage: '/',
    // new proxy tables as same as the past 'proxyTable'
    proxy: {
      '/v1': {
        target: 'http://example.com',
        changeOrigin: true,
        pathRewrite: {
          '^/v1': ''
        }
      }
    }
  }
}
