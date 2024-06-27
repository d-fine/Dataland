import type Keycloak from 'keycloak-js';
import type { Router } from 'vue-router';
import { mount } from 'cypress/vue';
import { createPinia } from 'pinia';
import PrimeVue from 'primevue/config';
import DialogService from 'primevue/dialogservice';
import { createMemoryHistory, createRouter } from 'vue-router';
import { routes } from '@/router';
import { ApiClientProvider } from '@/services/ApiClients';
import { assertDefined } from '@/utils/TypeScriptUtils';
import { defaultConfig, plugin } from '@formkit/vue';
import { defineComponent, h } from 'vue';
import DynamicDialog from 'primevue/dynamicdialog';

interface DatalandMountOptions {
  /**
   * They global keycloak injection. Used to configure authentication for any API calls.
   */
  keycloak?: Keycloak;
  /**
   * The router to use. If no router is provided, a memory router is used.
   */
  router?: Router;
  /**
   * If set to true, the component is mounted with a dialog wrapper containing the component.
   * WARNING: This implies that props will not be passed to the component directly, but to the wrapper.
   */
  dialogOptions?: {
    mountWithDialog: true;
    propsToPassToTheMountedComponent?: object;
  };
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
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  return (component: any, options: any) => {
    if (!options) {
      options = {};
    }
    options.global = options.global ?? {};
    options.global.stubs = options.global.stubs ?? {};
    options.global.plugins = options.global.plugins ?? [];
    options.global.plugins.push(createPinia());
    options.global.plugins.push(PrimeVue);
    options.global.plugins.push(DialogService);
    options.global.provide = options.global.provide ?? {};

    let componentForMounting = component;
    if (additionalOptions.dialogOptions) {
      Object.assign(options.global.stubs, { transition: false });
      componentForMounting = defineComponent({
        render() {
          return [
            h(DynamicDialog),
            h(component, additionalOptions.dialogOptions?.propsToPassToTheMountedComponent ?? {}),
          ];
        },
      });
    }

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
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      install(app: any) {
        app.use(assertDefined(options.router));
      },
    });

    options.global.plugins.push({
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      install(app: any) {
        app.use(plugin, defaultConfig);
      },
    });

    return mount(componentForMounting, options);
  };
}
