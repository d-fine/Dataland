import { minimalKeycloakMock } from '@ct/testUtils/Keycloak.ts';
import DateExtendedDataPointFormField from '@/components/resources/dataTable/modals/DateExtendedDataPointFormField.vue';

/**
 * Mounts the component with the given extended data point object
 * @param extendedDataPointObject the initial data point
 */
function mountComponent(extendedDataPointObject: object): void {
  //@ts-ignore
  cy.intercept('GET', '**/documents/**', { statusCode: 200, body: [] });
  //@ts-ignore
  cy.mountWithPlugins(DateExtendedDataPointFormField, {
    keycloak: minimalKeycloakMock({}),
    global: {
      provide: { companyId: 'company-id', getKeycloakPromise: () => Promise.resolve(minimalKeycloakMock({})) },
    },
    props: { extendedDataPointObject },
  }).then((mounted: { wrapper: { vm: unknown } }) => {
    cy.wrap(mounted.wrapper.vm).as('component');
  });
}

describe('Component test for DateExtendedDataPointFormField', () => {
  it('prefills an existing ISO date and returns it in the api body', () => {
    mountComponent({ value: '2024-12-31', quality: 'Reported', comment: 'c' });
    cy.get('[data-test="date-select"] input').should('have.value', '2024-12-31');
    cy.get('@component').then((component: { buildApiBodyWithExtendedInfo: () => string }) => {
      const body = JSON.parse(component.buildApiBodyWithExtendedInfo());
      expect(body.value).to.equal('2024-12-31');
    });
  });

  it('starts empty for a non-date value', () => {
    mountComponent({ value: 'not a date' });
    cy.get('[data-test="date-select"] input').should('have.value', '');
  });
});
