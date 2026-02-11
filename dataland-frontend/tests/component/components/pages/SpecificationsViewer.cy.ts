import SpecificationsViewer from '@/components/pages/SpecificationsViewer.vue';
import type { SimpleFrameworkSpecification, FrameworkSpecification } from '@clients/specificationservice';
import frameworkListFixture from '@testing/data/specifications/framework-list.json';
import lksgFrameworkFixture from '@testing/data/specifications/lksg-framework.json';
import emptyFrameworkFixture from '@testing/data/specifications/empty-framework.json';
import dataPointDetailsFixture from '@testing/data/specifications/datapointdetails.json';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { createRouter, createMemoryHistory } from 'vue-router';
import { routes } from '@/router';

describe('Component tests for SpecificationsViewer page', () => {
  beforeEach(() => {
    // Mock API calls with cy.intercept
    cy.intercept('GET', '/specifications/frameworks', { fixture: 'specifications/framework-list.json' }).as('getFrameworks');
    cy.intercept('GET', '/specifications/frameworks/lksg', { fixture: 'specifications/lksg-framework.json' }).as('getLksgFramework');
    cy.intercept('GET', '/specifications/data-point-types/*', { fixture: 'specifications/datapoint-details.json' }).as('getDataPoint');
  });

  describe('Framework selection flow', () => {
    it('Should show empty state when no framework selected', () => {
      cy.mountWithPlugins(SpecificationsViewer, {
        keycloak: minimalKeycloakMock({}),
      });

      cy.wait('@getFrameworks');
      cy.get('[data-test="specifications-content"]').should('be.visible');
      // After frameworks load, empty state should be visible (no framework selected yet)
      cy.get('.empty-state', { timeout: 10000 }).should('be.visible');
      cy.get('.empty-state').should('contain.text', 'Select a framework');
    });

    it('Should populate framework dropdown after loading', () => {
      cy.mountWithPlugins(SpecificationsViewer, { keycloak: minimalKeycloakMock({}) });

      cy.wait('@getFrameworks');
      cy.get('[data-test="framework-selector"]').should('be.visible');
      
      // Open dropdown - click the dropdown trigger
      cy.get('.framework-select .p-select-dropdown').click();
      
      // Should show framework options in list container
      cy.get('.p-select-list-container', { timeout: 5000 }).should('be.visible');
      cy.get('.p-select-option').should('have.length', 3);
      cy.contains('.p-select-option', 'LkSG').should('exist');
    });

    it('Should load specification when framework selected', () => {
      cy.mountWithPlugins(SpecificationsViewer, { keycloak: minimalKeycloakMock({}) });

      cy.wait('@getFrameworks');
      
      // Select framework
      cy.get('.framework-select .p-select-dropdown').click();
      cy.get('.p-select-list-container').contains('LkSG').click();
      
      cy.wait('@getLksgFramework');
      
      // Metadata panel should be visible
      cy.get('[data-test="framework-metadata"]', { timeout: 10000 }).should('be.visible');
      cy.get('[data-test="framework-metadata"]').should('contain.text', 'LkSG');
    });

    it('Should render schema tree after loading specification', () => {
      cy.mountWithPlugins(SpecificationsViewer, { keycloak: minimalKeycloakMock({}) });

      cy.wait('@getFrameworks');
      
      // Select framework
      cy.get('.framework-select .p-select-dropdown').click();
      cy.get('.p-select-list-container').contains('LkSG').click();
      
      cy.wait('@getLksgFramework');
      
      // Schema tree should be visible
      cy.get('[data-test="section-header"]', { timeout: 10000 }).should('exist');
    });

    it('Should update URL query param when framework selected', () => {
      cy.mountWithPlugins(SpecificationsViewer, { keycloak: minimalKeycloakMock({}) });

      cy.wait('@getFrameworks');
      
      // Select framework
      cy.get('.framework-select .p-select-dropdown').click();
      cy.get('.p-select-list-container').contains('LkSG').click();
      
      // URL should be updated
      cy.location('search', { timeout: 5000 }).should('include', 'framework=lksg');
    });
  });

  describe('Loading states', () => {
    it('Should show spinner in dropdown area while frameworks loading', () => {
      cy.intercept('GET', '/specifications/frameworks', (req) => {
        req.reply({ fixture: 'specifications/framework-list.json', delay: 500 });
      }).as('getFrameworksSlow');

      cy.mountWithPlugins(SpecificationsViewer, { keycloak: minimalKeycloakMock({}) });

      cy.get('.spinner-small').should('be.visible');
      
      cy.wait('@getFrameworksSlow');
      cy.get('.spinner-small').should('not.exist');
    });

    it('Should show loading state while specification loads', () => {
      cy.intercept('GET', '/specifications/frameworks/lksg', (req) => {
        req.reply({ fixture: 'specifications/lksg-framework.json', delay: 500 });
      }).as('getLksgFrameworkSlow');

      cy.mountWithPlugins(SpecificationsViewer, { keycloak: minimalKeycloakMock({}) });

      cy.wait('@getFrameworks');
      
      // Select framework
      cy.get('.framework-select').click();
      cy.contains('.p-select-option', 'LkSG').click();
      cy.get('.p-select-overlay').should('not.exist');
      
      // Loading state should appear
      cy.get('.loading-state').should('be.visible');
      cy.get('.loading-state').should('contain.text', 'Loading framework specification');
      
      cy.wait('@getLksgFrameworkSlow');
      cy.get('.loading-state').should('not.exist');
    });

    it('Should keep dropdown interactive while specification loads', () => {
      cy.intercept('GET', '/specifications/frameworks/lksg', (req) => {
        req.reply({ fixture: 'specifications/lksg-framework.json', delay: 500 });
      }).as('getLksgFrameworkSlow');

      cy.mountWithPlugins(SpecificationsViewer, { keycloak: minimalKeycloakMock({}) });

      cy.wait('@getFrameworks');
      
      // Select framework
      cy.get('.framework-select .p-select-dropdown').click();
      cy.get('.p-select-list-container').contains('LkSG').click();
      
      // Dropdown should still be interactive (not disabled during specification load)
      cy.get('.framework-select').should('not.be.disabled');
    });

    it('Should show batch loading indicator when loading data point details', () => {
      cy.intercept('GET', '/specifications/data-point-types/*', (req) => {
        req.reply({ fixture: 'specifications/datapoint-details.json', delay: 500 });
      }).as('getDataPointSlow');

      cy.mountWithPlugins(SpecificationsViewer, { keycloak: minimalKeycloakMock({}) });

      cy.wait('@getFrameworks');
      
      // Select framework
      cy.get('.framework-select .p-select-dropdown').click();
      cy.get('.p-select-list-container').contains('LkSG').click();
      
      cy.wait('@getLksgFramework');
      
      // Batch loading indicator should appear while loading data point details
      cy.get('.loading-details', { timeout: 10000 }).should('be.visible');
      cy.get('.loading-details').should('contain.text', 'Loading detailed descriptions');
    });
  });

  describe('Error scenarios', () => {
    it('Should show error message when framework list fetch fails', () => {
      cy.intercept('GET', '/specifications/frameworks', { statusCode: 500, body: { message: 'Server error' } }).as('getFrameworksError');

      cy.mountWithPlugins(SpecificationsViewer, { keycloak: minimalKeycloakMock({}) });

      cy.wait('@getFrameworksError');
      cy.get('.error-message').should('be.visible');
      cy.get('.error-message').should('contain.text', 'Failed to load');
    });

    it('Should show retry button for framework list errors', () => {
      cy.intercept('GET', '/specifications/frameworks', { statusCode: 500, body: { message: 'Server error' } }).as('getFrameworksError');

      cy.mountWithPlugins(SpecificationsViewer, { keycloak: minimalKeycloakMock({}) });

      cy.wait('@getFrameworksError');
      cy.contains('button', 'Retry').should('be.visible');
    });

    it('Should retry framework list fetch on retry button click', () => {
      let requestCount = 0;
      cy.intercept('GET', '/specifications/frameworks', (req) => {
        requestCount++;
        if (requestCount === 1) {
          req.reply({ statusCode: 500, body: { message: 'Server error' } });
        } else {
          req.reply({ fixture: 'specifications/framework-list.json' });
        }
      }).as('getFrameworksRetry');

      cy.mountWithPlugins(SpecificationsViewer, { keycloak: minimalKeycloakMock({}) });

      cy.wait('@getFrameworksRetry');
      cy.get('.error-message').should('be.visible');
      
      // Click retry
      cy.contains('button', 'Retry').click();
      
      cy.wait('@getFrameworksRetry');
      cy.get('.error-message').should('not.exist');
      cy.get('[data-test="framework-selector"]').should('be.visible');
    });

    it('Should show error message when specification fetch fails', () => {
      cy.intercept('GET', '/specifications/frameworks/lksg', { statusCode: 500, body: { message: 'Server error' } }).as('getLksgFrameworkError');

      cy.mountWithPlugins(SpecificationsViewer, { keycloak: minimalKeycloakMock({}) });

      cy.wait('@getFrameworks');
      
      // Select framework
      cy.get('.framework-select .p-select-dropdown').click();
      cy.get('.p-select-list-container').contains('LkSG').click();
      
      cy.wait('@getLksgFrameworkError');
      cy.get('.error-message', { timeout: 5000 }).should('be.visible');
    });

    it('Should clear error on successful retry', () => {
      let requestCount = 0;
      cy.intercept('GET', '/specifications/frameworks/lksg', (req) => {
        requestCount++;
        if (requestCount === 1) {
          req.reply({ statusCode: 500, body: { message: 'Server error' } });
        } else {
          req.reply({ fixture: 'specifications/lksg-framework.json' });
        }
      }).as('getLksgFrameworkRetry');

      cy.mountWithPlugins(SpecificationsViewer, { keycloak: minimalKeycloakMock({}) });

      cy.wait('@getFrameworks');
      
      // Select framework
      cy.get('.framework-select .p-select-dropdown').click();
      cy.get('.p-select-list-container').contains('LkSG').click();
      
      cy.wait('@getLksgFrameworkRetry');
      cy.get('.error-message').should('be.visible');
      
      // Click retry
      cy.contains('button', 'Retry').click();
      
      cy.wait('@getLksgFrameworkRetry');
      cy.get('.error-message', { timeout: 5000 }).should('not.exist');
      cy.get('[data-test="framework-metadata"]').should('be.visible');
    });
  });

  describe('Child component integration', () => {
    it('Should pass correct framework prop to FrameworkMetadataPanel', () => {
      cy.mountWithPlugins(SpecificationsViewer, { keycloak: minimalKeycloakMock({}) });

      cy.wait('@getFrameworks');
      
      cy.get('.framework-select .p-select-dropdown').click();
      cy.get('.p-select-list-container').contains('LkSG').click();
      
      cy.wait('@getLksgFramework');
      
      // Verify metadata panel displays correct framework
      cy.get('[data-test="framework-metadata"]').should('be.visible');
      cy.get('.framework-name').should('contain.text', 'LkSG');
      // Check for business definition text from fixture
      cy.get('.metadata-value').first().should('contain.text', 'German Supply Chain Due Diligence Act');
    });

    it('Should pass parsed schema to SpecificationSchemaTree', () => {
      cy.mountWithPlugins(SpecificationsViewer, { keycloak: minimalKeycloakMock({}) });

      cy.wait('@getFrameworks');
      
      cy.get('.framework-select .p-select-dropdown').click();
      cy.get('.p-select-list-container').contains('LkSG').click();
      
      cy.wait('@getLksgFramework');
      
      // Schema tree should render sections from parsed schema
      cy.get('[data-test="section-header"]', { timeout: 10000 }).should('exist');
      cy.get('[data-test="section-header"]').should('have.length.at.least', 1);
    });

    it('Should open DataPointTypeDetailsDialog when clicking View Details', () => {
      cy.mountWithPlugins(SpecificationsViewer, { keycloak: minimalKeycloakMock({}) });

      cy.wait('@getFrameworks');
      
      cy.get('.framework-select .p-select-dropdown').click();
      cy.get('.p-select-list-container').contains('LkSG').click();
      
      cy.wait('@getLksgFramework');
      
      // Wait for schema tree to render
      cy.get('[data-test="section-header"]', { timeout: 10000 }).should('exist');
      
      // Expand section
      cy.get('[data-test="section-header"]').first().click();
      
      // Wait for data points to be visible and click View Details
      cy.get('[data-test="view-details-button"]', { timeout: 5000 }).first().click();
      
      // Dialog should open
      cy.get('[role="dialog"]', { timeout: 5000 }).should('be.visible');
      cy.wait('@getDataPoint');
    });

    it('Should pass correct dataPointTypeId to dialog', () => {
      cy.mountWithPlugins(SpecificationsViewer, { keycloak: minimalKeycloakMock({}) });

      cy.wait('@getFrameworks');
      
      cy.get('.framework-select .p-select-dropdown').click();
      cy.get('.p-select-list-container').contains('LkSG').click();
      
      cy.wait('@getLksgFramework');
      
      // Wait for schema tree and expand section
      cy.get('[data-test="section-header"]', { timeout: 10000 }).first().click();
      cy.get('[data-test="view-details-button"]', { timeout: 5000 }).first().click();
      
      cy.wait('@getDataPoint');
      
      // Dialog should show data point details
      cy.get('[role="dialog"]', { timeout: 5000 }).should('be.visible');
      // Check for header with data point name from fixture
      cy.get('.p-dialog-header', { timeout: 5000 }).should('contain.text', 'Company Name');
    });

    it('Should close dialog and clear details on close', () => {
      cy.mountWithPlugins(SpecificationsViewer, { keycloak: minimalKeycloakMock({}) });

      cy.wait('@getFrameworks');
      
      cy.get('.framework-select .p-select-dropdown').click();
      cy.get('.p-select-list-container').contains('LkSG').click();
      
      cy.wait('@getLksgFramework');
      
      // Open dialog
      cy.get('[data-test="section-header"]', { timeout: 10000 }).first().click();
      cy.get('[data-test="view-details-button"]', { timeout: 5000 }).first().click();
      
      cy.wait('@getDataPoint');
      cy.get('[role="dialog"]', { timeout: 5000 }).should('be.visible');
      
      // Close dialog
      cy.get('[data-test="close-dialog"]').click();
      cy.get('[role="dialog"]', { timeout: 5000 }).should('not.exist');
    });

    it('Should allow opening dialog for different data point', () => {
      cy.mountWithPlugins(SpecificationsViewer, { keycloak: minimalKeycloakMock({}) });

      cy.wait('@getFrameworks');
      
      cy.get('.framework-select .p-select-dropdown').click();
      cy.get('.p-select-list-container').contains('LkSG').click();
      
      cy.wait('@getLksgFramework');
      
      // Open dialog for first data point
      cy.get('[data-test="section-header"]', { timeout: 10000 }).first().click();
      cy.get('[data-test="view-details-button"]', { timeout: 5000 }).first().click();
      
      cy.wait('@getDataPoint');
      cy.get('[role="dialog"]', { timeout: 5000 }).should('be.visible');
      
      // Close dialog
      cy.get('[data-test="close-dialog"]').click();
      cy.get('[role="dialog"]', { timeout: 5000 }).should('not.exist');
      
      // Open dialog for second data point
      cy.get('[data-test="view-details-button"]', { timeout: 5000 }).eq(1).click();
      
      cy.wait('@getDataPoint');
      cy.get('[role="dialog"]', { timeout: 5000 }).should('be.visible');
    });
  });

  describe('Empty states', () => {
    it('Should show empty state when no framework selected', () => {
      cy.mountWithPlugins(SpecificationsViewer, { keycloak: minimalKeycloakMock({}) });

      cy.wait('@getFrameworks');
      
      cy.get('.empty-state').should('be.visible');
      cy.get('.empty-state').should('contain.text', 'Select a framework');
      cy.get('.empty-icon').should('exist');
    });

    it('Should render empty schema gracefully', () => {
      cy.intercept('GET', '/specifications/frameworks/empty-test', { fixture: 'specifications/empty-framework.json' }).as('getEmptyFramework');
      
      // Update framework list to include empty framework
      cy.intercept('GET', '/specifications/frameworks', {
        body: [
          ...frameworkListFixture as SimpleFrameworkSpecification[],
          { framework: { id: 'empty-test', ref: '/specifications/frameworks/empty-test' }, name: 'Empty Framework' },
        ],
      }).as('getFrameworks');

      cy.mountWithPlugins(SpecificationsViewer, { keycloak: minimalKeycloakMock({}) });

      cy.wait('@getFrameworks');
      
      cy.get('.framework-select .p-select-dropdown').click();
      cy.get('.p-select-list-container').contains('Empty Framework').click();
      
      cy.wait('@getEmptyFramework');
      
      // Metadata panel should show but schema tree should be empty
      cy.get('[data-test="framework-metadata"]', { timeout: 10000 }).should('be.visible');
      cy.get('[data-test="section-header"]').should('not.exist');
    });
  });

  describe('URL synchronization', () => {
    it('Should auto-select framework from URL query param on mount', () => {
      // Create router with initial route per Dataland pattern
      const router = createRouter({
        routes: routes,
        history: createMemoryHistory(),
      });
      
      // Set initial route before mounting
      void router.push('/framework-specifications?framework=lksg');
      
      // Wait for router to be ready, then mount component
      cy.wrap(router.isReady()).then(() => {
        cy.mountWithPlugins(SpecificationsViewer, {
          keycloak: minimalKeycloakMock({}),
          router: router,
        });
      });

      cy.wait('@getFrameworks');
      cy.wait('@getLksgFramework');
      
      // Framework should be auto-selected
      cy.get('[data-test="framework-metadata"]').should('be.visible');
      cy.get('.framework-name').should('contain.text', 'LkSG');
    });

    it('Should update URL when switching frameworks', () => {
      cy.intercept('GET', '/specifications/frameworks/sfdr', {
        body: {
          framework: {
            id: 'sfdr',
            ref: '/specifications/frameworks/sfdr'
          },
          name: 'SFDR',
          businessDefinition: 'SFDR desc',
          schema: '{}',
        },
      }).as('getSfdrFramework');

      cy.mountWithPlugins(SpecificationsViewer, { keycloak: minimalKeycloakMock({}) });

      cy.wait('@getFrameworks');
      
      // Select first framework
      cy.get('.framework-select .p-select-dropdown').click();
      cy.get('.p-select-list-container').contains('LkSG').click();
      
      cy.wait('@getLksgFramework');
      cy.location('search', { timeout: 5000 }).should('include', 'framework=lksg');
      
      // Select second framework
      cy.get('.framework-select .p-select-dropdown').click();
      cy.get('.p-select-list-container').contains('SFDR').click();
      
      cy.wait('@getSfdrFramework');
      cy.location('search', { timeout: 5000 }).should('include', 'framework=sfdr');
    });
  });

  describe('Multiple interactions', () => {
    it('Should handle complete user journey', () => {
      cy.mountWithPlugins(SpecificationsViewer, { keycloak: minimalKeycloakMock({}) });

      // 1. Load frameworks
      cy.wait('@getFrameworks');
      cy.get('.empty-state', { timeout: 10000 }).should('be.visible');
      
      // 2. Select framework
      cy.get('.framework-select .p-select-dropdown').click();
      cy.get('.p-select-list-container').contains('LkSG').click();
      
      // 3. Verify framework loads
      cy.wait('@getLksgFramework');
      cy.get('[data-test="framework-metadata"]', { timeout: 10000 }).should('be.visible');
      
      // 4. Expand section
      cy.get('[data-test="section-header"]', { timeout: 10000 }).first().click();
      cy.get('[data-test="datapoint-name"]', { timeout: 5000 }).should('be.visible');
      
      // 5. View details
      cy.get('[data-test="view-details-button"]', { timeout: 5000 }).first().click();
      cy.wait('@getDataPoint');
      cy.get('[role="dialog"]', { timeout: 5000 }).should('be.visible');
      
      // 6. Close dialog
      cy.get('[data-test="close-dialog"]').click();
      cy.get('[role="dialog"]', { timeout: 5000 }).should('not.exist');
      
      // 7. Collapse section
      cy.get('[data-test="section-header"]').first().click();
      cy.get('[data-test="datapoint-name"]', { timeout: 5000 }).should('not.be.visible');
    });
  });
});
