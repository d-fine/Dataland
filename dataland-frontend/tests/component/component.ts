import { routes } from "@/router";
import PrimeVue from "primevue/config";
import { createMemoryHistory, createRouter, type Router } from "vue-router";
import { mount } from "cypress/vue";
import type Keycloak from "keycloak-js";
import { ApiClientProvider } from "@/services/ApiClients";

type MountParams = Parameters<typeof mount>;
type OptionsParam = MountParams[1] & { router?: Router; keycloak?: Keycloak };

declare global {
  namespace Cypress {
    interface Chainable {
      mount(component: any, options?: OptionsParam): Chainable<any>;
    }
  }
}

Cypress.Commands.add('mount', (component, options = {}) => {
  options.global = options.global || {};
  options.global.plugins = options.global.plugins || [];
  options.global.provide = options.global.provide || {};
  options.global.plugins.push(PrimeVue);

  if (!options.router) {
    options.router = createRouter({
      routes: routes,
      history: createMemoryHistory(),
    });
  }

  if (options.keycloak) {
    options.global.provide.apiClientProvider = new ApiClientProvider(Promise.resolve(options.keycloak));
    options.global.provide.getKeycloakPromise = (): Promise<Keycloak> => {
      return Promise.resolve(options.keycloak as Keycloak);
    };
    options.global.provide.authenticated = options.keycloak.authenticated;
  }

  options.global.plugins.push({
    install(app) {
      app.use(options.router);
    },
  });

  return mount(component, options);
});

/*
Cypress.Commands.add('mount', (component, options = {}) => {
//function mountWithPlugins(component: any, options: OptionsParam): Chainable<any> {
  options.global = options.global || {};
  options.global.plugins = options.global.plugins || [];
  options.global.provide = options.global.provide || {};
  options.global.plugins.push(PrimeVue);

  if (!options.router) {
    options.router = createRouter({
      routes: routes,
      history: createMemoryHistory(),
    });
  }

  if (options.keycloak) {
    options.global.provide.apiClientProvider = new ApiClientProvider(Promise.resolve(options.keycloak));
    options.global.provide.getKeycloakPromise = (): Promise<Keycloak> => {
      return Promise.resolve(options.keycloak as Keycloak);
    };
    options.global.provide.authenticated = options.keycloak.authenticated;
  }

  options.global.plugins.push({
    install(app) {
      app.use(options.router);
    },
  });

  return mount(component, options);
})
*/
//Cypress.Commands.add("mountWithPlugins", mountWithPlugins);
