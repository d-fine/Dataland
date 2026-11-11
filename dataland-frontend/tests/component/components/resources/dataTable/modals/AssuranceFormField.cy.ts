import { mountEditFormField } from '@ct/testUtils/MountEditFormField.ts';
import AssuranceFormField from '@/components/resources/dataTable/modals/AssuranceFormField.vue';

/**
 * @param extendedDataPointObject the initial data point
 */
function mountComponent(extendedDataPointObject: object): void {
  mountEditFormField(AssuranceFormField, extendedDataPointObject);
}

describe('Component test for AssuranceFormField', () => {
  it('prefills level, provider and comment from the data point', () => {
    mountComponent({ value: 'Limited Assurance', provider: 'Deloitte', comment: 'Section 4' });
    cy.get('[data-test="assurance-provider-input"]').should('have.value', 'Deloitte');
    cy.get('[data-test="assurance-comment-input"]').should('have.value', 'Section 4');
    cy.get('@component').then((component: { buildApiBodyWithExtendedInfo: () => string; isPageValid: boolean }) => {
      expect(JSON.parse(component.buildApiBodyWithExtendedInfo())).to.deep.equal({
        value: 'LimitedAssurance',
        provider: 'Deloitte',
        comment: 'Section 4',
      });
      expect(JSON.parse(component.buildApiBodyWithExtendedInfo())).to.not.have.property('quality');
      expect(component.isPageValid).to.equal(true);
    });
  });

  it('sends an edited comment and provider', () => {
    mountComponent({ value: 'Reasonable Assurance', provider: 'KPMG' });
    cy.get('[data-test="assurance-comment-input"]').type('New comment');
    cy.get('[data-test="assurance-provider-input"]').clear().type('EY');
    cy.get('@component').then((component: { buildApiBodyWithExtendedInfo: () => string }) => {
      expect(JSON.parse(component.buildApiBodyWithExtendedInfo())).to.deep.equal({
        value: 'ReasonableAssurance',
        provider: 'EY',
        comment: 'New comment',
      });
    });
  });

  it('handles legacy data without comment and sends exactly value, provider and no quality', () => {
    mountComponent({ value: 'Limited Assurance', provider: 'Deloitte' });
    cy.get('[data-test="assurance-comment-input"]').should('have.value', '');
    cy.get('@component').then((component: { buildApiBodyWithExtendedInfo: () => string }) => {
      const body = JSON.parse(component.buildApiBodyWithExtendedInfo()) as Record<string, unknown>;
      expect(body).to.deep.equal({ value: 'LimitedAssurance', provider: 'Deloitte' });
      expect(body).to.not.have.property('quality');
    });
  });

  it('keeps provider and comment when only the level is changed', () => {
    mountComponent({ value: 'Limited Assurance', provider: 'Deloitte', comment: 'Section 4' });
    cy.get('[data-test="assurance-provider-input"]').should('have.value', 'Deloitte');
    cy.get('@component').then((component: { buildApiBodyWithExtendedInfo: () => string }) => {
      const body = JSON.parse(component.buildApiBodyWithExtendedInfo()) as Record<string, unknown>;
      expect(body.provider).to.equal('Deloitte');
      expect(body.comment).to.equal('Section 4');
    });
  });

  it('is invalid without an assurance level', () => {
    mountComponent({ value: undefined });
    cy.get('@component').its('isPageValid').should('equal', false);
  });
});
