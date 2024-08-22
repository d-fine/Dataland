// @ts-nocheck
import BigDecimalExtendedDataPointFormField from '@/components/forms/parts/fields/BigDecimalExtendedDataPointFormField.vue';

describe('Component test for DataPointFormField', () => {
  it('Currency field should not visible when unit is hardcoded and Quality field should have no value if the value field has value', () => {
    cy.mountWithPlugins(BigDecimalExtendedDataPointFormField, {}).then((mounted) => {
      void mounted.wrapper.setProps({
        unit: 'Days',
      });
      cy.get('[data-test="dataPointToggleButton"]').click();
      cy.get("input[name='value']").type('1234');
      cy.get('div[name="currency"]').should('not.exist');
      cy.get('div[data-test="dataQuality"] div[name="quality"]').find('span').should('not.have.class', 'p-disabled');
      cy.get('div[data-test="dataQuality"] .form-field-label span.asterisk').should('not.exist');
    });
  });
});
