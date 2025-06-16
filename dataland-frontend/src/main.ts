import { definePreset } from '@primeuix/themes';
import { createApp } from 'vue';
import App from './App.vue';

import 'primeicons/primeicons.css'; // use this for icons embedded in inputs and declarative elements such as menus -> decent alignment
import 'material-icons/iconfont/material-icons.css'; // prefer these icons in most cases such as stand-alone or in buttons -> recommended by UI/UX
import 'primeflex/primeflex.min.css';
import '@/assets/scss/global.scss';
import '@/assets/fonts/ibm-plex-sans.scss';
import '@formkit/themes/genesis';
import { plugin, defaultConfig } from '@formkit/vue';
import router from './router';
import PrimeVue from 'primevue/config';
import Aura from '@primeuix/themes/aura';
import DialogService from 'primevue/dialogservice';
import { createPinia, type PiniaPlugin } from 'pinia';
import { PiniaSharedState } from 'pinia-shared-state';

const DatalandPreset = definePreset(Aura, {
  semantic: {
    primary: {
      50: '#e67f3f',
      100: '#e67f3f',
      // 200: '#e67f3f',
      // 300: '#e67f3f',
      400: '#e67f3f',
      500: '#e67f3f',
      600: '#e67f3f',
      700: '#e67f3f',
      800: '#cc7139',
    },
  },
});

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
    },
  });
  app.use(pinia);

  app.mount('#app');
}

instantiateVueApp();
