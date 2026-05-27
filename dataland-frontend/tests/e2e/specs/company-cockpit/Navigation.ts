import { describeIf } from '@e2e/support/TestUtility';
import { type CompanyIdAndName, DataTypeEnum } from '@clients/backend';
import { submitButton } from '@sharedUtils/components/SubmitButton';
import { fetchTestCompanies, setupCommonInterceptions } from '@e2e/utils/CompanyCockpitPage/CompanyCockpitUtils.ts';

const mediumTimeoutInMs = Number(Cypress.expose('medium_timeout_in_ms') ?? 30000);
const longTimeoutInMs = Number(Cypress.expose('long_timeout_in_ms') ?? 100000);

/**
 * Searches for a specified term in the companies search bar and selects the first autocomplete suggestion
 * @param searchTerm the term to search for
 */
function searchCompanyAndChooseFirstSuggestion(searchTerm: string): void {
  cy.get('input#company_search_bar_standard').scrollIntoView();
  cy.get('input#company_search_bar_standard').then(($input) => {
    cy.task('log', `[navigation-test] search bar found, current value="${String($input.val())}"`);
  });
  cy.get('input#company_search_bar_standard').type(searchTerm);
  cy.get('input#company_search_bar_standard').then(($input) => {
    cy.task('log', `[navigation-test] after .type(), input value="${String($input.val())}" (expected="${searchTerm}")`);
  });
  cy.get('[data-pc-section="list"]').then(($list) => {
    cy.task('log', `[navigation-test] autocomplete list visible, items: ${$list.text().trim().replace(/\s+/g, ' ')}`);
  });
  cy.get('[data-pc-section="list"]').contains(searchTerm).click();
}

/**
 * Searches for a specified term in the companies search bar on the landing page and selects the first autocomplete suggestion
 * @param searchTerm the term to search for
 */
function searchCompanyAndChooseFirstSuggestionLanding(searchTerm: string): void {
  cy.contains('section', 'Search sustainability data by company name or LEI').scrollIntoView();
  cy.contains('section', 'Search sustainability data by company name or LEI').within(() => {
    cy.get('#company-search-input', { timeout: 10000 }).should('exist').type(searchTerm);
    cy.contains('#company-search-listbox li[role="option"]', searchTerm, { timeout: 10000 }).click();
  });
}

describeIf(
  'As a user, I want the navigation around the company cockpit to work as expected',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  () => {
    let alphaCompanyIdAndName: CompanyIdAndName;
    let betaCompanyIdAndName: CompanyIdAndName;

    before(() => {
      fetchTestCompanies().then(([alpha, beta]) => {
        alphaCompanyIdAndName = alpha;
        betaCompanyIdAndName = beta;
      });
    });

    beforeEach(() => {
      setupCommonInterceptions();
    });

    it('From the landing page visit the company cockpit via the searchbar', () => {
      cy.visitAndCheckAppMount('/');
      searchCompanyAndChooseFirstSuggestionLanding(alphaCompanyIdAndName.companyName);
      cy.get('[data-test="companyNameTitle"]', { timeout: longTimeoutInMs }).contains(
        alphaCompanyIdAndName.companyName
      );
    });

    it('From the company cockpit page visit the company cockpit of a different company', () => {
      cy.task(
        'log',
        `[navigation-test] betaCompanyId=${betaCompanyIdAndName?.companyId} betaCompanyName=${betaCompanyIdAndName?.companyName}`
      );
      cy.intercept('GET', `**/api/companies/${betaCompanyIdAndName.companyId}/aggregated-framework-data-summary`).as(
        'fetchAggregatedFrameworkSummaryForBeta'
      );
      cy.task('log', '[navigation-test] intercept registered, visiting alpha cockpit');
      visitCockpitForCompanyAlpha();
      cy.task('log', '[navigation-test] alpha cockpit loaded, starting search for beta company');
      searchCompanyAndChooseFirstSuggestion(betaCompanyIdAndName.companyName);
      cy.task('log', '[navigation-test] clicked beta company in autocomplete, waiting for API request');
      cy.wait('@fetchAggregatedFrameworkSummaryForBeta').then(() => {
        cy.task('log', '[navigation-test] fetchAggregatedFrameworkSummaryForBeta request was intercepted successfully');
      });
      cy.url({ timeout: longTimeoutInMs }).should('not.contain', `/companies/${alphaCompanyIdAndName.companyId}`);
      cy.get('[data-test="companyNameTitle"]', { timeout: longTimeoutInMs }).contains(betaCompanyIdAndName.companyName);
    });

    it('From the company cockpit page visit a view page', () => {
      cy.ensureLoggedInAsUploader();
      visitCockpitForCompanyAlpha();
      cy.get(`[data-test='${DataTypeEnum.EutaxonomyNonFinancials}-summary-panel']`).click();
      cy.url({ timeout: longTimeoutInMs }).should(
        'contain',
        `/companies/${alphaCompanyIdAndName.companyId}/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}`
      );
      cy.get("[data-test='frameworkDataTableTitle']").should('exist');
    });

    it('From the company cockpit page visit an upload page', () => {
      cy.ensureLoggedInAsUploader();
      visitCockpitForCompanyAlpha();
      cy.get(`[data-test='${DataTypeEnum.EutaxonomyFinancials}-provide-data-button']`).click();
      cy.url({ timeout: longTimeoutInMs }).should(
        'contain',
        `/companies/${alphaCompanyIdAndName.companyId}/frameworks/${DataTypeEnum.EutaxonomyFinancials}/upload`
      );
      submitButton.exists();
    });
    it('From the company cockpit page claim company ownership via the panel', () => {
      cy.intercept('POST', '**/community/company-ownership/**', {
        statusCode: 200,
      }).as('postCompanyOwnershipRequest');
      cy.ensureLoggedInAsUploader();
      visitCockpitForCompanyAlpha();
      cy.get("[data-test='claimOwnershipPanelLink']").click();
      submitOwnershipClaimForCompanyAlpha('This is a test message for claiming ownership via panel.');
    });

    /**
     * Go through the dialog and claim company ownership with a message
     * @param message message to send with the request
     */
    function submitOwnershipClaimForCompanyAlpha(message: string): void {
      cy.get("[data-test='claimOwnershipDialogMessage']").should('exist');
      cy.get("[data-test='claimOwnershipDialogMessage']").should('contain.text', alphaCompanyIdAndName.companyName);
      cy.get("[data-test='messageInputField']").should('exist').type(message);
      cy.get("[data-test='submitButton']").should('exist').click();
      cy.wait('@postCompanyOwnershipRequest');
      cy.get("[data-test='claimOwnershipDialogSubmittedMessage']", {
        timeout: mediumTimeoutInMs,
      }).should('exist');
      cy.get("[data-test='claimOwnershipDialogMessage']").should('not.exist');
      cy.get("[data-test='closeButton']").should('exist').click();
      cy.get("[id='claimOwnerShipDialog']").should('not.exist');
    }

    /**
     * Visit the company cockpit of a predefined company
     */
    function visitCockpitForCompanyAlpha(): void {
      cy.visitAndCheckAppMount(`/companies/${alphaCompanyIdAndName.companyId}`);
      cy.contains('[data-test="companyNameTitle"]', alphaCompanyIdAndName.companyName).should('exist');
    }
  }
);
