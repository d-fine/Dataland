// @ts-nocheck
import CurrencyDataPointFormField from '@/components/forms/parts/fields/CurrencyDataPointFormField.vue';
import { selectItemFromDropdownByValue } from '@sharedUtils/Dropdown';

describe('Component test for CurrencyDataPointFormField', () => {
  /**
   * Ensures that the data quality field exists and does not have an asterisk, showing that it is not mandatory
   */
  function validateDataQualityField(): void {
    cy.get('div[data-test="dataQuality"] div[name="quality"]').find('span').should('have.text', 'Data quality');
    cy.get('div[data-test="dataQuality"] .form-field-label span.asterisk').should('not.exist');
  }

  it("Quality field should be 'Data quality' if the value field has no value and currency field should work as expected", () => {
    cy.mountWithPlugins(CurrencyDataPointFormField, {}).then(() => {
      cy.get('[data-test="dataPointToggleButton"]').click();
      validateDataQualityField();
      cy.get('input[name="value"]').should('be.visible').type('123');
      selectItemFromDropdownByValue(cy.get('div[name="currency"]').should('be.visible'), 'EUR', true);
    });
  });
});
