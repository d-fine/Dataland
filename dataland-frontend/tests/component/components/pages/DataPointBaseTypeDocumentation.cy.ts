import DataPointBaseTypeDocumentation from '@/components/pages/DataPointBaseTypeDocumentation.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { 
  mockFetchResponses, 
  mockErrorResponses, 
  mockDataPointBaseTypeSpecification 
} from '@ct/testUtils/DocumentationFixtures';
import {
  assertDocumentationPageStructure,
  assertLoadingState,
  assertErrorState,
  assertSpecificationDetailsSection,
  assertBasicSpecificationFields,
  assertSchemaSection,
  testResponsiveBehavior,
  assertAccessibilityFeatures,
  assertPerformanceMetrics,
} from '@ct/testUtils/DocumentationTestUtils';

describe('DataPointBaseTypeDocumentation Component Tests', () => {
  beforeEach(() => {
    mockFetchResponses();
  });

  describe('Component Structure and Layout', () => {
    it('should render the basic page structure correctly', () => {
      // @ts-ignore
      cy.mountWithPlugins(DataPointBaseTypeDocumentation, {
        props: { baseTypeId: 'extendedCurrency' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });

      cy.wait('@getExtendedCurrencySpec');
      assertDocumentationPageStructure();
    });

    it('should display the correct page title', () => {
      // @ts-ignore
      cy.mountWithPlugins(DataPointBaseTypeDocumentation, {
        props: { baseTypeId: 'extendedCurrency' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });

      cy.wait('@getExtendedCurrencySpec');
      cy.get('h1').should('contain.text', 'extendedCurrency Data Point Base Type Specification');
    });
  });

  describe('Loading States', () => {
    it('should show loading state initially', () => {
      // Delay the response to test loading state
      cy.intercept('GET', '/specifications/data-point-base-types/extendedCurrency', {
        delay: 1000,
        statusCode: 200,
        body: mockDataPointBaseTypeSpecification,
      }).as('getDelayedExtendedCurrencySpec');

      // @ts-ignore
      cy.mountWithPlugins(DataPointBaseTypeDocumentation, {
        props: { baseTypeId: 'extendedCurrency' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });

      assertLoadingState();
    });

    it('should hide loading state after successful data fetch', () => {
      // @ts-ignore
      cy.mountWithPlugins(DataPointBaseTypeDocumentation, {
        props: { baseTypeId: 'extendedCurrency' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });

      cy.wait('@getExtendedCurrencySpec');
      cy.get('.pi-spinner').should('not.exist');
      cy.contains('Loading specification data...').should('not.exist');
    });
  });

  describe('Error Handling', () => {
    beforeEach(() => {
      mockErrorResponses();
    });

    it('should display error message when API call fails', () => {
      // @ts-ignore
      cy.mountWithPlugins(DataPointBaseTypeDocumentation, {
        props: { baseTypeId: 'extendedCurrency' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });

      cy.wait('@getExtendedCurrencySpecError');
      assertErrorState('Failed to load specification data: 404 Not Found');
    });

    it('should not show specification content when there is an error', () => {
      // @ts-ignore
      cy.mountWithPlugins(DataPointBaseTypeDocumentation, {
        props: { baseTypeId: 'extendedCurrency' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });

      cy.wait('@getExtendedCurrencySpecError');
      cy.get('.specification-content').should('not.exist');
    });
  });

  describe('Specification Details Section', () => {
    beforeEach(() => {
      // @ts-ignore
      cy.mountWithPlugins(DataPointBaseTypeDocumentation, {
        props: { baseTypeId: 'extendedCurrency' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });
      cy.wait('@getExtendedCurrencySpec');
    });

    it('should display the specification details section with correct title', () => {
      assertSpecificationDetailsSection('extendedCurrency Data Point Base Type Specification');
    });

    it('should display all basic specification fields correctly', () => {
      assertBasicSpecificationFields({
        id: mockDataPointBaseTypeSpecification.dataPointBaseType.id,
        name: mockDataPointBaseTypeSpecification.name,
        businessDefinition: mockDataPointBaseTypeSpecification.businessDefinition,
        validatedBy: mockDataPointBaseTypeSpecification.validatedBy,
      });
    });

    it('should render all specification fields with proper labels', () => {
      const expectedFields = [
        'ID:',
        'Reference:',
        'Name:',
        'Business Definition:',
        'Validated By:',
      ];

      expectedFields.forEach((field) => {
        cy.contains(field).should('be.visible');
      });
    });

    it('should display the ID field with monospace font', () => {
      cy.contains('ID:').parent().within(() => {
        cy.get('.field-value').should('have.css', 'font-family').and('include', 'monospace');
      });
    });

    it('should display the validated by field with monospace font', () => {
      cy.contains('Validated By:').parent().within(() => {
        cy.get('.field-value').should('have.css', 'font-family').and('include', 'monospace');
      });
    });
  });

  describe('Example Structure Section', () => {
    beforeEach(() => {
      // @ts-ignore
      cy.mountWithPlugins(DataPointBaseTypeDocumentation, {
        props: { baseTypeId: 'extendedCurrency' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });
      cy.wait('@getExtendedCurrencySpec');
    });

    it('should display the example structure section with correct title', () => {
      assertSchemaSection('Example Structure');
    });

    it('should display the example JSON correctly formatted', () => {
      cy.get('.example-container').should('exist');
      cy.get('.example-json').should('exist');
      cy.get('.example-json').should('contain.text', 'value');
      cy.get('.example-json').should('contain.text', 'currency');
      cy.get('.example-json').should('contain.text', 'quality');
    });

    it('should show example data with proper JSON formatting', () => {
      cy.get('.example-json').within(() => {
        // Check for proper JSON structure
        cy.should('contain.text', mockDataPointBaseTypeSpecification.example.value);
        cy.should('contain.text', mockDataPointBaseTypeSpecification.example.currency);
        cy.should('contain.text', mockDataPointBaseTypeSpecification.example.quality);
        cy.should('contain.text', mockDataPointBaseTypeSpecification.example.comment);
      });
    });

    it('should display data source information in the example', () => {
      cy.get('.example-json').within(() => {
        cy.should('contain.text', 'dataSource');
        cy.should('contain.text', mockDataPointBaseTypeSpecification.example.dataSource.page);
        cy.should('contain.text', mockDataPointBaseTypeSpecification.example.dataSource.fileName);
        cy.should('contain.text', mockDataPointBaseTypeSpecification.example.dataSource.tagName);
      });
    });

    it('should use monospace font for JSON display', () => {
      cy.get('.example-json').should('have.css', 'font-family').and('include', 'monospace');
    });
  });

  describe('Used By Data Point Types Section', () => {
    beforeEach(() => {
      // @ts-ignore
      cy.mountWithPlugins(DataPointBaseTypeDocumentation, {
        props: { baseTypeId: 'extendedCurrency' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });
      cy.wait('@getExtendedCurrencySpec');
    });

    it('should display the used by section with correct title', () => {
      cy.get('.surface-card').eq(1).within(() => {
        cy.get('h3').should('contain.text', 'Used By Data Point Types');
      });
    });

    it('should display all data point types that use this base type', () => {
      cy.get('.used-by-container').should('exist');
      cy.get('.used-by-item').should('have.length', mockDataPointBaseTypeSpecification.usedBy.length);
      
      mockDataPointBaseTypeSpecification.usedBy.forEach((dataPointType, index) => {
        cy.get('.used-by-item').eq(index).within(() => {
          cy.get('span').should('contain.text', dataPointType.id);
          cy.get('button').should('contain.text', 'View Specification →');
        });
      });
    });

    it('should have proper styling for used by items', () => {
      cy.get('.used-by-item').should('have.class', 'p-3');
      cy.get('.used-by-item').should('have.class', 'border-1');
      cy.get('.used-by-item').should('have.class', 'border-round');
      cy.get('.used-by-item').should('have.class', 'surface-100');
    });

    it('should display badge styling for data point type IDs', () => {
      cy.get('.used-by-item').first().within(() => {
        cy.get('span').should('have.class', 'bg-primary-50');
        cy.get('span').should('have.class', 'text-primary-600');
        cy.get('span').should('have.class', 'border-round');
      });
    });

    it('should handle empty used by array', () => {
      const mockSpecWithEmptyUsedBy = {
        ...mockDataPointBaseTypeSpecification,
        usedBy: [],
      };

      cy.intercept('GET', '/specifications/data-point-base-types/extendedCurrency', {
        statusCode: 200,
        body: mockSpecWithEmptyUsedBy,
      }).as('getEmptyUsedBySpec');

      // @ts-ignore
      cy.mountWithPlugins(DataPointBaseTypeDocumentation, {
        props: { baseTypeId: 'extendedCurrency' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });

      cy.wait('@getEmptyUsedBySpec');
      cy.get('.surface-card').eq(1).within(() => {
        cy.contains('No data point types use this base type').should('be.visible');
      });
    });

    it('should have clickable buttons for navigation', () => {
      cy.get('.used-by-item').first().within(() => {
        cy.get('button').should('exist');
        cy.get('button').should('contain.text', 'View Specification →');
      });
    });
  });

  describe('Data Validation', () => {
    it('should handle empty example data gracefully', () => {
      const mockSpecWithEmptyExample = {
        ...mockDataPointBaseTypeSpecification,
        example: {},
      };

      cy.intercept('GET', '/specifications/data-point-base-types/extendedCurrency', {
        statusCode: 200,
        body: mockSpecWithEmptyExample,
      }).as('getEmptyExampleSpec');

      // @ts-ignore
      cy.mountWithPlugins(DataPointBaseTypeDocumentation, {
        props: { baseTypeId: 'extendedCurrency' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });

      cy.wait('@getEmptyExampleSpec');
      cy.get('.example-container').should('exist');
      cy.get('.example-json').should('contain.text', '{}');
    });

    it('should handle missing fields gracefully', () => {
      const mockSpecWithMissingFields = {
        dataPointBaseType: mockDataPointBaseTypeSpecification.dataPointBaseType,
        name: mockDataPointBaseTypeSpecification.name,
        // Missing businessDefinition and validatedBy
        example: mockDataPointBaseTypeSpecification.example,
        usedBy: mockDataPointBaseTypeSpecification.usedBy,
      };

      cy.intercept('GET', '/specifications/data-point-base-types/extendedCurrency', {
        statusCode: 200,
        body: mockSpecWithMissingFields,
      }).as('getMissingFieldsSpec');

      // @ts-ignore
      cy.mountWithPlugins(DataPointBaseTypeDocumentation, {
        props: { baseTypeId: 'extendedCurrency' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });

      cy.wait('@getMissingFieldsSpec');
      cy.contains('ID:').should('be.visible');
      cy.contains('Name:').should('be.visible');
      cy.contains('Business Definition:').should('not.exist');
      cy.contains('Validated By:').should('not.exist');
    });
  });

  describe('Responsive Design', () => {
    beforeEach(() => {
      // @ts-ignore
      cy.mountWithPlugins(DataPointBaseTypeDocumentation, {
        props: { baseTypeId: 'extendedCurrency' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });
      cy.wait('@getExtendedCurrencySpec');
    });

    it('should display correctly on mobile devices', () => {
      testResponsiveBehavior();
    });

    it('should maintain JSON readability on small screens', () => {
      cy.viewport(375, 667);
      cy.get('.example-container').should('be.visible');
      cy.get('.example-json').should('be.visible');
      
      // Check that horizontal scrolling is available for long JSON lines
      cy.get('.example-container').should('have.css', 'overflow-x', 'auto');
    });

    it('should adjust font size on mobile', () => {
      cy.viewport(375, 667);
      cy.get('.example-json').should('have.css', 'font-size', '12.8px'); // 0.8rem
    });
  });

  describe('Accessibility', () => {
    beforeEach(() => {
      // @ts-ignore
      cy.mountWithPlugins(DataPointBaseTypeDocumentation, {
        props: { baseTypeId: 'extendedCurrency' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });
      cy.wait('@getExtendedCurrencySpec');
    });

    it('should have proper accessibility features', () => {
      assertAccessibilityFeatures();
    });

    it('should have proper semantic structure for specification fields', () => {
      cy.get('.specification-field').each(($field) => {
        cy.wrap($field).within(() => {
          cy.get('label, .field-label').should('exist');
          cy.get('.field-value, span, p').should('exist');
        });
      });
    });

    it('should have proper contrast for JSON text', () => {
      cy.get('.example-json').should('be.visible').and('not.have.css', 'color', 'rgb(255, 255, 255)');
    });
  });

  describe('Performance', () => {
    it('should load and render content efficiently', () => {
      // @ts-ignore
      cy.mountWithPlugins(DataPointBaseTypeDocumentation, {
        props: { baseTypeId: 'extendedCurrency' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });

      assertPerformanceMetrics();
    });

    it('should handle large example objects without performance issues', () => {
      const largeExample = {
        ...mockDataPointBaseTypeSpecification.example,
        additionalData: {} as Record<string, string>,
      };

      // Add many fields to test performance
      for (let i = 0; i < 100; i++) {
        largeExample.additionalData[`field${i}`] = `value${i}`;
      }

      const mockSpecWithLargeExample = {
        ...mockDataPointBaseTypeSpecification,
        example: largeExample,
      };

      cy.intercept('GET', '/specifications/data-point-base-types/extendedCurrency', {
        statusCode: 200,
        body: mockSpecWithLargeExample,
      }).as('getLargeExampleSpec');

      // @ts-ignore
      cy.mountWithPlugins(DataPointBaseTypeDocumentation, {
        props: { baseTypeId: 'extendedCurrency' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });

      cy.wait('@getLargeExampleSpec');
      cy.get('.example-container', { timeout: 3000 }).should('be.visible');
      cy.get('.example-json').should('contain.text', 'additionalData');
    });
  });

  describe('Content Formatting', () => {
    beforeEach(() => {
      // @ts-ignore
      cy.mountWithPlugins(DataPointBaseTypeDocumentation, {
        props: { baseTypeId: 'extendedCurrency' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });
      cy.wait('@getExtendedCurrencySpec');
    });

    it('should format JSON with proper indentation', () => {
      cy.get('.example-json').should('contain.text', '  "value"'); // 2-space indentation
      cy.get('.example-json').should('contain.text', '  "currency"');
    });

    it('should preserve line breaks in business definition', () => {
      cy.contains('Business Definition:').parent().within(() => {
        cy.get('.field-value').should('have.css', 'line-height');
      });
    });

    it('should display numeric values correctly', () => {
      cy.get('.example-json').should('contain.text', '100.5');
    });

    it('should display string values with proper quotes', () => {
      cy.get('.example-json').should('contain.text', '"USD"');
      cy.get('.example-json').should('contain.text', '"Reported"');
    });
  });
});