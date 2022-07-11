const { defineConfig } = require('@vue/cli-service')
let preConfig = defineConfig({
  transpileDependencies: true,
  devServer: {
   client: {
     webSocketURL: 'wss://localhost/ws',
   }
  }
})
module.exports = preConfig;