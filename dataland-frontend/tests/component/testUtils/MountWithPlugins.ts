import { routes } from '@/router';
import { createPinia } from 'pinia';
import PrimeVue from 'primevue/config';
import DialogService from 'primevue/dialogservice';
import { plugin, defaultConfig } from '@formkit/vue';
import { createMemoryHistory, createRouter, type Router } from 'vue-router';
import { mount } from 'cypress/vue';
import { type VueWrapper } from '@vue/test-utils';
import { type DefineComponent, defineComponent, h } from 'vue';
import type Keycloak from 'keycloak-js';
import { assertDefined } from '@/utils/TypeScriptUtils';
import DynamicDialog from 'primevue/dynamicdialog';
import { ApiClientProvider } from '@/services/ApiClients';

/*
  This file defines a alternative mounting function that also includes many creature comforts
  (mounting of plugins, mocking router, mocking keycloak, ....)
  However, the underlying type definition of the mount function is very complex.
  The no-explicit-any overrides are present as the original mount type definitions also include these anys
 */

type MountingOptions = {
  router?: Router;
  keycloak?: Keycloak;
};

// eslint-disable-next-line @typescript-eslint/no-explicit-any
type ComponentMountingOptions<T extends DefineComponent<any, any, any, any, any>> = Parameters<typeof mount<T>>[1] &
  MountingOptions;

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
        component: VueWrapper<InstanceType<T>>['vm'];
      }>;
      /**
       * Helper mount function for Vue Components utilizing the DynamicDialog component
       * @param component Vue Component or JSX Element to mount
       * @param options Options passed to Vue Test Utils
       */
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      mountWithDialog<T extends DefineComponent<any, any, any, any, any>>(
        component: T,
        options: MountingOptions,
        props: object
      ): Cypress.Chainable<{
        wrapper: VueWrapper<InstanceType<T>>;
        component: VueWrapper<InstanceType<T>>['vm'];
      }>;
    }
  }
}

/**
 * A slightly modified version of the vue mount function that automatically initiates plugins used in dataland
 * like PrimeVue, Pinia or the Router and also allows for simple authentication injection
 * @param component the component you want to mount
 * @param options the mountingOptions for mounting said component
 * @returns a cypress chainable for the mounted wrapper and the Vue component
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
function mountWithPlugins<T extends DefineComponent<any, any, any, any, any>>(
  component: T,
  options: ComponentMountingOptions<T>
): Cypress.Chainable<{
  wrapper: VueWrapper<InstanceType<T>>;
  component: VueWrapper<InstanceType<T>>['vm'];
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
    options.global.provide.apiClientProvider = new ApiClientProvider(Promise.resolve(options.keycloak));
    options.global.provide.getKeycloakPromise = (): Promise<Keycloak> => {
      return Promise.resolve(options.keycloak as Keycloak);
    };
    options.global.provide.authenticated = options.keycloak.authenticated;
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

  // @ts-ignore
  return mount(component, options);
}

/**
 * Mounts a component in a wrapper also containing a DynamicDialog. Manipulating the component is only possible through the properties.
 * @param component the component you want to mount
 * @param options general mounting options, stubs are ignored
 * @param props properties to set for the component
 * @returns a cypress chainable for the mounted wrapper and the wrapper of the Vue component
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
function mountWithDialog<T extends DefineComponent<any, any, any, any, any>>(
  component: T,
  options: MountingOptions,
  props: object = {}
): Cypress.Chainable {
  const componentWrapper = defineComponent({
    render() {
      return [h(DynamicDialog), h(component, props ?? {})];
    },
  });
  const wrapperOptions = options as ComponentMountingOptions<typeof componentWrapper>;
  wrapperOptions.global ??= {};
  wrapperOptions.global.stubs ??= {};
  Object.assign(wrapperOptions.global.stubs, { transition: false });
  return mountWithPlugins(componentWrapper, wrapperOptions);
}

Cypress.Commands.add('mountWithPlugins', mountWithPlugins);
Cypress.Commands.add('mountWithDialog', mountWithDialog);
