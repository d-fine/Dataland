// @ts-nocheck
import IntegerExtendedDataPointFormField from '@/components/forms/parts/fields/IntegerExtendedDataPointFormField.vue';

describe('test IntegerExtendedDataPointFormField for entries', () => {
  it('Form field should exist and entries to form field should be integer and nothing else', () => {
    cy.mountWithPlugins(IntegerExtendedDataPointFormField, {}).then((mounted) => {
      void mounted.wrapper.setProps({
        name: 'foo',
        unit: '',
      });
      cy.get('[data-test="dataPointToggleButton"]').click();
      //check existence
      cy.get('input[name="value"]').should('exist');
      //error on float
      cy.get('input[name="value"]').type('5.6').blur();
      cy.get('.formkit-message').should('contain.text', 'must be an integer');
      //error on letter
      cy.get('input[name="value"]').clear().type('x').blur();
      cy.get('.formkit-message').should('contain.text', 'must be a number');
      //error on empty
      cy.get('input[name="value"]').clear().blur();
      cy.get('.formkit-message').should('not.exist');
      //error on overflow
    });
  });
});
