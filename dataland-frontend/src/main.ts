import { createApp } from 'vue'
import App from './App.vue'
import 'primevue/resources/themes/saga-blue/theme.css'
import 'primevue/resources/primevue.min.css'
import 'primeicons/primeicons.css'
import 'primeflex/primeflex.css'
import '@formkit/themes/genesis'
import { plugin, defaultConfig } from '@formkit/vue'
import router from './router'
import PrimeVue from 'primevue/config';
createApp(App).use(plugin, defaultConfig).use(router).use(PrimeVue).mount('#app')