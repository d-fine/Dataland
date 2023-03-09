import { routes } from "@/router";
import { createMemoryHistory, createRouter, Router } from "vue-router";
import { mount as cpMount } from "cypress/vue";
import Keycloak from "keycloak-js";

type MountReturn = ReturnType<typeof cpMount>;
type MountParams = Parameters<typeof cpMount>;
// eslint-disable-next-line @typescript-eslint/no-explicit-any
type ComponentParam = any;
// eslint-disable-next-line @typescript-eslint/no-explicit-any
type OptionsParam = MountParams[1] & { router?: Router; keycloak?: Keycloak; propsOverride?: any };

declare global {
  // eslint-disable-next-line @typescript-eslint/no-namespace
  namespace Cypress {
    interface Chainable {
      /**
       * Helper mount function for Vue Components
       *
       * @param component Vue Component or JSX Element to mount
       * @param options Options passed to Vue Test Utils
       */
      mount(component: ComponentParam, options?: OptionsParam): MountReturn;
    }
  }
}

Cypress.Commands.add("mount", (component: ComponentParam, options: OptionsParam = {}) => {
  // Setup options object
  options.global = options.global || {};
  options.global.plugins = options.global.plugins || [];
  options.global.provide = options.global.provide || {};

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

  if (options.propsOverride) {
    // eslint-disable-next-line @typescript-eslint/no-unsafe-assignment
    options.propsData = options.propsOverride;
  }

  options.global.plugins.push({
    install(app) {
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      app.use(options.router);
    },
  });

  return cpMount(component, options);
});
