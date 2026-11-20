import { mountEditFormField } from '@ct/testUtils/MountEditFormField.ts';
import RadioButtonsExtendedDataPointFormField from '@/components/resources/dataTable/modals/RadioButtonsExtendedDataPointFormField.vue';

/**
 * @param extendedDataPointObject the initial data point
 */
function mountComponent(extendedDataPointObject: object, dataPointTypeId?: string): void {
  mountEditFormField(RadioButtonsExtendedDataPointFormField, extendedDataPointObject, { dataPointTypeId });
}

const fiscalYearTypeId = 'extendedEnumFiscalYearDeviation';

describe('Component test for RadioButtonsExtendedDataPointFormField', () => {
  it('maps the humanized displayed value to the technical enum value', () => {
    mountComponent({ value: 'No Deviation', quality: 'Reported', comment: '' }, fiscalYearTypeId);
    cy.get('@component').then((component: { buildApiBodyWithExtendedInfo: () => string }) => {
      expect(JSON.parse(component.buildApiBodyWithExtendedInfo()).value).to.equal('NoDeviation');
    });
  });

  it('allows switching the selection', () => {
    mountComponent({ value: 'Deviation' }, fiscalYearTypeId);
    cy.get('[data-test="radio-buttons-select"]').contains('No Deviation').click();
    cy.get('@component').then((component: { buildApiBodyWithExtendedInfo: () => string }) => {
      expect(JSON.parse(component.buildApiBodyWithExtendedInfo()).value).to.equal('NoDeviation');
    });
  });

  it('offers Listed and Unlisted for the company exchange status', () => {
    mountComponent({ value: 'Listed' }, 'extendedEnumCompanyExchangeStatus');
    cy.get('[data-test="radio-buttons-select"]').should('contain', 'Listed').and('contain', 'Unlisted');
    cy.get('[data-test="radio-buttons-select"]').should('not.contain', 'Deviation');
    cy.get('[data-test="radio-buttons-select"]').contains('Unlisted').click();
    cy.get('@component').then((component: { buildApiBodyWithExtendedInfo: () => string }) => {
      expect(JSON.parse(component.buildApiBodyWithExtendedInfo()).value).to.equal('Unlisted');
    });
  });

  it('offers Listed and Unlisted for the SFDR company exchange status', () => {
    mountComponent({ value: 'Listed' }, 'extendedEnumSfdrCompanyExchangeStatus');
    cy.get('[data-test="radio-buttons-select"]').should('contain', 'Listed').and('contain', 'Unlisted');
    cy.get('[data-test="radio-buttons-select"]').should('not.contain', 'Deviation');
    cy.get('[data-test="radio-buttons-select"]').contains('Unlisted').click();
    cy.get('@component').then((component: { buildApiBodyWithExtendedInfo: () => string }) => {
      expect(JSON.parse(component.buildApiBodyWithExtendedInfo()).value).to.equal('Unlisted');
    });
  });

  it('falls back to the deviation options if no dataPointTypeId is provided', () => {
    mountComponent({ value: 'Deviation' });
    cy.get('[data-test="radio-buttons-select"]').should('contain', 'No Deviation');
    cy.get('[data-test="radio-buttons-select"]').should('not.contain', 'Listed');
    cy.get('[data-test="radio-buttons-select"]').contains('No Deviation').click();
    cy.get('@component').then((component: { buildApiBodyWithExtendedInfo: () => string }) => {
      expect(JSON.parse(component.buildApiBodyWithExtendedInfo()).value).to.equal('NoDeviation');
    });
  });

  it('has no selection for an empty initial value', () => {
    mountComponent({}, fiscalYearTypeId);
    cy.get('@component').then((component: { buildApiBodyWithExtendedInfo: () => string }) => {
      expect(JSON.parse(component.buildApiBodyWithExtendedInfo()).value).to.equal(undefined);
    });
    cy.get('[data-test="radio-buttons-select"]').contains('Deviation').click();
    cy.get('@component').then((component: { buildApiBodyWithExtendedInfo: () => string }) => {
      expect(JSON.parse(component.buildApiBodyWithExtendedInfo()).value).to.equal('Deviation');
    });
  });
});
