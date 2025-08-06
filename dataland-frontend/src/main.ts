import { DatalandPreset } from '@/assets/theme/dataland-theme.ts';
import { defaultConfig, plugin } from '@formkit/vue';
import { createPinia, type PiniaPlugin } from 'pinia';
import { PiniaSharedState } from 'pinia-shared-state';
import PrimeVue from 'primevue/config';
import DialogService from 'primevue/dialogservice';
import { createApp } from 'vue';
import App from './App.vue';

import 'primeicons/primeicons.css'; // use this for icons embedded in inputs and declarative elements such as menus -> decent alignment
import 'material-icons/iconfont/material-icons.css'; // prefer these icons in most cases such as stand-alone or in buttons -> recommended by UI/UX
import 'primeflex/primeflex.min.css';
import '@/assets/fonts/ibm-plex-sans.scss';
import '@/assets/theme/main.css';
import '@formkit/themes/genesis';
import router from './router';
import Vue3Tour from 'vue3-tour';
import'vue3-tour/dist/vue3-tour.css';
import Steps from 'primevue/steps';
import Dialog from 'primevue/dialog';

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
  app.use(PrimeVue, {
    theme: {
      preset: DatalandPreset,
      options: {
        darkModeSelector: 'none',
      },
    },
  });
  app.use(pinia);
  app.use(Vue3Tour);
  app.component('Steps',Steps);
  app.component('Dialog',Dialog);

  app.mount('#app');
}

instantiateVueApp();