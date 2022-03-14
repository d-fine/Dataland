import { createApp } from 'vue'
import App from './App.vue'
import 'materialize-css/dist/css/materialize.min.css'
import 'materialize-css'
import { plugin, defaultConfig } from '@formkit/vue'
import router from './router'

createApp(App).use(plugin, defaultConfig).use(router).mount('#app')