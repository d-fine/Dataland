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
  it('renders existing activities as read-only text with their NACE codes', () => {
    mountComponent();
    cy.get('[data-test="activity-name"]').should('have.length', 2);
    cy.get('[data-test="activity-name"]').first().should('have.text', 'Afforestation');
    cy.get('[data-test="nace-codes"]').first().should('contain.text', 'A2');
    cy.get('[data-test="activity-select"]').should('not.exist');
    cy.get('[data-test="nace-select"]').should('not.exist');
    cy.get('[data-test="currency-select"]').should('have.length', 2);
  });

  it('removes a row via the trash icon and adds a row with prefilled NACE codes via the select', () => {
    mountComponent();
    cy.get('[data-test="delete-activity"]').first().click();
    cy.get('[data-test="activity-name"]').should('have.length', 1);
    cy.get('[data-test="add-activity-select"]').click();
    cy.get('.p-select-option').first().click();
    cy.get('[data-test="activity-name"]').should('have.length', 2);
    cy.get('[data-test="nace-codes"]').last().find('li').should('have.length.greaterThan', 0);
  });

  it('shows quality, data source and the documents link in the footer toolbar', () => {
    mountComponent();
    cy.get('[data-test="quality-select"]').should('exist');
    cy.get('[data-test="document-select"]').should('exist');
    cy.get('[data-test="go-to-documents-link"]').should('have.attr', 'target', '_blank');
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
