import { createApp } from 'vue';
import App from './App.vue';

// import 'primevue/resources/primevue.min.css'; path does not exist anymore: look at styled mode/recreate custom themes using new API (https://primevue.org/guides/migration/v4/)
import 'primeicons/primeicons.css'; // use this for icons embedded in inputs and declarative elements such as menus -> decent alignment
import 'material-icons/iconfont/material-icons.css'; // prefer these icons in most cases such as stand-alone or in buttons -> recommended by UI/UX
import 'primeflex/primeflex.min.css';
import './assets/scss/global.scss';
import './assets/css/ibm-plex-sans.scss';
import '@formkit/themes/genesis';
import { plugin, defaultConfig } from '@formkit/vue';
import router from './router';
import PrimeVue from 'primevue/config';
import DialogService from 'primevue/dialogservice';
import { createPinia, type PiniaPlugin } from 'pinia';
import { PiniaSharedState } from 'pinia-shared-state';

/**
 * The main entrypoint of the dataland frontend initiating the vue app
 */
function instantiateVueApp(): void {
  const app = createApp(App);
  const pinia = createPinia();
  pinia.use(
    PiniaSharedState({
      enable: true,
      type: 'native',
    }) as PiniaPlugin
  );
  app.use(plugin, defaultConfig);
  app.use(DialogService);
  app.use(router);
  app.use(PrimeVue);
  app.use(pinia);

  app.mount('#app');
}

instantiateVueApp();
