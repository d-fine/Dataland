import { minimalKeycloakMock } from '@ct/testUtils/Keycloak.ts';
import RadioButtonsExtendedDataPointFormField from '@/components/resources/dataTable/modals/RadioButtonsExtendedDataPointFormField.vue';

/**
 * Mounts the component with the given extended data point object
 * @param extendedDataPointObject the initial data point
 */
function mountComponent(extendedDataPointObject: object): void {
  //@ts-ignore
  cy.intercept('GET', '**/documents/**', { statusCode: 200, body: [] });
  //@ts-ignore
  cy.mountWithPlugins(RadioButtonsExtendedDataPointFormField, {
    keycloak: minimalKeycloakMock({}),
    global: {
      provide: { companyId: 'company-id', getKeycloakPromise: () => Promise.resolve(minimalKeycloakMock({})) },
    },
    props: { extendedDataPointObject },
  }).then((mounted: { wrapper: { vm: unknown } }) => {
    cy.wrap(mounted.wrapper.vm).as('component');
  });
}

describe('Component test for RadioButtonsExtendedDataPointFormField', () => {
  it('maps the humanized displayed value to the technical enum value', () => {
    mountComponent({ value: 'No Deviation', quality: 'Reported', comment: '' });
    cy.get('@component').then((component: { buildApiBodyWithExtendedInfo: () => string }) => {
      expect(JSON.parse(component.buildApiBodyWithExtendedInfo()).value).to.equal('NoDeviation');
    });
  });

  it('allows switching the selection', () => {
    mountComponent({ value: 'Deviation' });
    cy.get('[data-test="radio-buttons-select"]').contains('No Deviation').click();
    cy.get('@component').then((component: { buildApiBodyWithExtendedInfo: () => string }) => {
      expect(JSON.parse(component.buildApiBodyWithExtendedInfo()).value).to.equal('NoDeviation');
    });
  });
});
