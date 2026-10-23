import { mountEditFormField } from '@ct/testUtils/MountEditFormField.ts';
import AssuranceFormField from '@/components/resources/dataTable/modals/AssuranceFormField.vue';

/**
 * @param extendedDataPointObject the initial data point
 */
function mountComponent(extendedDataPointObject: object): void {
  mountEditFormField(AssuranceFormField, extendedDataPointObject);
}

describe('Component test for AssuranceFormField', () => {
  it('prefills level and provider from the displayed values', () => {
    mountComponent({ value: 'Limited Assurance', comment: 'Assurance provided by Deloitte' });
    cy.get('[data-test="assurance-provider-input"]').should('have.value', 'Deloitte');
    cy.get('@component').then((component: { buildApiBodyWithExtendedInfo: () => string; isPageValid: boolean }) => {
      expect(JSON.parse(component.buildApiBodyWithExtendedInfo())).to.deep.equal({
        value: 'LimitedAssurance',
        provider: 'Deloitte',
      });
      expect(component.isPageValid).to.equal(true);
    });
  });

  it('is invalid without an assurance level', () => {
    mountComponent({ value: undefined });
    cy.get('@component').its('isPageValid').should('equal', false);
  });

  it('does not offer quality or comment inputs', () => {
    mountComponent({ value: 'None' });
    cy.get('[data-test="quality-select"]').should('not.exist');
    cy.get('#comment-input').should('not.exist');
  });
});
