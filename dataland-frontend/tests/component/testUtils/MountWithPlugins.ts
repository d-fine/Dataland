import { routes } from "@/router";
import { createPinia } from "pinia";
import PrimeVue from "primevue/config";
import DialogService from "primevue/dialogservice";
import { plugin, defaultConfig } from "@formkit/vue";
import { createMemoryHistory, createRouter, Router } from "vue-router";
import { mount } from "cypress/vue";
import { VueWrapper } from "@vue/test-utils";
import { DefineComponent } from "vue";
import Keycloak from "keycloak-js";
import { assertDefined } from "@/utils/TypeScriptUtils";

/*
  This file defines a alternative mounting function that also includes many creature comforts
  (mounting of plugins, mocking router, mocking keycloak, ....)
  However, the underlying type definition of the mount function is very complex.
  The no-explicit-any overrides are present as the original mount type definitions also include these anys
 */

// eslint-disable-next-line @typescript-eslint/no-explicit-any
type ComponentMountingOptions<T extends DefineComponent<any, any, any, any, any>> = Parameters<typeof mount<T>>[1] & {
  router?: Router;
  keycloak?: Keycloak;
};

declare global {
  // eslint-disable-next-line @typescript-eslint/no-namespace
  namespace Cypress {
    interface Chainable {
      /**
       * Helper mount function for Vue Components
       * @param component Vue Component or JSX Element to mount
       * @param options Options passed to Vue Test Utils
       */
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      mountWithPlugins<T extends DefineComponent<any, any, any, any, any>>(
        component: T,
        options: ComponentMountingOptions<T>
      ): Cypress.Chainable<{
        wrapper: VueWrapper<InstanceType<T>>;
        component: VueWrapper<InstanceType<T>>["vm"];
      }>;
    }
  }
}

/**
 * A slightly modified version of the vue mount function that automatically initiates plugins used in dataland
 * like PrimeVue, Pinia or the Router and also allows for simple authentication injection
 * @param component the component you want to mount
 * @param options the options for mounting said component
 * @returns a cypress chainable for the mounted wrapper and the Vue component
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
function mountWithPlugins<T extends DefineComponent<any, any, any, any, any>>(
  component: T,
  options: ComponentMountingOptions<T>
): Cypress.Chainable<{
  wrapper: VueWrapper<InstanceType<T>>;
  component: VueWrapper<InstanceType<T>>["vm"];
}> {
  options.global = options.global ?? {};
  options.global.plugins = options.global.plugins ?? [];
  options.global.plugins.push(createPinia());
  options.global.plugins.push(PrimeVue);
  options.global.plugins.push(DialogService);
  options.global.provide = options.global.provide ?? {};

  if (!options.router) {
    options.router = createRouter({
      routes: routes,
      history: createMemoryHistory(),
    });
  }

  if (options.keycloak) {
    options.global.provide.getKeycloakPromise = (): Promise<Keycloak> => {
      return Promise.resolve(options.keycloak as Keycloak);
    };
    options.global.provide.authenticated = true;
  }

  options.global.plugins.push({
    install(app) {
      app.use(assertDefined(options.router));
    },
  });

  options.global.plugins.push({
    install(app) {
      app.use(plugin, defaultConfig);
    },
  });

  /*
    The mount() function returns a VueWrapper different from the VueWrapper exported by "@vue/test-utils"
    but that type is not exported and accessing a non-exported type is non-trivial.
    The VueWrapper component from "@vue/test-utils" is, however, "close enough" to the actual return value for our
    purposes. That is the reason for the ts-ignore and eslint-ignores
   */
  // eslint-disable-next-line @typescript-eslint/ban-ts-comment
  // @ts-ignore
  return mount(component, options);
}

Cypress.Commands.add("mountWithPlugins", mountWithPlugins);
