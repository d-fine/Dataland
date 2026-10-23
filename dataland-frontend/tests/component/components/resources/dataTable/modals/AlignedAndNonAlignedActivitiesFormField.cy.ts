import { mountEditFormField } from '@ct/testUtils/MountEditFormField.ts';
import AlignedActivitiesFormField from '@/components/resources/dataTable/modals/AlignedActivitiesFormField.vue';
import NonAlignedActivitiesFormField from '@/components/resources/dataTable/modals/NonAlignedActivitiesFormField.vue';

const row = { activityName: 'Afforestation', naceCodes: ['A2'], share: { relativeShareInPercent: 10 } };
const dataPoint = { value: [row], quality: 'Reported', comment: '' };

const variants = [
  {
    name: 'AlignedActivitiesFormField',
    component: AlignedActivitiesFormField,
    expectsYesNoColumns: true,
  },
  {
    name: 'NonAlignedActivitiesFormField',
    component: NonAlignedActivitiesFormField,
    expectsYesNoColumns: false,
  },
];

variants.forEach(({ name, component, expectsYesNoColumns }) => {
  describe(`Component test for ${name}`, () => {
    it('renders the activity read-only and the expected columns', () => {
      mountEditFormField(component, dataPoint);
      cy.get('[data-test="activity-name"]').should('have.text', 'Afforestation');
      cy.get('[data-test="activity-select"]').should('not.exist');
      cy.get('[data-test="percent-share.relativeShareInPercent"]').should('exist');
      if (expectsYesNoColumns) {
        cy.get('[data-test="yesno-minimumSafeguards"]').should('exist');
        cy.get('[data-test="yesno-dnshToClimateChangeMitigation"]').should('exist');
        cy.get('[data-test="percent-substantialContributionToClimateChangeMitigationInPercent"]').should('exist');
      } else {
        cy.get('[data-test^="yesno-"]').should('not.exist');
      }
    });

    it('adds a row via the select and deletes rows', () => {
      mountEditFormField(component, dataPoint);
      cy.get('[data-test="add-activity-select"]').click();
      cy.get('.p-select-option').first().click();
      cy.get('[data-test="activity-name"]').should('have.length', 2);
      cy.get('[data-test="delete-activity"]').last().click();
      cy.get('[data-test="activity-name"]').should('have.length', 1);
    });

    it('returns the list in the api body', () => {
      mountEditFormField(component, dataPoint);
      cy.get('@component').then((instance: { buildApiBodyWithExtendedInfo: () => string }) => {
        const body = JSON.parse(instance.buildApiBodyWithExtendedInfo());
        expect(body.value).to.have.length(1);
        expect(body.value[0].activityName).to.equal('Afforestation');
      });
    });
  });
});
