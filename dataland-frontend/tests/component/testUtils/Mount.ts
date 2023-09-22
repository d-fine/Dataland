import {routes} from "@/router";
import {createPinia} from "pinia";
import PrimeVue from "primevue/config";
import DialogService from "primevue/dialogservice";
import {defaultConfig, plugin} from "@formkit/vue";
import {createMemoryHistory, createRouter, type Router} from "vue-router";
import {mount} from "cypress/vue";

import Keycloak from "keycloak-js";
import {assertDefined} from "@/utils/TypeScriptUtils";

type AdditionalComponentMountingOptions = {
  router?: Router;
  keycloak?: Keycloak;
};


/**
 * A higher-order function that returns a
 * slightly modified version of the vue mount function that automatically initiates plugins used in dataland
 * like PrimeVue, Pinia or the Router and also allows for simple authentication injection
 * @param additionalOptions The Options configuring the returned mounting routine
 * @returns a modified mounting function with the desired properties.
 */
export function getMountingFunction(additionalOptions: AdditionalComponentMountingOptions | undefined = undefined): typeof mount {

  /*
    This file defines a alternative mounting function that also includes many creature comforts
    (mounting of plugins, mocking router, mocking keycloak, ....)
    However, the underlying type definition of the mount function is very complex (> 100 LOC).
    Therefore, we decided to create this un-checked meta-function.
  */
  return (component: any, options: any) => {
    options.global = options.global ?? {};
    options.global.plugins = options.global.plugins ?? [];
    options.global.plugins.push(createPinia());
    options.global.plugins.push(PrimeVue);
    options.global.plugins.push(DialogService);
    options.global.provide = options.global.provide ?? {};

    if (additionalOptions?.router) {
      options.router = additionalOptions.router;
    } else {
      options.router = createRouter({
        routes: routes,
        history: createMemoryHistory(),
      });
    }

    if (additionalOptions?.keycloak) {
      options.global.provide.getKeycloakPromise = (): Promise<Keycloak> => {
        return Promise.resolve(additionalOptions.keycloak as Keycloak);
      };
      options.global.provide.authenticated = true;
    }

    options.global.plugins.push({
      install(app: any) {
        app.use(assertDefined(options.router));
      },
    });

    options.global.plugins.push({
      install(app: any) {
        app.use(plugin, defaultConfig);
      },
    });
    return mount(component, options);
  };
}
