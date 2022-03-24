import { createApp } from 'vue'
import App from './App.vue'
import 'materialize-css/dist/css/materialize.min.css'
import 'materialize-css'
import '@formkit/themes/genesis'
import { plugin, defaultConfig } from '@formkit/vue'
import router from './router'
import PrimeVue from 'primevue/config';
createApp(App).use(plugin, defaultConfig).use(router).use(PrimeVue).mount('#app')