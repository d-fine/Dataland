import {createApp} from 'vue'
import App from './App.vue'

import 'primevue/resources/primevue.min.css'
import 'primeicons/primeicons.css' // use this for icons embedded in inputs and declarative elements such as menus -> decent alignment
import 'material-icons/iconfont/material-icons.css'; // prefer these icons in most cases such as stand-alone or in buttons -> recommended by UI/UX
import 'primeflex/primeflex.css'
import './assets/theme/theme.css'
import '@formkit/themes/genesis'
import {plugin, defaultConfig} from '@formkit/vue'
import router from './router'
import PrimeVue from 'primevue/config';

function instantiateVueApp() {
    const app = createApp(App)
    app.use(plugin, defaultConfig)
    app.use(router)

    //router.beforeResolve(to => {
    //    if (to.name == "Create Data" && !this.keycloak_authenticated) {
    //        return {name: "Welcome to Dataland"}
    //    }
    //})

    app.use(PrimeVue)
    app.config.unwrapInjectedRef = true
    app.mount('#app')
}

instantiateVueApp()
