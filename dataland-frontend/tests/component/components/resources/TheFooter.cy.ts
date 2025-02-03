import TheFooter from '@/components/generics/TheFooter.vue';
import { mount } from 'cypress/vue';

describe('Component test for DatalandFooter', () => {
  it('Check if footer is as expected', () => {
    mount(TheFooter);
    cy.get(`img[alt="Dataland Logo"]`).should('be.visible');
    cy.get('body').should('contain.text', 'Legal');
    cy.get('body').should('contain.text', 'Copyright © 2024 Dataland');
    cy.get('[data-test="imprint"]').should('contain.text', 'Imprint').should('have.attr', 'to', '/imprint');
    cy.get('[data-test="data privacy"]')
      .should('contain.text', 'Data Privacy')
      .should('have.attr', 'to', '/dataprivacy');
  });
});
