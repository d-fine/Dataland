import { describeIf } from '@e2e/support/TestUtility';
import { type CompanyIdAndName, DataTypeEnum } from '@clients/backend';
import { submitButton } from '@sharedUtils/components/SubmitButton';
import { fetchTestCompanies, setupCommonInterceptions } from '@e2e/utils/CompanyCockpitPage/CompanyCockpitUtils.ts';

const mediumTimeoutInMs = Number(Cypress.expose('medium_timeout_in_ms') ?? 30000);
const longTimeoutInMs = Number(Cypress.expose('long_timeout_in_ms') ?? 100000);

/**
 * Searches for a specified term in the companies search bar and selects the autocomplete suggestion
 * that matches the given company ID exactly, avoiding ambiguity when multiple companies share a name.
 * @param searchTerm the term to search for
 * @param companyId the company ID of the option to click
 */
function searchCompanyAndChooseById(searchTerm: string, companyId: string): void {
  const optionSelector = `[data-pc-section="option"][data-company-id="${companyId}"]`;
  cy.get('input#company_search_bar_standard').scrollIntoView();
  cy.get('input#company_search_bar_standard').type(searchTerm);
  cy.get(optionSelector).should('be.visible').click();
}

/**
 * Searches for a specified term in the companies search bar on the landing page and selects the first autocomplete suggestion
 * @param searchTerm the term to search for
 */
function searchCompanyAndChooseFirstSuggestionLanding(searchTerm: string): void {
  const searchSectionText = 'Search sustainability data by company name or LEI';
  const searchInputSelector = '#company-search-input';
  const optionSelector = '#company-search-listbox li[role="option"]';

  cy.contains('section', searchSectionText).scrollIntoView();

  cy.contains('section', searchSectionText).within(() => {
    cy.get(searchInputSelector, { timeout: 10000 }).should('be.visible');
    cy.get(searchInputSelector).click();
    cy.get(searchInputSelector).clear();
    cy.get(searchInputSelector).type(searchTerm);
    cy.get(searchInputSelector).should('have.value', searchTerm);
    cy.get(searchInputSelector).should('have.focus');
  });

  cy.wait('@companiesNameSearch').then(({ response }) => {
    const count = Array.isArray(response?.body) ? response?.body.length : '?';
    cy.task('log', `[DEBUG] companies/names response: ${count} results at ${new Date().toISOString()}`);
    expect(JSON.stringify(response?.body)).to.contain(searchTerm);
  });

  cy.contains(optionSelector, searchTerm, { timeout: 5000 }).should('be.visible').click({ scrollBehavior: false });
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

    afterEach(function (this: Mocha.Context) {
      if (this.currentTest?.state === 'failed') {
        const screenshotName = `failure-${this.currentTest.fullTitle()}`.replace(/[<>:"/\\|?*]+/g, '-');

        cy.screenshot(screenshotName, {
          capture: 'fullPage',
        });
      }
    });

    it('From the landing page visit the company cockpit via the searchbar', () => {
      cy.intercept('GET', '/api/companies/names*').as('companiesNameSearch');
      cy.intercept('GET', '/scripts/companySearchBar.js').as('companySearchBar');
      cy.visitAndCheckAppMount('/');
      cy.wait('@companySearchBar');
      searchCompanyAndChooseFirstSuggestionLanding(alphaCompanyIdAndName.companyName);
      cy.get('[data-test="companyNameTitle"]', { timeout: longTimeoutInMs }).contains(
        alphaCompanyIdAndName.companyName
      );
    });

    it('From the company cockpit page visit the company cockpit of a different company', () => {
      cy.intercept('GET', `**/api/companies/${betaCompanyIdAndName.companyId}/aggregated-framework-data-summary`).as(
        'fetchAggregatedFrameworkSummaryForBeta'
      );
      visitCockpitForCompanyAlpha();
      searchCompanyAndChooseById(betaCompanyIdAndName.companyName, betaCompanyIdAndName.companyId);
      cy.wait('@fetchAggregatedFrameworkSummaryForBeta');
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
