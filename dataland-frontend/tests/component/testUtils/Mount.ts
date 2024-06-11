import type Keycloak from "keycloak-js";
import type { Router } from "vue-router";
import { mount } from "cypress/vue";
import { createPinia } from "pinia";
import PrimeVue from "primevue/config";
import DialogService from "primevue/dialogservice";
import { createMemoryHistory, createRouter } from "vue-router";
import { routes } from "@/router";
import { ApiClientProvider } from "@/services/ApiClients";
import { assertDefined } from "@/utils/TypeScriptUtils";
import { defaultConfig, plugin } from "@formkit/vue";

interface DatalandMountOptions {
  keycloak?: Keycloak;
  router?: Router;
}
/**
 * A higher-order function that returns a
 * slightly modified version of the vue mount function that automatically initiates plugins used in dataland
 * like PrimeVue, Pinia or the Router and also allows for simple authentication injection
 * @param additionalOptions The Options configuring the returned mounting routine
 * @returns a modified mounting function with the desired properties.
 */
export function getMountingFunction(additionalOptions: DatalandMountOptions = {}): typeof mount {
  /*
    This file defines a alternative mounting function that also includes many creature comforts
    (mounting of plugins, mocking router, mocking keycloak, ....)
    However, the underlying type definition of the mount function is very complex (> 100 LOC).
    Therefore, we decided to create this un-checked meta-function.
  */
  return (component: any, options: any) => {
    options.global = options.global ?? {};
    options.global.stubs = options.global.stubs ?? {};
    options.global.plugins = options.global.plugins ?? [];
    options.global.plugins.push(createPinia());
    options.global.plugins.push(PrimeVue);
    options.global.plugins.push(DialogService);
    options.global.provide = options.global.provide ?? {};
    Object.assign(options.global.stubs, { transition: false });

    if (additionalOptions?.router) {
      options.router = additionalOptions.router;
    } else {
      options.router = createRouter({
        routes: routes,
        history: createMemoryHistory(),
      });
    }

    if (additionalOptions.keycloak) {
      options.global.provide.apiClientProvider = new ApiClientProvider(Promise.resolve(options.keycloak));
      options.global.provide.getKeycloakPromise = (): Promise<Keycloak> => {
        return Promise.resolve(additionalOptions.keycloak as Keycloak);
      };
      options.global.provide.authenticated = additionalOptions.keycloak.authenticated;
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
