import { minimalKeycloakMock } from '@ct/testUtils/Keycloak.ts';
import EligibleOrAlignedActivitiesFormField from '@/components/resources/dataTable/modals/EligibleOrAlignedActivitiesFormField.vue';

const activities = [
  { activityName: 'Afforestation', naceCodes: ['A2'], share: { relativeShareInPercent: 10 } },
  { activityName: 'ForestManagement', naceCodes: ['A2'], enablingActivity: 'Yes' },
];

/**
 * Mounts the component with the given activities
 * @param value the initial activities
 */
function mountComponent(value: object[] = activities): void {
  //@ts-ignore
  cy.intercept('GET', '**/documents/**', { statusCode: 200, body: [] });
  //@ts-ignore
  cy.mountWithPlugins(EligibleOrAlignedActivitiesFormField, {
    keycloak: minimalKeycloakMock({}),
    global: {
      provide: { companyId: 'company-id', getKeycloakPromise: () => Promise.resolve(minimalKeycloakMock({})) },
    },
    props: { extendedDataPointObject: { value, quality: 'Reported', comment: '' } },
  }).then((mounted: { wrapper: { vm: unknown } }) => {
    cy.wrap(mounted.wrapper.vm).as('component');
  });
}

describe('Component test for EligibleOrAlignedActivitiesFormField', () => {
  it('renders all existing activities and the add controls', () => {
    mountComponent();
    cy.get('[data-test="activity-select"]').should('have.length', 2);
    cy.get('[data-test="currency-select"]').should('have.length', 2);
  });

  it('removes a row via the trash icon and appends an empty row via add', () => {
    mountComponent();
    cy.get('[data-test="delete-activity"]').first().click();
    cy.get('[data-test="activity-select"]').should('have.length', 1);
    cy.get('[data-test="add-activity"]').click();
    cy.get('[data-test="activity-select"]').should('have.length', 2);
    cy.get('[data-test="nace-select"]').last().should('have.class', 'p-disabled');
  });

  it('blocks saving when an activity is missing', () => {
    mountComponent([...activities, {}]);
    cy.get('@component').its('isPageValid').should('equal', false);
  });

  it('returns the list in the api body', () => {
    mountComponent();
    cy.get('@component').then((component: { buildApiBodyWithExtendedInfo: () => string }) => {
      const body = JSON.parse(component.buildApiBodyWithExtendedInfo());
      expect(body.value).to.have.length(2);
      expect(body.value[0].activityName).to.equal('Afforestation');
    });
  });
});
