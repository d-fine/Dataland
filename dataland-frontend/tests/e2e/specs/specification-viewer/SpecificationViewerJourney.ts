import { describeIf } from '@e2e/support/TestUtility';

describeIf(
  'As a business user, I want to explore framework specifications',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  function (): void {
    beforeEach(() => {
      cy.ensureLoggedIn();
    });

    it('Should allow complete specification exploration journey', () => {
      // Step 1: Navigate to specifications page
      cy.visit('/framework-specifications');
      cy.url().should('include', '/framework-specifications');

      // Step 2: Verify framework selector is visible
      cy.get('[data-test="framework-selector"]', { timeout: 10000 }).should('be.visible');
      cy.get('.framework-select').should('be.visible');

      // Step 3: Verify empty state is shown initially
      cy.get('.empty-state').should('be.visible');
      cy.get('.empty-state').should('contain.text', 'Select a framework');

      // Step 4: Open framework dropdown
      cy.get('.framework-select', { timeout: 10000 }).should('not.be.disabled');
      cy.get('.framework-select').click();

      // Step 5: Select a framework (using PCAF which exists in backend)
      // Note: This test requires PCAF framework to be deployed in the backend
      cy.get('.p-select-overlay', { timeout: 5000 }).should('be.visible');
      cy.contains('.p-select-option', 'PCAF', { timeout: 5000 }).should('exist').click();

      // Step 6: Verify URL updates with framework query param
      cy.url({ timeout: 5000 }).should('include', '?framework=');

      // Step 7: Verify metadata panel displays
      cy.get('[data-test="framework-metadata"]', { timeout: 10000 }).should('be.visible');
      cy.get('.framework-name', { timeout: 5000 }).should('contain.text', 'PCAF');

      // Step 8: Verify schema tree renders
      cy.get('[data-test="section-header"]', { timeout: 10000 }).should('exist');
      cy.get('[data-test="section-header"]').should('have.length.at.least', 1);

      // Step 9: Verify top-level sections are initially collapsed
      cy.get('[data-test="section-header"]').first().should('have.attr', 'aria-expanded', 'false');

      // Step 10: Expand a section
      cy.get('[data-test="section-header"]').first().click();
      cy.get('[data-test="section-header"]').first().should('have.attr', 'aria-expanded', 'true');

      // Step 11: Check if data points or subsections are visible
      // PCAF framework might have nested sections or direct data points
      cy.get('body').then(($body) => {
        if ($body.find('[data-test="datapoint-name"]').length > 0) {
          // Data points found - test data point interactions
          cy.get('[data-test="datapoint-name"]').should('exist');
          cy.get('[data-test="view-details-button"]', { timeout: 5000 }).first().should('be.visible');
          cy.get('[data-test="view-details-button"]').first().click();

          // Verify modal opens
          cy.get('[role="dialog"]', { timeout: 10000 }).should('be.visible');
          cy.get('.p-dialog-header').should('be.visible');

          // Close modal with close button
          cy.get('[role="dialog"]').within(() => {
            cy.get('[data-test="close-dialog"]', { timeout: 5000 }).should('be.visible').click();
          });
          cy.get('[role="dialog"]').should('not.exist');
        } else if ($body.find('[data-test="section-header"]').length > 1) {
          // Nested sections found - expand one more level
          cy.get('[data-test="section-header"]').eq(1).click();
          
          // Check for data points at this level
          cy.get('body').then(($innerBody) => {
            if ($innerBody.find('[data-test="datapoint-name"]').length > 0) {
              cy.get('[data-test="datapoint-name"]').should('exist');
              cy.get('[data-test="view-details-button"]').first().should('be.visible').click();
              cy.get('[role="dialog"]', { timeout: 10000 }).should('be.visible');
              // Close with close button
              cy.get('[role="dialog"]').within(() => {
                cy.get('[data-test="close-dialog"]', { timeout: 5000 }).should('be.visible').click();
              });
              cy.get('[role="dialog"]').should('not.exist');
            }
          });
        }
      });

      // Step 12: Collapse the expanded section
      cy.get('[data-test="section-header"]').first().click();
      cy.get('[data-test="section-header"]').first().should('have.attr', 'aria-expanded', 'false');

      // Step 17: Switch to a different framework (if available)
      cy.get('.framework-select').click();
      
      // Check if SFDR or another framework exists
      cy.get('.p-select-overlay').should('be.visible');
      cy.get('.p-select-option').then(($options) => {
        if ($options.length > 1) {
          // Select second framework if available
          cy.get('.p-select-option').eq(1).click();
          
          // Verify URL updates
          cy.url({ timeout: 5000 }).should('include', '?framework=');
          
          // Verify new framework loads
          cy.get('[data-test="framework-metadata"]', { timeout: 10000 }).should('be.visible');
        }
      });

      // Test complete - specifications viewer is fully functional
    });

    it('Should support keyboard navigation through specifications', () => {
      cy.visit('/framework-specifications?framework=pcaf');

      // Wait for page to load
      cy.get('[data-test="framework-metadata"]', { timeout: 10000 }).should('be.visible');
      cy.get('[data-test="section-header"]', { timeout: 10000 }).should('exist');

      // Focus on first section header
      cy.get('[data-test="section-header"]').first().focus();

      // Verify focus is on section header
      cy.focused().should('have.attr', 'data-test', 'section-header');

      // Toggle section with Enter key
      cy.focused().type('{enter}');
      cy.get('[data-test="section-header"]').first().should('have.attr', 'aria-expanded', 'true');

      // Toggle with Space key
      cy.get('[data-test="section-header"]').first().focus();
      cy.focused().type(' ');
      cy.get('[data-test="section-header"]').first().should('have.attr', 'aria-expanded', 'false');

      // Toggle back with Enter
      cy.focused().type('{enter}');
      cy.get('[data-test="section-header"]').first().should('have.attr', 'aria-expanded', 'true');

      // Check if View Details button exists (depends on framework structure)
      cy.get('body').then(($body) => {
        if ($body.find('[data-test="view-details-button"]').length > 0) {
          // Tab to View Details button
          cy.get('[data-test="view-details-button"]', { timeout: 5000 }).first().focus();

          // Trigger with Enter key
          cy.focused().type('{enter}');

          // Verify modal opens
          cy.get('[role="dialog"]', { timeout: 10000 }).should('be.visible');

          // Close with ESC key
          cy.get('body').type('{esc}');
          cy.get('[role="dialog"]').should('not.exist');
        }
      });
    });

    it('Should persist framework selection across browser navigation', () => {
      // Select framework
      cy.visit('/framework-specifications');
      cy.get('[data-test="framework-selector"]', { timeout: 10000 }).should('be.visible');
      cy.get('.framework-select').click();
      cy.contains('.p-select-option', 'PCAF', { timeout: 5000 }).click();

      // Verify framework loads
      cy.get('[data-test="framework-metadata"]', { timeout: 10000 }).should('be.visible');
      cy.url().should('include', '?framework=');

      // Navigate away
      cy.visit('/');

      // Navigate back with browser back button
      cy.go('back');

      // Framework should still be selected from URL
      cy.get('[data-test="framework-metadata"]', { timeout: 10000 }).should('be.visible');
      cy.url().should('include', '?framework=');
    });

    it('Should handle direct URL access with framework parameter', () => {
      // Visit specifications page directly with framework parameter
      cy.visit('/framework-specifications?framework=pcaf');

      // Framework should be auto-loaded
      cy.get('[data-test="framework-metadata"]', { timeout: 10000 }).should('be.visible');
      cy.get('.framework-name').should('contain.text', 'PCAF');

      // Schema tree should be rendered
      cy.get('[data-test="section-header"]', { timeout: 10000 }).should('exist');
    });
  }
);
