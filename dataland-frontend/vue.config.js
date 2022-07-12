const { defineConfig } = require('@vue/cli-service')
let preConfig = defineConfig({
  transpileDependencies: true,
  devServer: {
   allowedHosts: ['dataland-local.duckdns.org'],
   client: {
     webSocketURL: 'wss://dataland-local.duckdns.org/ws',
   }
  }
})
module.exports = preConfig;