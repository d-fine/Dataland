import { reader_name, reader_pw, uploader_name, uploader_pw } from '@e2e/utils/Cypress';
import { searchBasicCompanyInformationForDataType } from '@e2e/utils/GeneralApiUtils';
import { getKeycloakToken } from '@e2e/utils/Auth';
import { describeIf } from '@e2e/support/TestUtility';
import { type CompanyIdAndName, DataTypeEnum } from '@clients/backend';
import { submitButton } from '@sharedUtils/components/SubmitButton';

describeIf(
  'As a user, I want the navigation around the company cockpit to work as expected',
  {
    executionEnvironments: ['developmentLocal', 'ci', 'developmentCd'],
  },
  () => {
    let alphaCompanyIdAndName: CompanyIdAndName;
    let betaCompanyIdAndName: CompanyIdAndName;

    before(() => {
      getKeycloakToken(reader_name, reader_pw)
        .then((token: string) => {
          return searchBasicCompanyInformationForDataType(token, DataTypeEnum.EutaxonomyNonFinancials);
        })
        .then((basicCompanyInfos) => {
          expect(basicCompanyInfos).to.be.not.empty;
          alphaCompanyIdAndName = {
            companyId: basicCompanyInfos[0].companyId,
            companyName: basicCompanyInfos[0].companyName,
          };
          betaCompanyIdAndName = {
            companyId: basicCompanyInfos[1].companyId,
            companyName: basicCompanyInfos[1].companyName,
          };
        });
    });

    beforeEach(() => {
      cy.intercept('https://youtube.com/**', []);
      cy.intercept('https://jnn-pa.googleapis.com/**', []);
      cy.intercept('https://play.google.com/**', []);
      cy.intercept('https://googleads.g.doubleclick.net/**', []);
    });

    it('From the landing page visit the company cockpit via the searchbar', () => {
      cy.visitAndCheckAppMount('/');
      cy.closeCookieBannerIfItExists();
      searchCompanyAndChooseFirstSuggestion(alphaCompanyIdAndName.companyName);
      cy.get('[data-test="companyNameTitle"]', { timeout: Cypress.env('long_timeout_in_ms') as number }).contains(
        alphaCompanyIdAndName.companyName
      );
    });

    it('From the company cockpit page visit the company cockpit of a different company', () => {
      visitCockpitForCompanyAlpha();
      cy.closeCookieBannerIfItExists();
      cy.intercept('GET', `**/api/companies/${betaCompanyIdAndName.companyId}/aggregated-framework-data-summary`).as(
        'fetchAggregatedFrameworkSummaryForBeta'
      );
      searchCompanyAndChooseFirstSuggestion(betaCompanyIdAndName.companyName);
      cy.wait('@fetchAggregatedFrameworkSummaryForBeta');
      cy.url({ timeout: Cypress.env('long_timeout_in_ms') as number }).should(
        'not.contain',
        `/companies/${alphaCompanyIdAndName.companyId}`
      );
      cy.get('[data-test="companyNameTitle"]', { timeout: Cypress.env('long_timeout_in_ms') as number }).contains(
        betaCompanyIdAndName.companyName
      );
    });

    it('From the company cockpit page visit a view page', () => {
      cy.ensureLoggedIn(uploader_name, uploader_pw);
      visitCockpitForCompanyAlpha();
      cy.closeCookieBannerIfItExists();
      cy.get(`[data-test='${DataTypeEnum.EutaxonomyNonFinancials}-summary-panel']`).click();
      cy.url({ timeout: Cypress.env('long_timeout_in_ms') as number }).should(
        'contain',
        `/companies/${alphaCompanyIdAndName.companyId}/frameworks/${DataTypeEnum.EutaxonomyNonFinancials}`
      );
      cy.get("[data-test='frameworkDataTableTitle']").should('exist');
    });

    it('From the company cockpit page visit an upload page', () => {
      cy.ensureLoggedIn(uploader_name, uploader_pw);
      visitCockpitForCompanyAlpha();
      cy.closeCookieBannerIfItExists();
      cy.get(`[data-test='${DataTypeEnum.EutaxonomyFinancials}-summary-panel'] a`).click();
      cy.url({ timeout: Cypress.env('long_timeout_in_ms') as number }).should(
        'contain',
        `/companies/${alphaCompanyIdAndName.companyId}/frameworks/${DataTypeEnum.EutaxonomyFinancials}/upload`
      );
      submitButton.exists();
    });
    it('From the company cockpit page claim company ownership via the panel and context menu', () => {
      cy.ensureLoggedIn(uploader_name, uploader_pw);
      visitCockpitForCompanyAlpha();
      cy.closeCookieBannerIfItExists();
      cy.get("[data-test='claimOwnershipPanelLink']").click();
      submitOwnershipClaimForCompanyAlpha('This is a test message for claiming ownership via panel.');
      cy.get("[data-test='contextMenuButton']").click();
      cy.get("[data-test='contextMenuItem']").should('contain.text', 'Claim').click();
      submitOwnershipClaimForCompanyAlpha(
        'This is a test message for claiming ownership via context menu in company info'
      );
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
      cy.get("[data-test='claimOwnershipDialogSubmittedMessage']").should('exist');
      cy.get("[data-test='claimOwnershipDialogMessage']").should('not.exist');
      cy.get("[data-test='closeButton']").should('exist').click();
      cy.get("[id='claimOwnerShipDialog']").should('not.exist');
    }

    /**
     * Visit the company cockpit of a predefined company
     */
    function visitCockpitForCompanyAlpha(): void {
      cy.visit(`/companies/${alphaCompanyIdAndName.companyId}`);
      cy.contains('[data-test="companyNameTitle"]', alphaCompanyIdAndName.companyName).should('exist');
    }

    /**
     * Searches for a specified term in the companies search bar and selects the first autocomplete suggestion
     * @param searchTerm the term to search for
     */
    function searchCompanyAndChooseFirstSuggestion(searchTerm: string): void {
      cy.get('input#company_search_bar_standard').scrollIntoView();
      cy.get('input#company_search_bar_standard').type(searchTerm);
      cy.get('[data-pc-section="panel"]').contains(searchTerm).click();
    }
  }
);
