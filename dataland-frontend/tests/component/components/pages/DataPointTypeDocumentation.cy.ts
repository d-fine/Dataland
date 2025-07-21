import DataPointTypeDocumentation from '@/components/pages/DataPointTypeDocumentation.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { 
  mockFetchResponses, 
  mockErrorResponses, 
  mockDataPointTypeSpecification 
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

describe('DataPointTypeDocumentation Component Tests', () => {
  beforeEach(() => {
    mockFetchResponses();
  });

  describe('Component Structure and Layout', () => {
    it('should render the basic page structure correctly', () => {
      // @ts-ignore
      cy.mountWithPlugins(DataPointTypeDocumentation, {
        props: { dataPointTypeId: 'extendedDecimalScope1GhgEmissionsInTonnes' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });

      cy.wait('@getExtendedDecimalSpec');
      assertDocumentationPageStructure();
    });

    it('should display the correct page title', () => {
      // @ts-ignore
      cy.mountWithPlugins(DataPointTypeDocumentation, {
        props: { dataPointTypeId: 'extendedDecimalScope1GhgEmissionsInTonnes' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });

      cy.wait('@getExtendedDecimalSpec');
      cy.get('h1').should('contain.text', 'extendedDecimalScope1GhgEmissionsInTonnes Data Point Type Specification');
    });
  });

  describe('Loading States', () => {
    it('should show loading state initially', () => {
      // Delay the response to test loading state
      cy.intercept('GET', '/specifications/data-point-types/extendedDecimalScope1GhgEmissionsInTonnes', {
        delay: 1000,
        statusCode: 200,
        body: mockDataPointTypeSpecification,
      }).as('getDelayedExtendedDecimalSpec');

      // @ts-ignore
      cy.mountWithPlugins(DataPointTypeDocumentation, {
        props: { dataPointTypeId: 'extendedDecimalScope1GhgEmissionsInTonnes' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });

      assertLoadingState();
    });

    it('should hide loading state after successful data fetch', () => {
      // @ts-ignore
      cy.mountWithPlugins(DataPointTypeDocumentation, {
        props: { dataPointTypeId: 'extendedDecimalScope1GhgEmissionsInTonnes' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });

      cy.wait('@getExtendedDecimalSpec');
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
      cy.mountWithPlugins(DataPointTypeDocumentation, {
        props: { dataPointTypeId: 'extendedDecimalScope1GhgEmissionsInTonnes' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });

      cy.wait('@getExtendedDecimalSpecError');
      assertErrorState('Failed to load specification data: 500 Internal Server Error');
    });

    it('should not show specification content when there is an error', () => {
      // @ts-ignore
      cy.mountWithPlugins(DataPointTypeDocumentation, {
        props: { dataPointTypeId: 'extendedDecimalScope1GhgEmissionsInTonnes' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });

      cy.wait('@getExtendedDecimalSpecError');
      cy.get('.specification-content').should('not.exist');
    });
  });

  describe('Specification Details Section', () => {
    beforeEach(() => {
      // @ts-ignore
      cy.mountWithPlugins(DataPointTypeDocumentation, {
        props: { dataPointTypeId: 'extendedDecimalScope1GhgEmissionsInTonnes' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });
      cy.wait('@getExtendedDecimalSpec');
    });

    it('should display the specification details section with correct title', () => {
      assertSpecificationDetailsSection('extendedDecimalScope1GhgEmissionsInTonnes Data Point Type Specification');
    });

    it('should display all specification fields correctly', () => {
      // Check ID field
      cy.contains('ID:').parent().within(() => {
        cy.get('.field-value').should('contain.text', mockDataPointTypeSpecification.dataPointType.id);
      });

      // Check Reference field
      cy.contains('Reference:').parent().within(() => {
        cy.get('button').should('contain.text', mockDataPointTypeSpecification.dataPointType.ref);
      });

      // Check Name field
      cy.contains('Name:').parent().within(() => {
        cy.get('.field-value').should('contain.text', mockDataPointTypeSpecification.name);
      });

      // Check Business Definition field
      cy.contains('Business Definition:').parent().within(() => {
        cy.get('.field-value').should('contain.text', mockDataPointTypeSpecification.businessDefinition);
      });

      // Check Data Point Base Type field
      cy.contains('Data Point Base Type:').parent().within(() => {
        cy.get('.field-value').should('contain.text', mockDataPointTypeSpecification.dataPointBaseType.id);
        cy.get('button').should('contain.text', mockDataPointTypeSpecification.dataPointBaseType.ref);
      });

      // Check Used By field
      cy.contains('Used By:').parent().within(() => {
        mockDataPointTypeSpecification.usedBy.forEach((framework) => {
          cy.get('.field-value').should('contain.text', framework.id.toUpperCase());
          cy.get('button').should('contain.text', framework.ref);
        });
      });
    });

    it('should render all specification fields with proper labels', () => {
      const expectedFields = [
        'ID:',
        'Reference:',
        'Name:',
        'Business Definition:',
        'Data Point Base Type:',
        'Used By:',
      ];

      expectedFields.forEach((field) => {
        cy.contains(field).should('be.visible');
      });
    });

    it('should display GHG emissions specific content', () => {
      cy.contains('Scope 1 GHG emissions').should('be.visible');
      cy.contains('greenhouse gas emissions').should('be.visible');
      cy.contains('equity share approach').should('be.visible');
    });

    it('should display the data point base type correctly', () => {
      cy.contains('Data Point Base Type:').parent().within(() => {
        cy.get('.field-value').should('contain.text', 'extendedDecimal');
      });
    });

    it('should display used by badges', () => {
      cy.contains('Used By:').parent().within(() => {
        cy.get('.field-value span').should('contain.text', 'SFDR');
        cy.get('.field-value span').should('have.class', 'bg-primary-50');
        cy.get('.field-value span').should('have.class', 'text-primary-600');
      });
    });
  });

  describe('Data Point Type Specific Features', () => {
    beforeEach(() => {
      // @ts-ignore
      cy.mountWithPlugins(DataPointTypeDocumentation, {
        props: { dataPointTypeId: 'extendedDecimalScope1GhgEmissionsInTonnes' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });
      cy.wait('@getExtendedDecimalSpec');
    });

    it('should display data point base type information', () => {
      cy.contains('Data Point Base Type:').should('be.visible');
      cy.contains('extendedDecimal').should('be.visible');
    });

    it('should show used by information', () => {
      cy.contains('Used By:').should('be.visible');
      cy.contains('SFDR').should('be.visible');
    });

    it('should handle constraints when they exist', () => {
      // Current test data has null constraints, so this field should not be displayed
      cy.contains('Constraints:').should('not.exist');
    });
  });

  describe('Framework Integration', () => {
    beforeEach(() => {
      // @ts-ignore
      cy.mountWithPlugins(DataPointTypeDocumentation, {
        props: { dataPointTypeId: 'extendedDecimalScope1GhgEmissionsInTonnes' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });
      cy.wait('@getExtendedDecimalSpec');
    });

    it('should display used by as badges', () => {
      cy.contains('Used By:').parent().within(() => {
        cy.get('span').should('have.length', 1);
        cy.get('span').first().should('contain.text', 'SFDR');
        cy.get('span').first().should('have.class', 'bg-primary-50');
      });
    });

    it('should handle multiple frameworks in used by', () => {
      const mockSpecWithMultipleFrameworks = {
        ...mockDataPointTypeSpecification,
        usedBy: [
          { id: 'sfdr', ref: 'https://local-dev.dataland.com/specifications/frameworks/sfdr' },
          { id: 'eu-taxonomy', ref: 'https://local-dev.dataland.com/specifications/frameworks/eu-taxonomy' },
          { id: 'lksg', ref: 'https://local-dev.dataland.com/specifications/frameworks/lksg' }
        ],
      };

      cy.intercept('GET', '/specifications/data-point-types/extendedDecimalScope1GhgEmissionsInTonnes', {
        statusCode: 200,
        body: mockSpecWithMultipleFrameworks,
      }).as('getMultipleFrameworksSpec');

      // @ts-ignore
      cy.mountWithPlugins(DataPointTypeDocumentation, {
        props: { dataPointTypeId: 'extendedDecimalScope1GhgEmissionsInTonnes' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });

      cy.wait('@getMultipleFrameworksSpec');
      cy.contains('Used By:').parent().within(() => {
        cy.get('.mb-2').should('have.length', 3);
        cy.contains('SFDR').should('be.visible');
        cy.contains('EU-TAXONOMY').should('be.visible');
        cy.contains('LKSG').should('be.visible');
      });
    });
  });

  describe('Data Validation', () => {
    it('should handle constraints when they are provided', () => {
      const mockSpecWithConstraints = {
        ...mockDataPointTypeSpecification,
        constraints: {
          minValue: 0,
          maxValue: 1000000,
          unit: 'tonnes CO2 equivalent'
        },
      };

      cy.intercept('GET', '/specifications/data-point-types/extendedDecimalScope1GhgEmissionsInTonnes', {
        statusCode: 200,
        body: mockSpecWithConstraints,
      }).as('getConstraintsSpec');

      // @ts-ignore
      cy.mountWithPlugins(DataPointTypeDocumentation, {
        props: { dataPointTypeId: 'extendedDecimalScope1GhgEmissionsInTonnes' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });

      cy.wait('@getConstraintsSpec');
      cy.contains('Constraints:').should('be.visible');
    });

    it('should handle different data point base types', () => {
      const mockSpecWithDifferentBaseType = {
        ...mockDataPointTypeSpecification,
        dataPointBaseType: {
          id: 'extendedCurrency',
          ref: 'https://local-dev.dataland.com/specifications/data-point-base-types/extendedCurrency'
        },
      };

      cy.intercept('GET', '/specifications/data-point-types/extendedDecimalScope1GhgEmissionsInTonnes', {
        statusCode: 200,
        body: mockSpecWithDifferentBaseType,
      }).as('getDifferentBaseTypeSpec');

      // @ts-ignore
      cy.mountWithPlugins(DataPointTypeDocumentation, {
        props: { dataPointTypeId: 'extendedDecimalScope1GhgEmissionsInTonnes' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });

      cy.wait('@getDifferentBaseTypeSpec');
      cy.contains('Data Point Base Type:').parent().within(() => {
        cy.get('.field-value').should('contain.text', 'extendedCurrency');
      });
    });

    it('should handle empty used by array', () => {
      const mockSpecWithEmptyFrameworks = {
        ...mockDataPointTypeSpecification,
        usedBy: [],
      };

      cy.intercept('GET', '/specifications/data-point-types/extendedDecimalScope1GhgEmissionsInTonnes', {
        statusCode: 200,
        body: mockSpecWithEmptyFrameworks,
      }).as('getEmptyFrameworksSpec');

      // @ts-ignore
      cy.mountWithPlugins(DataPointTypeDocumentation, {
        props: { dataPointTypeId: 'extendedDecimalScope1GhgEmissionsInTonnes' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });

      cy.wait('@getEmptyFrameworksSpec');
      cy.contains('Used By:').parent().within(() => {
        cy.get('.mb-2').should('have.length', 0);
      });
    });
  });

  describe('Responsive Design', () => {
    beforeEach(() => {
      // @ts-ignore
      cy.mountWithPlugins(DataPointTypeDocumentation, {
        props: { dataPointTypeId: 'extendedDecimalScope1GhgEmissionsInTonnes' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });
      cy.wait('@getExtendedDecimalSpec');
    });

    it('should display correctly on mobile devices', () => {
      testResponsiveBehavior();
    });

    it('should maintain field readability on small screens', () => {
      cy.viewport(375, 667);
      cy.get('.specification-field').should('be.visible');
      cy.get('.field-label').should('be.visible');
      cy.get('.field-value').should('be.visible');
    });

    it('should handle long business definition on mobile', () => {
      cy.viewport(375, 667);
      cy.contains('Business Definition:').parent().within(() => {
        cy.get('.field-value').should('be.visible');
        cy.get('.field-value').should('have.css', 'line-height');
      });
    });
  });

  describe('Accessibility', () => {
    beforeEach(() => {
      // @ts-ignore
      cy.mountWithPlugins(DataPointTypeDocumentation, {
        props: { dataPointTypeId: 'extendedDecimalScope1GhgEmissionsInTonnes' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });
      cy.wait('@getExtendedDecimalSpec');
    });

    it('should have proper accessibility features', () => {
      assertAccessibilityFeatures();
    });

    it('should have proper semantic structure', () => {
      cy.get('main[role="main"]').should('exist');
      cy.get('h1').should('have.length', 1);
      cy.get('h2').should('have.length', 1);
      cy.get('h3').should('have.length', 1);
    });

    it('should have proper labels for all fields', () => {
      cy.get('.specification-field').each(($field) => {
        cy.wrap($field).within(() => {
          cy.get('.field-label').should('exist').and('be.visible');
          cy.get('.field-value').should('exist').and('be.visible');
        });
      });
    });
  });

  describe('Performance', () => {
    it('should load and render content efficiently', () => {
      // @ts-ignore
      cy.mountWithPlugins(DataPointTypeDocumentation, {
        props: { dataPointTypeId: 'extendedDecimalScope1GhgEmissionsInTonnes' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });

      assertPerformanceMetrics();
    });

    it('should handle complex constraint objects', () => {
      const mockSpecWithComplexConstraints = {
        ...mockDataPointTypeSpecification,
        constraints: {
          validationRules: {
            minValue: 0,
            maxValue: 1000000,
            unit: 'tonnes CO2 equivalent'
          },
          metadata: {
            source: 'GHG Protocol',
            standard: 'ISO 14064-1',
            verificationLevel: 'Limited Assurance',
          },
        },
      };

      cy.intercept('GET', '/specifications/data-point-types/extendedDecimalScope1GhgEmissionsInTonnes', {
        statusCode: 200,
        body: mockSpecWithComplexConstraints,
      }).as('getComplexConstraintsSpec');

      // @ts-ignore
      cy.mountWithPlugins(DataPointTypeDocumentation, {
        props: { dataPointTypeId: 'extendedDecimalScope1GhgEmissionsInTonnes' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });

      cy.wait('@getComplexConstraintsSpec');
      cy.get('.specification-content', { timeout: 3000 }).should('be.visible');
      cy.contains('Constraints:').should('be.visible');
    });
  });

  describe('Content Formatting and Display', () => {
    beforeEach(() => {
      // @ts-ignore
      cy.mountWithPlugins(DataPointTypeDocumentation, {
        props: { dataPointTypeId: 'extendedDecimalScope1GhgEmissionsInTonnes' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });
      cy.wait('@getExtendedDecimalSpec');
    });

    it('should format monospace fields correctly', () => {
      cy.contains('ID:').parent().within(() => {
        cy.get('.field-value').should('have.class', 'font-family-monospace');
      });
      cy.contains('Data Point Base Type:').parent().within(() => {
        cy.get('.field-value .font-family-monospace').should('exist');
      });
    });

    it('should handle long text fields with proper line wrapping', () => {
      cy.contains('Business Definition:').parent().within(() => {
        cy.get('.field-value').should('have.css', 'line-height', '1.5');
      });
    });

    it('should display technical field names correctly', () => {
      cy.contains('extendedDecimalScope1GhgEmissionsInTonnes').should('be.visible');
      cy.contains('extendedDecimal').should('be.visible');
    });

    it('should style used by badges correctly', () => {
      cy.contains('Used By:').parent().within(() => {
        cy.get('span').should('have.class', 'bg-primary-50');
        cy.get('span').should('have.class', 'text-primary-600');
        cy.get('span').should('have.class', 'border-round');
      });
    });
  });

  describe('Component Integration', () => {
    it('should work correctly with the base layout component', () => {
      // @ts-ignore
      cy.mountWithPlugins(DataPointTypeDocumentation, {
        props: { dataPointTypeId: 'extendedDecimalScope1GhgEmissionsInTonnes' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });

      cy.wait('@getExtendedDecimalSpec');
      
      // Verify that base layout is properly integrated
      cy.get('header').should('exist');
      cy.get('main').should('exist');
      cy.get('footer').should('exist');
      cy.get('[data-cy="back-button"]').should('exist');
    });

    it('should use the specification details component correctly', () => {
      // @ts-ignore
      cy.mountWithPlugins(DataPointTypeDocumentation, {
        props: { dataPointTypeId: 'extendedDecimalScope1GhgEmissionsInTonnes' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });

      cy.wait('@getExtendedDecimalSpec');
      
      // Verify specification details component integration
      cy.get('.surface-card').should('have.length', 1); // Only Details section now
      cy.get('.specification-details').should('exist');
    });

    it('should display all required specification fields', () => {
      // @ts-ignore
      cy.mountWithPlugins(DataPointTypeDocumentation, {
        props: { dataPointTypeId: 'extendedDecimalScope1GhgEmissionsInTonnes' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });

      cy.wait('@getExtendedDecimalSpec');
      
      // Verify all required fields are present
      cy.get('.specification-field').should('have.length.at.least', 5); // ID, Reference, Name, Business Definition, Data Point Base Type, Used By
      cy.contains('ID:').should('exist');
      cy.contains('Reference:').should('exist');
      cy.contains('Name:').should('exist');
      cy.contains('Business Definition:').should('exist');
      cy.contains('Data Point Base Type:').should('exist');
      cy.contains('Used By:').should('exist');
    });
  });
});