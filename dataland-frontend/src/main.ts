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
import Keycloak from "keycloak-js";

const initOptions = {
    url: 'http://localhost:8095/', realm: 'myrealm', clientId: 'dataland-frontend'
}

export async function authenticateAgainstKeycloak(): Promise<void> {
    const keycloak = new Keycloak(initOptions)
    keycloak.init({onLoad: 'login-required'}).then((auth) => {
        if (!auth) {
            window.location.reload();
        } else {
            console.info("Authenticated");
        }
        if (keycloak.token) {
            window.sessionStorage.setItem('keycloakToken', keycloak.token)
        }
//Token Refresh
        setInterval(() => {
            keycloak.updateToken(70).then((refreshed) => {
                if (refreshed) {
                    console.info('Token refreshed' + refreshed);
                } else {
                    console.warn('Token not refreshed, valid for ')
                }
            }).catch(() => {
                console.error('Failed to refresh token');
            });
        }, 6000)

    }).catch(() => {
        console.error("Authenticated Failed");
    });
}

function instantiateVueApp() {
    const app = createApp(App)
    app.use(plugin, defaultConfig)
    app.use(router)
    app.use(PrimeVue)
    app.mount('#app')
}

if (!window.localStorage.getItem('keycloakToken')) {
    authenticateAgainstKeycloak().then(() => {
        instantiateVueApp()
    })
} else {
    instantiateVueApp()
}
