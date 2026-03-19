import LandingPage from '@/components/pages/LandingPage.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';

describe('Component test for the landing page', () => {
  it('Check if essential elements are present', () => {
    cy.mountWithPlugins(LandingPage, {
      keycloak: minimalKeycloakMock({
        authenticated: false,
      }),
    }).then(() => {
      validateIntroSection();
      validateFindLeiSection();
      validateWhyUsSection();
      validateFrameworksSection();
      validateTestimonialsSection();
    });
  });
});

/**
 * Validates the elements of the intro section
 */
function validateIntroSection(): void {
  cy.get('.intro').should('exist');
  cy.get('h1').should('contain.text', 'Democratizing access to high-quality sustainability data');
  cy.get('h1').should('contain.text', 'A European non-profit shared data platform');
  cy.get('[data-test="hero-register-button"]').should('exist');
}

/**
 * Validates the Find LEI section with search bar
 */
function validateFindLeiSection(): void {
  cy.get('.find-lei').should('exist');
  cy.get('.find-lei__heading').should('contain.text', 'Search sustainability data by company name or identifier');
}

/**
 * Validates the Why Us section with problem-solution blocks
 */
function validateWhyUsSection(): void {
  cy.get('.why-us').should('exist');
  cy.get('.why-us__heading').should('contain.text', 'Common challenges in sustainability data procurement');
}

/**
 * Validates the frameworks section with 6 framework cards
 */
function validateFrameworksSection(): void {
  cy.get('.frameworks').should('exist');
  cy.get('.framework-card').should('have.length', 6);
}

/**
 * Validates the testimonials section
 */
function validateTestimonialsSection(): void {
  cy.get('.testimonials').should('exist');
  cy.get('.testimonials__heading').should('contain.text', 'What our members share about their experience');
}
