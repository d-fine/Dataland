// @ts-nocheck
import NaceCodeSelector from '@/components/forms/parts/elements/derived/NaceCodeSelector.vue';

describe('Component tests for the NaceCodeSelector', () => {
  it('Ensure that NACE codes can by search and select and displayed correctly', () => {
    cy.mountWithPlugins(NaceCodeSelector, {}).then(() => {
      cy.get('[data-test="NaceCodeSelectorInput"]').should('be.visible');
      cy.get('[data-test="NaceCodeSelectorInput"]').click();
      cy.get('[data-test="NaceCodeSelectorTree"]').should('be.visible');
      cy.get('[data-test="NaceCodeSelectorTree"]').find('li').should('have.length', 21);

      cy.get('[data-test="NaceCodeSelectorInput"]').type('01.11');
      cy.get('[data-test="NaceCodeSelectorTree"]').find('li').should('have.length', 4).eq(3).should('contain', '01.11');
      cy.get('[data-test="NaceCodeSelectorInput"]').clear().type('62.02');
      cy.get('[data-test="NaceCodeSelectorTree"]')
        .find('li')
        .should('have.length', 4)
        .eq(3)
        .should('contain', 'Computer consultancy activities');

      cy.get('[data-test="NaceCodeSelectorCheckbox"]').eq(3).click();
      cy.get('[data-test="NaceCodeSelectorInput"]').clear();
      cy.get('[data-test="NaceCodeSelectorTree"]').should('be.visible');
      cy.get('[data-test="NaceCodeSelectorTree"] span.p-badge').filter(':contains("1")').should('have.length', 1);
    });
  });
});
