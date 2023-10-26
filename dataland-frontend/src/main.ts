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
import { createPinia, type PiniaPlugin } from "pinia";
import { PiniaSharedState } from "pinia-shared-state";

/**
 * The main entrypoint of the dataland frontend initiating the vue app
 */
function instantiateVueApp(): void {
  const app = createApp(App);
  const script = document.createElement("script");
  script.id = "Cookiebot";
  script.src = "https://consent.cookiebot.com/uc.js";
  script.setAttribute("data-cbid", "cba5002e-6f0e-4848-aadc-ccc8d5c96c86");
  script.setAttribute("data-blockingmode", "auto");
  script.type = "text/javascript";
  document.head.appendChild(script);
  const pinia = createPinia();
  pinia.use(
    PiniaSharedState({
      enable: false,
      type: "native",
    }) as PiniaPlugin,
  );
  app.use(plugin, defaultConfig);
  app.use(DialogService);
  app.use(router);
  app.use(PrimeVue);
  app.use(pinia);

  app.mount("#app");
}

instantiateVueApp();
