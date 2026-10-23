import { minimalKeycloakMock } from '@ct/testUtils/Keycloak.ts';
import AlignedActivitiesFormField from '@/components/resources/dataTable/modals/AlignedActivitiesFormField.vue';

/**
 * Mounts the component with the given extended data point object
 * @param extendedDataPointObject the initial data point
 */
function mountComponent(extendedDataPointObject: object): void {
  //@ts-ignore
  cy.intercept('GET', '**/documents/**', { statusCode: 200, body: [] });
  //@ts-ignore
  cy.mountWithPlugins(AlignedActivitiesFormField, {
    keycloak: minimalKeycloakMock({}),
    global: {
      provide: { companyId: 'company-id', getKeycloakPromise: () => Promise.resolve(minimalKeycloakMock({})) },
    },
    props: { extendedDataPointObject },
  }).then((mounted: { wrapper: { vm: unknown } }) => {
    cy.wrap(mounted.wrapper.vm).as('component');
  });
}

describe('Component test for AlignedActivitiesFormField', () => {
  const row = { activityName: 'Afforestation', naceCodes: ['A2'], share: { relativeShareInPercent: 10 } };

  it('renders the activity read-only and the expected columns', () => {
    mountComponent({ value: [row], quality: 'Reported', comment: '' });
    cy.get('[data-test="activity-name"]').should('have.text', 'Afforestation');
    cy.get('[data-test="activity-select"]').should('not.exist');
    cy.get('[data-test="percent-share.relativeShareInPercent"]').should('exist');
    cy.get('[data-test="yesno-minimumSafeguards"]').should('exist');
    cy.get('[data-test="yesno-dnshToClimateChangeMitigation"]').should('exist');
    cy.get('[data-test="percent-substantialContributionToClimateChangeMitigationInPercent"]').should('exist');
  });

  it('adds a row via the select and deletes rows', () => {
    mountComponent({ value: [row], quality: 'Reported', comment: '' });
    cy.get('[data-test="add-activity-select"]').click();
    cy.get('.p-select-option').first().click();
    cy.get('[data-test="activity-name"]').should('have.length', 2);
    cy.get('[data-test="delete-activity"]').last().click();
    cy.get('[data-test="activity-name"]').should('have.length', 1);
  });

  it('returns the list in the api body', () => {
    mountComponent({ value: [row], quality: 'Reported', comment: '' });
    cy.get('@component').then((component: { buildApiBodyWithExtendedInfo: () => string }) => {
      const body = JSON.parse(component.buildApiBodyWithExtendedInfo());
      expect(body.value).to.have.length(1);
      expect(body.value[0].activityName).to.equal('Afforestation');
    });
  });
});
