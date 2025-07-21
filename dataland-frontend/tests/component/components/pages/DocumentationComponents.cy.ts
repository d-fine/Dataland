/**
 * Integration test suite for all documentation components
 * This file runs cross-component tests to ensure consistency and integration
 */

import FrameworkDocumentation from '@/components/pages/FrameworkDocumentation.vue';
import DataPointBaseTypeDocumentation from '@/components/pages/DataPointBaseTypeDocumentation.vue';
import DataPointTypeDocumentation from '@/components/pages/DataPointTypeDocumentation.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { mockFetchResponses } from '@ct/testUtils/DocumentationFixtures';

describe('Documentation Components Integration Tests', () => {
  beforeEach(() => {
    mockFetchResponses();
  });

  describe('Cross-Component Consistency', () => {
    it('should have consistent loading states across all components', () => {
      const components = [
        FrameworkDocumentation,
        DataPointBaseTypeDocumentation,
        DataPointTypeDocumentation,
      ];

      components.forEach((Component, index) => {
        const componentProps = index === 0 
          ? { frameworkId: 'sfdr' } 
          : index === 1 
          ? { baseTypeId: 'extendedCurrency' }
          : { dataPointTypeId: 'extendedDecimalScope1GhgEmissionsInTonnes' };
        
        // @ts-ignore
        cy.mountWithPlugins(Component, {
          props: componentProps,
          keycloak: minimalKeycloakMock({ authenticated: false }),
        });

        // Check loading state
        cy.get('.pi-spinner').should('exist');
        cy.contains('Loading specification data...').should('be.visible');

        // Wait for respective API call
        if (index === 0) cy.wait('@getSfdrFrameworkSpec');
        if (index === 1) cy.wait('@getExtendedCurrencySpec');
        if (index === 2) cy.wait('@getExtendedDecimalSpec');

        // Check loaded state
        cy.get('.pi-spinner').should('not.exist');
        cy.get('.specification-content').should('exist');
      });
    });

    it('should have consistent page structure across all components', () => {
      const components = [
        { component: FrameworkDocumentation, waitAlias: '@getSfdrFrameworkSpec', props: { frameworkId: 'sfdr' } },
        { component: DataPointBaseTypeDocumentation, waitAlias: '@getExtendedCurrencySpec', props: { baseTypeId: 'extendedCurrency' } },
        { component: DataPointTypeDocumentation, waitAlias: '@getExtendedDecimalSpec', props: { dataPointTypeId: 'extendedDecimalScope1GhgEmissionsInTonnes' } },
      ];

      components.forEach(({ component: Component, waitAlias, props }) => {
        // @ts-ignore
        cy.mountWithPlugins(Component, {
          props,
          keycloak: minimalKeycloakMock({ authenticated: false }),
        });

        cy.wait(waitAlias);

        // Common structure elements
        cy.get('header').should('exist');
        cy.get('main[role="main"]').should('exist');
        cy.get('footer').should('exist');
        cy.get('[data-cy="back-button"]').should('exist');
        cy.get('h1').should('exist').and('be.visible');
        cy.get('.surface-card').should('have.length', 2); // Details + Schema/Example
      });
    });

    it('should have consistent error handling across all components', () => {
      // Mock all API calls to return errors
      cy.intercept('GET', '/specs/frameworks/sfdr.json', {
        statusCode: 500,
        body: { error: 'Server Error' },
      }).as('getSfdrError');

      cy.intercept('GET', '/specs/dataPointBaseTypes/extendedCurrency.json', {
        statusCode: 404,
        body: { error: 'Not Found' },
      }).as('getCurrencyError');

      cy.intercept('GET', '/specs/dataPointBaseTypes/extendedDecimalScope1GhgEmissionsInTonnes.json', {
        statusCode: 503,
        body: { error: 'Service Unavailable' },
      }).as('getDecimalError');

      const components = [
        { component: FrameworkDocumentation, waitAlias: '@getSfdrError', errorText: '500', props: { frameworkId: 'sfdr' } },
        { component: DataPointBaseTypeDocumentation, waitAlias: '@getCurrencyError', errorText: '404', props: { baseTypeId: 'extendedCurrency' } },
        { component: DataPointTypeDocumentation, waitAlias: '@getDecimalError', errorText: '503', props: { dataPointTypeId: 'extendedDecimalScope1GhgEmissionsInTonnes' } },
      ];

      components.forEach(({ component: Component, waitAlias, errorText, props }) => {
        // @ts-ignore
        cy.mountWithPlugins(Component, {
          props,
          keycloak: minimalKeycloakMock({ authenticated: false }),
        });

        cy.wait(waitAlias);

        // Check error display
        cy.get('[data-cy="error-message"]').should('exist');
        cy.contains('Error loading specification data').should('be.visible');
        cy.contains(errorText).should('be.visible');
        cy.get('.specification-content').should('not.exist');
      });
    });
  });

  describe('Shared Component Usage', () => {
    it('should use BaseDocumentationLayout consistently', () => {
      const components = [
        { component: FrameworkDocumentation, waitAlias: '@getSfdrFrameworkSpec', props: { frameworkId: 'sfdr' } },
        { component: DataPointBaseTypeDocumentation, waitAlias: '@getExtendedCurrencySpec', props: { baseTypeId: 'extendedCurrency' } },
        { component: DataPointTypeDocumentation, waitAlias: '@getExtendedDecimalSpec', props: { dataPointTypeId: 'extendedDecimalScope1GhgEmissionsInTonnes' } },
      ];

      components.forEach(({ component: Component, waitAlias, props }) => {
        // @ts-ignore
        cy.mountWithPlugins(Component, {
          props,
          keycloak: minimalKeycloakMock({ authenticated: false }),
        });

        cy.wait(waitAlias);

        // Check that base layout elements are present
        cy.get('.static-content').should('exist');
        cy.get('main').should('have.css', 'margin-top').and('not.equal', '0px');
        cy.get('main').should('have.css', 'margin-bottom').and('not.equal', '0px');
      });
    });

    it('should use SpecificationDetails component consistently', () => {
      const components = [
        { component: FrameworkDocumentation, waitAlias: '@getSfdrFrameworkSpec', props: { frameworkId: 'sfdr' } },
        { component: DataPointBaseTypeDocumentation, waitAlias: '@getExtendedCurrencySpec', props: { baseTypeId: 'extendedCurrency' } },
        { component: DataPointTypeDocumentation, waitAlias: '@getExtendedDecimalSpec', props: { dataPointTypeId: 'extendedDecimalScope1GhgEmissionsInTonnes' } },
      ];

      components.forEach(({ component: Component, waitAlias, props }) => {
        // @ts-ignore
        cy.mountWithPlugins(Component, {
          props,
          keycloak: minimalKeycloakMock({ authenticated: false }),
        });

        cy.wait(waitAlias);

        // Check specification details structure
        cy.get('.surface-card').first().within(() => {
          cy.get('h2').should('exist');
          cy.get('.specification-details').should('exist');
          cy.get('.specification-field').should('have.length.greaterThan', 0);
        });
      });
    });

    it('should use SchemaRenderer component consistently', () => {
      const components = [
        { component: FrameworkDocumentation, waitAlias: '@getSfdrFrameworkSpec', props: { frameworkId: 'sfdr' } },
        { component: DataPointBaseTypeDocumentation, waitAlias: '@getExtendedCurrencySpec', props: { baseTypeId: 'extendedCurrency' } },
        { component: DataPointTypeDocumentation, waitAlias: '@getExtendedDecimalSpec', props: { dataPointTypeId: 'extendedDecimalScope1GhgEmissionsInTonnes' } },
      ];

      components.forEach(({ component: Component, waitAlias, props }) => {
        // @ts-ignore
        cy.mountWithPlugins(Component, {
          props,
          keycloak: minimalKeycloakMock({ authenticated: false }),
        });

        cy.wait(waitAlias);

        // Check schema/example section structure
        cy.get('.surface-card').last().within(() => {
          cy.get('h3').should('exist');
          cy.get('[data-cy="schema-container"], .example-container, .schema-container').should('exist');
        });
      });
    });
  });

  describe('Responsive Behavior Consistency', () => {
    it('should handle mobile viewport consistently across components', () => {
      const components = [
        { component: FrameworkDocumentation, waitAlias: '@getSfdrFrameworkSpec', props: { frameworkId: 'sfdr' } },
        { component: DataPointBaseTypeDocumentation, waitAlias: '@getExtendedCurrencySpec', props: { baseTypeId: 'extendedCurrency' } },
        { component: DataPointTypeDocumentation, waitAlias: '@getExtendedDecimalSpec', props: { dataPointTypeId: 'extendedDecimalScope1GhgEmissionsInTonnes' } },
      ];

      components.forEach(({ component: Component, waitAlias, props }) => {
        cy.viewport(375, 667); // Mobile viewport

        // @ts-ignore
        cy.mountWithPlugins(Component, {
          props,
          keycloak: minimalKeycloakMock({ authenticated: false }),
        });

        cy.wait(waitAlias);

        // Check mobile responsiveness
        cy.get('.specification-content').should('be.visible');
        cy.get('h1').should('be.visible');
        cy.get('.surface-card').should('be.visible');
      });
    });

    it('should handle desktop viewport consistently across components', () => {
      const components = [
        { component: FrameworkDocumentation, waitAlias: '@getSfdrFrameworkSpec', props: { frameworkId: 'sfdr' } },
        { component: DataPointBaseTypeDocumentation, waitAlias: '@getExtendedCurrencySpec', props: { baseTypeId: 'extendedCurrency' } },
        { component: DataPointTypeDocumentation, waitAlias: '@getExtendedDecimalSpec', props: { dataPointTypeId: 'extendedDecimalScope1GhgEmissionsInTonnes' } },
      ];

      components.forEach(({ component: Component, waitAlias, props }) => {
        cy.viewport(1920, 1080); // Desktop viewport

        // @ts-ignore
        cy.mountWithPlugins(Component, {
          props,
          keycloak: minimalKeycloakMock({ authenticated: false }),
        });

        cy.wait(waitAlias);

        // Check desktop layout
        cy.get('.specification-content').should('be.visible');
        cy.get('.lg\\:w-8').should('exist'); // PrimeVue responsive classes
      });
    });
  });

  describe('Accessibility Consistency', () => {
    it('should have consistent heading hierarchy across components', () => {
      const components = [
        { component: FrameworkDocumentation, waitAlias: '@getSfdrFrameworkSpec', props: { frameworkId: 'sfdr' } },
        { component: DataPointBaseTypeDocumentation, waitAlias: '@getExtendedCurrencySpec', props: { baseTypeId: 'extendedCurrency' } },
        { component: DataPointTypeDocumentation, waitAlias: '@getExtendedDecimalSpec', props: { dataPointTypeId: 'extendedDecimalScope1GhgEmissionsInTonnes' } },
      ];

      components.forEach(({ component: Component, waitAlias, props }) => {
        // @ts-ignore
        cy.mountWithPlugins(Component, {
          props,
          keycloak: minimalKeycloakMock({ authenticated: false }),
        });

        cy.wait(waitAlias);

        // Check heading hierarchy
        cy.get('h1').should('have.length', 1);
        cy.get('h2').should('have.length', 1);
        cy.get('h3').should('have.length', 1);
      });
    });

    it('should have consistent semantic markup across components', () => {
      const components = [
        { component: FrameworkDocumentation, waitAlias: '@getSfdrFrameworkSpec' },
        { component: DataPointBaseTypeDocumentation, waitAlias: '@getExtendedCurrencySpec' },
        { component: DataPointTypeDocumentation, waitAlias: '@getExtendedDecimalSpec' },
      ];

      components.forEach(({ component: Component, waitAlias }) => {
        // @ts-ignore
        cy.mountWithPlugins(Component, {
          props: { frameworkId: 'sfdr', baseTypeId: 'extendedCurrency', dataPointTypeId: 'extendedDecimalScope1GhgEmissionsInTonnes' },
          keycloak: minimalKeycloakMock({ authenticated: false }),
        });

        cy.wait(waitAlias);

        // Check semantic elements
        cy.get('main[role="main"]').should('exist');
        cy.get('header').should('exist');
        cy.get('footer').should('exist');
        cy.get('label, .field-label').should('exist');
      });
    });
  });

  describe('Performance Consistency', () => {
    it('should load within acceptable time limits across components', () => {
      const components = [
        { component: FrameworkDocumentation, waitAlias: '@getSfdrFrameworkSpec' },
        { component: DataPointBaseTypeDocumentation, waitAlias: '@getExtendedCurrencySpec' },
        { component: DataPointTypeDocumentation, waitAlias: '@getExtendedDecimalSpec' },
      ];

      components.forEach(({ component: Component, waitAlias }) => {
        const startTime = Date.now();

        // @ts-ignore
        cy.mountWithPlugins(Component, {
          props: { frameworkId: 'sfdr', baseTypeId: 'extendedCurrency', dataPointTypeId: 'extendedDecimalScope1GhgEmissionsInTonnes' },
          keycloak: minimalKeycloakMock({ authenticated: false }),
        });

        cy.wait(waitAlias);

        cy.get('.specification-content', { timeout: 5000 }).should('be.visible').then(() => {
          const loadTime = Date.now() - startTime;
          expect(loadTime).to.be.lessThan(5000); // Should load within 5 seconds
        });
      });
    });
  });

  describe('CSS Consistency', () => {
    it('should use consistent styling across components', () => {
      const components = [
        { component: FrameworkDocumentation, waitAlias: '@getSfdrFrameworkSpec' },
        { component: DataPointBaseTypeDocumentation, waitAlias: '@getExtendedCurrencySpec' },
        { component: DataPointTypeDocumentation, waitAlias: '@getExtendedDecimalSpec' },
      ];

      components.forEach(({ component: Component, waitAlias }) => {
        // @ts-ignore
        cy.mountWithPlugins(Component, {
          props: { frameworkId: 'sfdr', baseTypeId: 'extendedCurrency', dataPointTypeId: 'extendedDecimalScope1GhgEmissionsInTonnes' },
          keycloak: minimalKeycloakMock({ authenticated: false }),
        });

        cy.wait(waitAlias);

        // Check consistent CSS classes and styling
        cy.get('.surface-card').should('exist');
        cy.get('.specification-field').should('exist');
        cy.get('.field-label').should('have.css', 'font-weight');
        cy.get('.field-value').should('exist');
      });
    });
  });
});