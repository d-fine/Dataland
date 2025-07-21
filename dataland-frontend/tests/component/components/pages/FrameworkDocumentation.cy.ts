import FrameworkDocumentation from '@/components/pages/FrameworkDocumentation.vue';
import { minimalKeycloakMock } from '@ct/testUtils/Keycloak';
import { 
  mockFetchResponses, 
  mockErrorResponses, 
  mockSfdrFrameworkSpecification 
} from '@ct/testUtils/DocumentationFixtures';
import {
  assertDocumentationPageStructure,
  assertLoadingState,
  assertErrorState,
  assertSpecificationDetailsSection,
  assertBasicSpecificationFields,
  assertFrameworkSpecificationFields,
  assertSchemaSection,
  assertClickableSchemaLinks,
  testSchemaLinkClicks,
  assertAliasExportFormat,
  testResponsiveBehavior,
  assertAccessibilityFeatures,
  assertPerformanceMetrics,
} from '@ct/testUtils/DocumentationTestUtils';

describe('FrameworkDocumentation Component Tests', () => {
  beforeEach(() => {
    mockFetchResponses();
  });

  describe('Component Structure and Layout', () => {
    it('should render the basic page structure correctly', () => {
      // @ts-ignore
      cy.mountWithPlugins(FrameworkDocumentation, {
        props: { frameworkId: 'sfdr' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });

      cy.wait('@getSfdrFrameworkSpec');
      assertDocumentationPageStructure();
    });

    it('should display the correct page title', () => {
      // @ts-ignore
      cy.mountWithPlugins(FrameworkDocumentation, {
        props: { frameworkId: 'sfdr' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });

      cy.wait('@getSfdrFrameworkSpec');
      cy.get('h1').should('contain.text', 'SFDR Framework Specification');
    });
  });

  describe('Loading States', () => {
    it('should show loading state initially', () => {
      // Delay the response to test loading state
      cy.intercept('GET', '/specifications/frameworks/sfdr', {
        delay: 1000,
        statusCode: 200,
        body: mockSfdrFrameworkSpecification,
      }).as('getDelayedSfdrSpec');

      // @ts-ignore
      cy.mountWithPlugins(FrameworkDocumentation, {
        props: { frameworkId: 'sfdr' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });

      assertLoadingState();
    });

    it('should hide loading state after successful data fetch', () => {
      // @ts-ignore
      cy.mountWithPlugins(FrameworkDocumentation, {
        props: { frameworkId: 'sfdr' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });

      cy.wait('@getSfdrFrameworkSpec');
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
      cy.mountWithPlugins(FrameworkDocumentation, {
        props: { frameworkId: 'sfdr' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });

      cy.wait('@getSfdrFrameworkSpecError');
      assertErrorState('Failed to load specification data: 503 Service Unavailable');
    });

    it('should not show specification content when there is an error', () => {
      // @ts-ignore
      cy.mountWithPlugins(FrameworkDocumentation, {
        props: { frameworkId: 'sfdr' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });

      cy.wait('@getSfdrFrameworkSpecError');
      cy.get('.specification-content').should('not.exist');
    });
  });

  describe('Specification Details Section', () => {
    beforeEach(() => {
      // @ts-ignore
      cy.mountWithPlugins(FrameworkDocumentation, {
        props: { frameworkId: 'sfdr' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });
      cy.wait('@getSfdrFrameworkSpec');
    });

    it('should display the specification details section with correct title', () => {
      assertSpecificationDetailsSection('SFDR Framework Specification');
    });

    it('should display framework-specific fields correctly', () => {
      assertFrameworkSpecificationFields({
        frameworkId: mockSfdrFrameworkSpecification.framework.id,
        frameworkRef: mockSfdrFrameworkSpecification.framework.ref,
        referencedReportJsonPath: mockSfdrFrameworkSpecification.referencedReportJsonPath,
      });
    });

    it('should display basic specification fields correctly', () => {
      assertBasicSpecificationFields({
        name: mockSfdrFrameworkSpecification.name,
        businessDefinition: mockSfdrFrameworkSpecification.businessDefinition,
      });
    });

    it('should render all specification fields with proper labels', () => {
      const expectedFields = [
        'Framework ID:',
        'Framework Reference:',
        'Name:',
        'Business Definition:',
        'Referenced Report JSON Path:',
      ];

      expectedFields.forEach((field) => {
        cy.contains(field).should('be.visible');
      });
    });
  });

  describe('Schema Section', () => {
    beforeEach(() => {
      // @ts-ignore
      cy.mountWithPlugins(FrameworkDocumentation, {
        props: { frameworkId: 'sfdr' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });
      cy.wait('@getSfdrFrameworkSpec');
    });

    it('should display the schema section with correct title', () => {
      assertSchemaSection('Schema Structure');
    });

    it('should parse and display the JSON schema correctly', () => {
      cy.get('.schema-container').should('exist');
      cy.get('.schema-property').should('have.length.greaterThan', 0);
    });

    it('should display nested schema structure with proper indentation', () => {
      cy.get('.nested-object').should('exist');
      cy.get('.schema-property.nested').should('exist');
    });

    it('should render data point references as clickable links', () => {
      assertClickableSchemaLinks();
    });

    it('should display export aliases with correct format', () => {
      assertAliasExportFormat();
    });
  });

  describe('Interactive Features', () => {
    beforeEach(() => {
      // @ts-ignore
      cy.mountWithPlugins(FrameworkDocumentation, {
        props: { frameworkId: 'sfdr' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });
      cy.wait('@getSfdrFrameworkSpec');
    });

    it('should handle clicking on data point type links', () => {
      testSchemaLinkClicks();
    });

    it('should show tooltip on link hover', () => {
      cy.get('.ref-id-link').first().trigger('mouseover');
      cy.get('.ref-id-link').first().should('have.attr', 'title');
    });

    it('should handle keyboard navigation on links', () => {
      cy.get('.ref-id-link').first().focus().should('be.focused');
      cy.get('.ref-id-link').first().type('{enter}');
    });
  });

  describe('Schema Data Processing', () => {
    it('should handle stringified JSON schema correctly', () => {
      const mockSpecWithStringSchema = {
        ...mockSfdrFrameworkSpecification,
        schema: JSON.stringify({
          testField: {
            testSubField: {
              id: 'testId',
              ref: 'https://test.com/test',
              aliasExport: 'TEST_ALIAS',
            },
          },
        }),
      };

      cy.intercept('GET', '/specifications/frameworks/sfdr', {
        statusCode: 200,
        body: mockSpecWithStringSchema,
      }).as('getStringSchemaSpec');

      // @ts-ignore
      cy.mountWithPlugins(FrameworkDocumentation, {
        props: { frameworkId: 'sfdr' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });

      cy.wait('@getStringSchemaSpec');
      cy.get('.schema-container').should('exist');
      cy.contains('testField').should('be.visible');
      cy.contains('testId').should('be.visible');
    });

    it('should handle malformed JSON schema gracefully', () => {
      const mockSpecWithInvalidSchema = {
        ...mockSfdrFrameworkSpecification,
        schema: 'invalid json string{',
      };

      cy.intercept('GET', '/specifications/frameworks/sfdr', {
        statusCode: 200,
        body: mockSpecWithInvalidSchema,
      }).as('getInvalidSchemaSpec');

      // @ts-ignore
      cy.mountWithPlugins(FrameworkDocumentation, {
        props: { frameworkId: 'sfdr' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });

      cy.wait('@getInvalidSchemaSpec');
      cy.get('.example-container').should('exist');
      cy.contains('Raw Schema (JSON string):').should('be.visible');
    });
  });

  describe('Responsive Design', () => {
    beforeEach(() => {
      // @ts-ignore
      cy.mountWithPlugins(FrameworkDocumentation, {
        props: { frameworkId: 'sfdr' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });
      cy.wait('@getSfdrFrameworkSpec');
    });

    it('should display correctly on mobile devices', () => {
      testResponsiveBehavior();
    });

    it('should maintain readability on different screen sizes', () => {
      const viewports = [
        [375, 667], // Mobile
        [768, 1024], // Tablet
        [1920, 1080], // Desktop
      ];

      viewports.forEach(([width, height]) => {
        cy.viewport(width, height);
        cy.get('.specification-content').should('be.visible');
        cy.get('h1').should('be.visible');
        cy.get('.specification-field').should('be.visible');
      });
    });
  });

  describe('Accessibility', () => {
    beforeEach(() => {
      // @ts-ignore
      cy.mountWithPlugins(FrameworkDocumentation, {
        props: { frameworkId: 'sfdr' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });
      cy.wait('@getSfdrFrameworkSpec');
    });

    it('should have proper accessibility features', () => {
      assertAccessibilityFeatures();
    });

    it('should have proper ARIA labels and roles', () => {
      cy.get('main[role="main"]').should('exist');
      cy.get('label').should('exist').and('have.length.greaterThan', 0);
    });

    it('should support keyboard navigation', () => {
      cy.get('body').focus().type('{tab}');
      cy.focused().should('exist');
    });
  });

  describe('Performance', () => {
    it('should load and render content efficiently', () => {
      // @ts-ignore
      cy.mountWithPlugins(FrameworkDocumentation, {
        props: { frameworkId: 'sfdr' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });

      assertPerformanceMetrics();
    });

    it('should handle large schema data without performance issues', () => {
      const largeSchema = {
        ...JSON.parse(mockSfdrFrameworkSpecification.schema),
      };

      // Add many nested fields to test performance
      for (let i = 0; i < 50; i++) {
        largeSchema[`testCategory${i}`] = {
          [`testField${i}`]: {
            id: `testId${i}`,
            ref: `https://test.com/test${i}`,
            aliasExport: `TEST_ALIAS_${i}`,
          },
        };
      }

      const mockSpecWithLargeSchema = {
        ...mockSfdrFrameworkSpecification,
        schema: JSON.stringify(largeSchema),
      };

      cy.intercept('GET', '/specifications/frameworks/sfdr', {
        statusCode: 200,
        body: mockSpecWithLargeSchema,
      }).as('getLargeSchemaSpec');

      // @ts-ignore
      cy.mountWithPlugins(FrameworkDocumentation, {
        props: { frameworkId: 'sfdr' },
        keycloak: minimalKeycloakMock({ authenticated: false }),
      });

      cy.wait('@getLargeSchemaSpec');
      cy.get('.schema-container', { timeout: 5000 }).should('be.visible');
      cy.get('.schema-property').should('have.length.greaterThan', 50);
    });
  });
});