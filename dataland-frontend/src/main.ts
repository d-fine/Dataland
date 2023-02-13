import { createApp } from "vue";
import App from "./App.vue";

import "primevue/resources/primevue.min.css";
import "primeicons/primeicons.css"; // use this for icons embedded in inputs and declarative elements such as menus -> decent alignment
import "material-icons/iconfont/material-icons.css"; // prefer these icons in most cases such as stand-alone or in buttons -> recommended by UI/UX
import "primeflex/primeflex.min.css";
import "./assets/scss/global.scss";
import "./assets/css/ibm-plex-sans.scss";
import "@formkit/themes/genesis";
import { plugin, defaultConfig } from "@formkit/vue";
import router from "./router";
import PrimeVue from "primevue/config";
import DialogService from "primevue/dialogservice";
import { createPinia } from "pinia";

/**
 * The main entrypoint of the dataland frontend initiating the vue app
 */
function instantiateVueApp(): void {
  const app = createApp(App);
  const pinia = createPinia();
  app.use(plugin, defaultConfig);
  app.use(DialogService);
  app.use(router);
  app.use(PrimeVue);
  app.use(pinia);
  app.config.unwrapInjectedRef = true;
  app.config.globalProperties.$sanitizeURL = function (url: string): string {
    console.log("$sanitizeURL", url);
    return url.replace(/[^a-zA-Z0-9.:/?#&=_-]+/g, "");
  };
  app.mount("#app");
}

instantiateVueApp();
