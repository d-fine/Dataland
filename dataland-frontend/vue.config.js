const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
})

module.exports = {
  devServer: {
    proxy: {
      '/api/': {
        target: "http:/localhost:80/api/",
        secure: false
      },
      "/gists": {
        target: "https://api.github.com",
        secure: false
      }
    }
  }
}
