const path = require('path')
const config = require('./config.js')
const webpack = require('webpack')

module.exports = {
    entry: './index.js',
    target: 'web',
    output: {
        path: path.resolve(__dirname, 'dist'),
        filename: 'index.bundle.js',
    },
    devServer: {
        contentBase: [path.join(__dirname, "dist"), __dirname],
        compress: false,
        port: 9999,
        host: '127.0.0.1',
        proxy: {
            '/api': {
                target: config.origin,
                secure: false,
                changeOrigin: true,
            },
        },
    },
    plugins: [
        new webpack.DefinePlugin({
            config_appId: `'${config.appId}'`,
            config_appKey: `'${config.appKey}'`,
            config_origin: `'${config.origin}'`,
        }),
    ],
};