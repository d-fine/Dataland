/**
 * Shared test utilities for documentation components
 */

/**
 * Common assertions for documentation page structure
 */
export const assertDocumentationPageStructure = () => {
  // Check header and navigation
  cy.get('header').should('exist');
  cy.get('[data-cy="back-button"]').should('exist');
  cy.get('h1').should('exist').and('be.visible');
  
  // Check main content area
  cy.get('main').should('exist');
  cy.get('.specification-content').should('exist');
  
  // Check footer
  cy.get('footer').should('exist');
};

/**
 * Common assertions for loading state
 */
export const assertLoadingState = () => {
  cy.get('.pi-spinner').should('exist');
  cy.contains('Loading specification data...').should('be.visible');
};

/**
 * Common assertions for error state
 */
export const assertErrorState = (errorMessage = 'Error loading specification data') => {
  cy.get('[data-cy="error-message"]').should('exist');
  cy.contains(errorMessage).should('be.visible');
};

/**
 * Common assertions for specification details section
 */
export const assertSpecificationDetailsSection = (title: string) => {
  cy.get('.surface-card').first().within(() => {
    cy.get('h2').should('contain.text', title);
    cy.get('.specification-details').should('exist');
    cy.get('.specification-field').should('have.length.greaterThan', 0);
  });
};

/**
 * Assertions for basic specification fields (ID, Name, Business Definition)
 */
export const assertBasicSpecificationFields = (expectedData: {
  id?: string;
  name?: string;
  businessDefinition?: string;
  validatedBy?: string;
}) => {
  if (expectedData.id) {
    cy.contains('ID:').parent().should('contain.text', expectedData.id);
  }
  
  if (expectedData.name) {
    cy.contains('Name:').parent().should('contain.text', expectedData.name);
  }
  
  if (expectedData.businessDefinition) {
    cy.contains('Business Definition:').parent().should('contain.text', expectedData.businessDefinition);
  }
  
  if (expectedData.validatedBy) {
    cy.contains('Validated By:').parent().should('contain.text', expectedData.validatedBy);
  }
};

/**
 * Assertions for framework-specific fields
 */
export const assertFrameworkSpecificationFields = (expectedData: {
  frameworkId?: string;
  frameworkRef?: string;
  referencedReportJsonPath?: string;
}) => {
  if (expectedData.frameworkId) {
    cy.contains('Framework ID:').parent().should('contain.text', expectedData.frameworkId);
  }
  
  if (expectedData.frameworkRef) {
    cy.contains('Framework Reference:').parent().should('contain.text', expectedData.frameworkRef);
  }
  
  if (expectedData.referencedReportJsonPath) {
    cy.contains('Referenced Report JSON Path:').parent().should('contain.text', expectedData.referencedReportJsonPath);
  }
};

/**
 * Common assertions for schema/example section
 */
export const assertSchemaSection = (title: string) => {
  cy.get('.surface-card').last().within(() => {
    cy.get('h3').should('contain.text', title);
    cy.get('[data-cy="schema-container"], .example-container').should('exist');
  });
};

/**
 * Assertions for clickable schema links
 */
export const assertClickableSchemaLinks = () => {
  cy.get('.ref-id-link').should('exist').and('have.length.greaterThan', 0);
  
  // Test that links are clickable and have proper attributes
  cy.get('.ref-id-link').first().should('have.attr', 'href', '#');
  cy.get('.ref-id-link').first().should('have.attr', 'title');
};

/**
 * Test clicking on schema links
 */
export const testSchemaLinkClicks = () => {
  // Mock window.open to prevent actual navigation during tests
  cy.window().then((win) => {
    cy.stub(win, 'open').as('windowOpen');
  });
  
  cy.get('.ref-id-link').first().click();
  cy.get('@windowOpen').should('have.been.called');
};

/**
 * Assertions for alias export display
 */
export const assertAliasExportFormat = () => {
  cy.get('.alias-export').should('exist');
  cy.get('.alias-export').should('contain.text', 'Export alias:');
  cy.get('.alias-export').should('not.contain.text', '(');
  cy.get('.alias-export').should('not.contain.text', ')');
};

/**
 * Test responsive behavior
 */
export const testResponsiveBehavior = () => {
  // Test mobile viewport
  cy.viewport(375, 667);
  cy.get('.specification-content').should('be.visible');
  cy.get('.schema-container, .example-container').should('be.visible');
  
  // Test desktop viewport
  cy.viewport(1920, 1080);
  cy.get('.specification-content').should('be.visible');
  cy.get('.schema-container, .example-container').should('be.visible');
};

/**
 * Common accessibility checks
 */
export const assertAccessibilityFeatures = () => {
  // Check for proper heading hierarchy
  cy.get('h1').should('exist');
  cy.get('h2').should('exist');
  
  // Check for proper semantic markup
  cy.get('main[role="main"]').should('exist');
  cy.get('label').should('exist');
  
  // Check for keyboard navigation
  cy.get('.ref-id-link').first().focus().should('be.focused');
};

/**
 * Performance and loading checks
 */
export const assertPerformanceMetrics = () => {
  // Check that component loads within reasonable time
  cy.get('.specification-content', { timeout: 5000 }).should('be.visible');
  
  // Check that large schema renders efficiently
  cy.get('.schema-container', { timeout: 3000 }).should('be.visible');
};