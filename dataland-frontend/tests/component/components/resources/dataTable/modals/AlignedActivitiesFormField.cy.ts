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
  it('renders existing activities and the aligned columns', () => {
    mountComponent({
      value: [{ activityName: 'Afforestation', naceCodes: ['A2'], share: { relativeShareInPercent: 10 } }],
      quality: 'Reported',
      comment: '',
    });
    cy.get('[data-test="activity-select"]').should('have.length', 1);
    cy.get('[data-test="percent-share.relativeShareInPercent"]').should('exist');
    cy.get('[data-test="yesno-minimumSafeguards"]').should('exist');
    cy.get('[data-test="yesno-dnshToClimateChangeMitigation"]').should('exist');
    cy.get('[data-test="percent-substantialContributionToClimateChangeMitigationInPercent"]').should('exist');
  });

  it('adds and deletes rows and blocks saving without an activity', () => {
    mountComponent({
      value: [{ activityName: 'Afforestation', naceCodes: ['A2'], share: { relativeShareInPercent: 10 } }],
      quality: 'Reported',
      comment: '',
    });
    cy.get('[data-test="add-activity"]').click();
    cy.get('[data-test="activity-select"]').should('have.length', 2);
    cy.get('@component').its('isPageValid').should('equal', false);
    cy.get('[data-test="delete-activity"]').last().click();
    cy.get('@component').its('isPageValid').should('equal', true);
  });

  it('returns the list in the api body', () => {
    mountComponent({
      value: [{ activityName: 'Afforestation', naceCodes: ['A2'], share: { relativeShareInPercent: 10 } }],
      quality: 'Reported',
      comment: '',
    });
    cy.get('@component').then((component: { buildApiBodyWithExtendedInfo: () => string }) => {
      const body = JSON.parse(component.buildApiBodyWithExtendedInfo());
      expect(body.value).to.have.length(1);
      expect(body.value[0].activityName).to.equal('Afforestation');
    });
  });
});
