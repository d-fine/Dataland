import SpecificationsViewer from '@/components/pages/SpecificationsViewer.vue';
import type { SimpleFrameworkSpecification, FrameworkSpecification } from '@clients/specificationservice';
import frameworkListFixture from '@/../testing/data/specifications/framework-list.json';
import lksgFrameworkFixture from '@/../testing/data/specifications/lksg-framework.json';
import emptyFrameworkFixture from '@/../testing/data/specifications/empty-framework.json';
import dataPointDetailsFixture from '@/../testing/data/specifications/datapointdetails.json';

describe('Component tests for SpecificationsViewer page', () => {
  beforeEach(() => {
    // Mock API calls with cy.intercept
    cy.intercept('GET', '/api/specification/frameworks', { fixture: 'specifications/framework-list.json' }).as('getFrameworks');
    cy.intercept('GET', '/api/specification/frameworks/lksg', { fixture: 'specifications/lksg-framework.json' }).as('getLksgFramework');
    cy.intercept('GET', '/api/specification/datapoints/*', { fixture: 'specifications/datapoint-details.json' }).as('getDataPoint');
  });

  describe('Framework selection flow', () => {
    it('Should show empty state when no framework selected', () => {
      cy.mountWithPlugins(SpecificationsViewer, {});

      cy.wait('@getFrameworks');
      cy.get('[data-test="specifications-content"]').should('be.visible');
      cy.get('.empty-state').should('be.visible');
      cy.get('.empty-state').should('contain.text', 'Select a framework');
    });

    it('Should populate framework dropdown after loading', () => {
      cy.mountWithPlugins(SpecificationsViewer, {});

      cy.wait('@getFrameworks');
      cy.get('[data-test="framework-selector"]').should('be.visible');
      
      // Open dropdown
      cy.get('.framework-select').click();
      
      // Should show framework options
      cy.get('.p-select-overlay').should('be.visible');
      cy.get('.p-select-option').should('have.length', 3);
      cy.get('.p-select-option').first().should('contain.text', 'LkSG');
    });

    it('Should load specification when framework selected', () => {
      cy.mountWithPlugins(SpecificationsViewer, {});

      cy.wait('@getFrameworks');
      
      // Select framework
      cy.get('.framework-select').click();
      cy.contains('.p-select-option', 'LkSG').click();
      
      cy.wait('@getLksgFramework');
      
      // Metadata panel should be visible
      cy.get('[data-test="framework-metadata"]').should('be.visible');
      cy.get('[data-test="framework-metadata"]').should('contain.text', 'LkSG');
    });

    it('Should render schema tree after loading specification', () => {
      cy.mountWithPlugins(SpecificationsViewer, {});

      cy.wait('@getFrameworks');
      
      // Select framework
      cy.get('.framework-select').click();
      cy.contains('.p-select-option', 'LkSG').click();
      
      cy.wait('@getLksgFramework');
      
      // Schema tree should be visible
      cy.get('[data-test="section-header"]').should('exist');
    });

    it('Should update URL query param when framework selected', () => {
      cy.mountWithPlugins(SpecificationsViewer, {});

      cy.wait('@getFrameworks');
      
      // Select framework
      cy.get('.framework-select').click();
      cy.contains('.p-select-option', 'LkSG').click();
      
      // URL should be updated
      cy.location('search').should('include', 'framework=lksg');
    });
  });

  describe('Loading states', () => {
    it('Should show spinner in dropdown area while frameworks loading', () => {
      cy.intercept('GET', '/api/specification/frameworks', (req) => {
        req.reply({ fixture: 'specifications/framework-list.json', delay: 500 });
      }).as('getFrameworksSlow');

      cy.mountWithPlugins(SpecificationsViewer, {});

      cy.get('.spinner-small').should('be.visible');
      
      cy.wait('@getFrameworksSlow');
      cy.get('.spinner-small').should('not.exist');
    });

    it('Should show loading state while specification loads', () => {
      cy.intercept('GET', '/api/specification/frameworks/lksg', (req) => {
        req.reply({ fixture: 'specifications/lksg-framework.json', delay: 500 });
      }).as('getLksgFrameworkSlow');

      cy.mountWithPlugins(SpecificationsViewer, {});

      cy.wait('@getFrameworks');
      
      // Select framework
      cy.get('.framework-select').click();
      cy.contains('.p-select-option', 'LkSG').click();
      
      // Loading state should appear
      cy.get('.loading-state').should('be.visible');
      cy.get('.loading-state').should('contain.text', 'Loading framework specification');
      
      cy.wait('@getLksgFrameworkSlow');
      cy.get('.loading-state').should('not.exist');
    });

    it('Should keep dropdown interactive while specification loads', () => {
      cy.intercept('GET', '/api/specification/frameworks/lksg', (req) => {
        req.reply({ fixture: 'specifications/lksg-framework.json', delay: 500 });
      }).as('getLksgFrameworkSlow');

      cy.mountWithPlugins(SpecificationsViewer, {});

      cy.wait('@getFrameworks');
      
      // Select framework
      cy.get('.framework-select').click();
      cy.contains('.p-select-option', 'LkSG').click();
      
      // Dropdown should still be interactive
      cy.get('.framework-select').should('not.be.disabled');
    });

    it('Should show batch loading indicator when loading data point details', () => {
      cy.intercept('GET', '/api/specification/datapoints/*', (req) => {
        req.reply({ fixture: 'specifications/datapoint-details.json', delay: 500 });
      }).as('getDataPointSlow');

      cy.mountWithPlugins(SpecificationsViewer, {});

      cy.wait('@getFrameworks');
      
      // Select framework
      cy.get('.framework-select').click();
      cy.contains('.p-select-option', 'LkSG').click();
      
      // Batch loading indicator should appear
      cy.get('.loading-details').should('be.visible');
      cy.get('.loading-details').should('contain.text', 'Loading detailed descriptions');
    });
  });

  describe('Error scenarios', () => {
    it('Should show error message when framework list fetch fails', () => {
      cy.intercept('GET', '/api/specification/frameworks', { statusCode: 500, body: { message: 'Server error' } }).as('getFrameworksError');

      cy.mountWithPlugins(SpecificationsViewer, {});

      cy.wait('@getFrameworksError');
      cy.get('.error-message').should('be.visible');
      cy.get('.error-message').should('contain.text', 'Failed to load');
    });

    it('Should show retry button for framework list errors', () => {
      cy.intercept('GET', '/api/specification/frameworks', { statusCode: 500, body: { message: 'Server error' } }).as('getFrameworksError');

      cy.mountWithPlugins(SpecificationsViewer, {});

      cy.wait('@getFrameworksError');
      cy.contains('button', 'Retry').should('be.visible');
    });

    it('Should retry framework list fetch on retry button click', () => {
      let requestCount = 0;
      cy.intercept('GET', '/api/specification/frameworks', (req) => {
        requestCount++;
        if (requestCount === 1) {
          req.reply({ statusCode: 500, body: { message: 'Server error' } });
        } else {
          req.reply({ fixture: 'specifications/framework-list.json' });
        }
      }).as('getFrameworksRetry');

      cy.mountWithPlugins(SpecificationsViewer, {});

      cy.wait('@getFrameworksRetry');
      cy.get('.error-message').should('be.visible');
      
      // Click retry
      cy.contains('button', 'Retry').click();
      
      cy.wait('@getFrameworksRetry');
      cy.get('.error-message').should('not.exist');
      cy.get('[data-test="framework-selector"]').should('be.visible');
    });

    it('Should show error message when specification fetch fails', () => {
      cy.intercept('GET', '/api/specification/frameworks/lksg', { statusCode: 500, body: { message: 'Server error' } }).as('getLksgFrameworkError');

      cy.mountWithPlugins(SpecificationsViewer, {});

      cy.wait('@getFrameworks');
      
      // Select framework
      cy.get('.framework-select').click();
      cy.contains('.p-select-option', 'LkSG').click();
      
      cy.wait('@getLksgFrameworkError');
      cy.get('.error-message').should('be.visible');
    });

    it('Should clear error on successful retry', () => {
      let requestCount = 0;
      cy.intercept('GET', '/api/specification/frameworks/lksg', (req) => {
        requestCount++;
        if (requestCount === 1) {
          req.reply({ statusCode: 500, body: { message: 'Server error' } });
        } else {
          req.reply({ fixture: 'specifications/lksg-framework.json' });
        }
      }).as('getLksgFrameworkRetry');

      cy.mountWithPlugins(SpecificationsViewer, {});

      cy.wait('@getFrameworks');
      
      // Select framework
      cy.get('.framework-select').click();
      cy.contains('.p-select-option', 'LkSG').click();
      
      cy.wait('@getLksgFrameworkRetry');
      cy.get('.error-message').should('be.visible');
      
      // Click retry
      cy.contains('button', 'Retry').click();
      
      cy.wait('@getLksgFrameworkRetry');
      cy.get('.error-message').should('not.exist');
      cy.get('[data-test="framework-metadata"]').should('be.visible');
    });
  });

  describe('Child component integration', () => {
    it('Should pass correct framework prop to FrameworkMetadataPanel', () => {
      cy.mountWithPlugins(SpecificationsViewer, {});

      cy.wait('@getFrameworks');
      
      cy.get('.framework-select').click();
      cy.contains('.p-select-option', 'LkSG').click();
      
      cy.wait('@getLksgFramework');
      
      // Verify metadata panel displays correct framework
      cy.get('[data-test="framework-metadata"]').should('be.visible');
      cy.get('.framework-name').should('contain.text', 'LkSG');
      cy.get('.metadata-value').should('contain.text', 'German Supply Chain');
    });

    it('Should pass parsed schema to SpecificationSchemaTree', () => {
      cy.mountWithPlugins(SpecificationsViewer, {});

      cy.wait('@getFrameworks');
      
      cy.get('.framework-select').click();
      cy.contains('.p-select-option', 'LkSG').click();
      
      cy.wait('@getLksgFramework');
      
      // Schema tree should render sections from parsed schema
      cy.get('[data-test="section-header"]').should('exist');
      cy.get('[data-test="section-header"]').should('have.length.at.least', 1);
    });

    it('Should open DataPointTypeDetailsDialog when clicking View Details', () => {
      cy.mountWithPlugins(SpecificationsViewer, {});

      cy.wait('@getFrameworks');
      
      cy.get('.framework-select').click();
      cy.contains('.p-select-option', 'LkSG').click();
      
      cy.wait('@getLksgFramework');
      
      // Expand section
      cy.get('[data-test="section-header"]').first().click();
      
      // Click View Details
      cy.get('[data-test="view-details-button"]').first().click();
      
      // Dialog should open
      cy.get('[role="dialog"]').should('be.visible');
      cy.wait('@getDataPoint');
    });

    it('Should pass correct dataPointTypeId to dialog', () => {
      cy.mountWithPlugins(SpecificationsViewer, {});

      cy.wait('@getFrameworks');
      
      cy.get('.framework-select').click();
      cy.contains('.p-select-option', 'LkSG').click();
      
      cy.wait('@getLksgFramework');
      
      // Expand section and click View Details
      cy.get('[data-test="section-header"]').first().click();
      cy.get('[data-test="view-details-button"]').first().click();
      
      cy.wait('@getDataPoint');
      
      // Dialog should show data point details
      cy.get('[role="dialog"]').should('be.visible');
      cy.get('.p-dialog-header').should('contain.text', 'Company Name');
    });

    it('Should close dialog and clear details on close', () => {
      cy.mountWithPlugins(SpecificationsViewer, {});

      cy.wait('@getFrameworks');
      
      cy.get('.framework-select').click();
      cy.contains('.p-select-option', 'LkSG').click();
      
      cy.wait('@getLksgFramework');
      
      // Open dialog
      cy.get('[data-test="section-header"]').first().click();
      cy.get('[data-test="view-details-button"]').first().click();
      
      cy.wait('@getDataPoint');
      cy.get('[role="dialog"]').should('be.visible');
      
      // Close dialog
      cy.get('[data-test="close-dialog"]').click();
      cy.get('[role="dialog"]').should('not.exist');
    });

    it('Should allow opening dialog for different data point', () => {
      cy.mountWithPlugins(SpecificationsViewer, {});

      cy.wait('@getFrameworks');
      
      cy.get('.framework-select').click();
      cy.contains('.p-select-option', 'LkSG').click();
      
      cy.wait('@getLksgFramework');
      
      // Open dialog for first data point
      cy.get('[data-test="section-header"]').first().click();
      cy.get('[data-test="view-details-button"]').first().click();
      
      cy.wait('@getDataPoint');
      cy.get('[role="dialog"]').should('be.visible');
      
      // Close dialog
      cy.get('[data-test="close-dialog"]').click();
      
      // Open dialog for second data point
      cy.get('[data-test="view-details-button"]').eq(1).click();
      
      cy.wait('@getDataPoint');
      cy.get('[role="dialog"]').should('be.visible');
    });
  });

  describe('Empty states', () => {
    it('Should show empty state when no framework selected', () => {
      cy.mountWithPlugins(SpecificationsViewer, {});

      cy.wait('@getFrameworks');
      
      cy.get('.empty-state').should('be.visible');
      cy.get('.empty-state').should('contain.text', 'Select a framework');
      cy.get('.empty-icon').should('exist');
    });

    it('Should render empty schema gracefully', () => {
      cy.intercept('GET', '/api/specification/frameworks/empty-test', { fixture: 'specifications/empty-framework.json' }).as('getEmptyFramework');
      
      // Update framework list to include empty framework
      cy.intercept('GET', '/api/specification/frameworks', {
        body: [
          ...frameworkListFixture as SimpleFrameworkSpecification[],
          { id: 'empty-test', name: 'Empty Framework', businessDefinition: 'Empty' },
        ],
      }).as('getFrameworks');

      cy.mountWithPlugins(SpecificationsViewer, {});

      cy.wait('@getFrameworks');
      
      cy.get('.framework-select').click();
      cy.contains('.p-select-option', 'Empty Framework').click();
      
      cy.wait('@getEmptyFramework');
      
      // Metadata panel should show but schema tree should be empty
      cy.get('[data-test="framework-metadata"]').should('be.visible');
      cy.get('[data-test="section-header"]').should('not.exist');
    });
  });

  describe('URL synchronization', () => {
    it('Should auto-select framework from URL query param on mount', () => {
      cy.mountWithPlugins(SpecificationsViewer, {
        router: {
          initialRoute: '/specifications?framework=lksg',
        },
      });

      cy.wait('@getFrameworks');
      cy.wait('@getLksgFramework');
      
      // Framework should be auto-selected
      cy.get('[data-test="framework-metadata"]').should('be.visible');
      cy.get('.framework-name').should('contain.text', 'LkSG');
    });

    it('Should update URL when switching frameworks', () => {
      cy.intercept('GET', '/api/specification/frameworks/sfdr', {
        body: {
          id: 'sfdr',
          name: 'SFDR',
          businessDefinition: 'SFDR desc',
          schema: '{}',
        },
      }).as('getSfdrFramework');

      cy.mountWithPlugins(SpecificationsViewer, {});

      cy.wait('@getFrameworks');
      
      // Select first framework
      cy.get('.framework-select').click();
      cy.contains('.p-select-option', 'LkSG').click();
      
      cy.wait('@getLksgFramework');
      cy.location('search').should('include', 'framework=lksg');
      
      // Select second framework
      cy.get('.framework-select').click();
      cy.contains('.p-select-option', 'SFDR').click();
      
      cy.wait('@getSfdrFramework');
      cy.location('search').should('include', 'framework=sfdr');
    });
  });

  describe('Multiple interactions', () => {
    it('Should handle complete user journey', () => {
      cy.mountWithPlugins(SpecificationsViewer, {});

      // 1. Load frameworks
      cy.wait('@getFrameworks');
      cy.get('.empty-state').should('be.visible');
      
      // 2. Select framework
      cy.get('.framework-select').click();
      cy.contains('.p-select-option', 'LkSG').click();
      
      // 3. Verify framework loads
      cy.wait('@getLksgFramework');
      cy.get('[data-test="framework-metadata"]').should('be.visible');
      
      // 4. Expand section
      cy.get('[data-test="section-header"]').first().click();
      cy.get('[data-test="datapoint-name"]').should('be.visible');
      
      // 5. View details
      cy.get('[data-test="view-details-button"]').first().click();
      cy.wait('@getDataPoint');
      cy.get('[role="dialog"]').should('be.visible');
      
      // 6. Close dialog
      cy.get('[data-test="close-dialog"]').click();
      cy.get('[role="dialog"]').should('not.exist');
      
      // 7. Collapse section
      cy.get('[data-test="section-header"]').first().click();
      cy.get('[data-test="datapoint-name"]').should('not.exist');
    });
  });
});
