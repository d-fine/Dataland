import type { Component } from 'vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak.ts';

/**
 * Mounts an edit data point form field with the shared test setup (documents stub, keycloak, company id)
 * and exposes the component instance under the alias "component".
 * @param component the edit form field to mount
 * @param extendedDataPointObject the initial data point
 * @param additionalProvide additional values to provide to the component
 */
export function mountEditFormField(
  component: Component,
  extendedDataPointObject: object,
  additionalProvide: Record<string, unknown> = {}
): void {
  //@ts-ignore
  cy.intercept('GET', '**/documents/**', { statusCode: 200, body: [] });
  //@ts-ignore
  cy.mountWithPlugins(component, {
    keycloak: minimalKeycloakMock({}),
    global: {
      provide: {
        companyId: 'company-id',
        getKeycloakPromise: () => Promise.resolve(minimalKeycloakMock({})),
        ...additionalProvide,
      },
    },
    props: { extendedDataPointObject },
  }).then((mounted: { wrapper: { vm: unknown } }) => {
    cy.wrap(mounted.wrapper.vm).as('component');
  });
}
